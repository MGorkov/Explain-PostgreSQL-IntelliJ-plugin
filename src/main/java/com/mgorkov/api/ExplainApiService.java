package com.mgorkov.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.mgorkov.settings.AppSettingsState;
import com.mgorkov.settings.Subscriber;
import org.jetbrains.annotations.NotNull;
//import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public final class ExplainApiService implements Subscriber, Disposable {
    private static final Logger log = Logger.getInstance(ExplainApiService.class);
    private String EXPLAIN_URL;
    private final String EXPLAIN_URL_TOPIC = "EXPLAIN_URL";
    private static final String API_BEAUTIFIER = "/beautifier-api";
    private static final String API_PLANARCHIVE = "/explain";

    public ExplainApiService() {
        AppSettingsState settings = AppSettingsState.getInstance();
        this.EXPLAIN_URL = settings.getExplainUrl();
        settings.addSubscriber(EXPLAIN_URL_TOPIC, this);
    }

    public CompletableFuture<String> beautifier(AnActionEvent actionEvent, @NotNull String sql) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("query_src", sql);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("query_src", sql);

        log.debug("POST JSON: " + jsonObject);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(EXPLAIN_URL + API_BEAUTIFIER))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((res) -> {
                    log.debug("Beautifier result: " + res);
                    JsonElement jsonElement = JsonParser.parseString(res);
                    JsonObject object = jsonElement.getAsJsonObject();
                    String btf_query = object.get("btf_query").getAsString();
                    String btf_query_text = object.get("btf_query_text").getAsString();

//                    JSONObject jsonRes = new JSONObject(res);
//                    String btf_query = jsonRes.getString("btf_query");
//                    String btf_query_text = jsonRes.getString("btf_query_text");
                    if (btf_query.equals(btf_query_text)) {
                        showErrorMessage(actionEvent, btf_query_text);
                        return null;
                    }
                    return btf_query_text;
                })
                .exceptionally((ex) -> {
                    log.info("Got exception " + ex.getMessage());
                    return null;
                });
    }

    public CompletableFuture<String> plan_archive(String plan, String query) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("plan", plan);
        jsonObject.addProperty("query", query);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("plan", plan);
//        jsonObject.put("query", query);
        log.debug("POST JSON: " + jsonObject);

        MessageFormat format = new MessageFormat("Found. Redirecting to {0}");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(EXPLAIN_URL + API_PLANARCHIVE))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((res) -> {
                    if (res == null) {
                        log.debug("Received null result, going to " + EXPLAIN_URL);
                        return EXPLAIN_URL;
                    } else {
                        log.debug("Received result: " + res);
                        try {
                            Object[] parse = format.parse(res);
                            log.debug("Parsed result: " + parse[0]);
                            return (EXPLAIN_URL + parse[0].toString());
                        } catch (ParseException e) {
                            log.debug("Parse error: " + e.getMessage());
                            return EXPLAIN_URL;
                        }
                    }
                })
                .exceptionally((ex) -> {
                    log.info("Got exception " + ex.getMessage());
                    return EXPLAIN_URL;
                });
    }

    private void showErrorMessage(@NotNull AnActionEvent e, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            HintManager.getInstance().showErrorHint(Objects.requireNonNull(e.getData(CommonDataKeys.EDITOR)), msg);
        });
/*
        Project project = e.getProject();
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(msg, MessageType.WARNING, null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.above);
*/
    }

    @Override
    public void handleSettings(String key, String value) {
        switch (key) {
            case "EXPLAIN_URL":
                this.EXPLAIN_URL = value;
                break;
        }
    }

    @Override
    public void dispose() {
        AppSettingsState.getInstance().delSubscriber(EXPLAIN_URL_TOPIC, this);
    }
}
