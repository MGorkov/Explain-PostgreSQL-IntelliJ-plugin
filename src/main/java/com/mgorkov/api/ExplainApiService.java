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
import com.mgorkov.settings.AppSettingsState;
import com.mgorkov.settings.Subscriber;
import com.mgorkov.toolwindow.ExplainBrowser;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public final class ExplainApiService implements Subscriber, Disposable {
    private static final Logger log = Logger.getInstance(ExplainApiService.class);
    private String EXPLAIN_URL;
    private final String EXPLAIN_URL_TOPIC = "EXPLAIN_URL";
    private static final String API_BEAUTIFIER = "/beautifier-api";
    private static final String API_PLANARCHIVE = "/explain";
    private ExplainBrowser browser;

    public ExplainApiService() {
        AppSettingsState settings = AppSettingsState.getInstance();
        this.EXPLAIN_URL = settings.getExplainUrl();
        settings.addSubscriber(EXPLAIN_URL_TOPIC, this);
        browser = new ExplainBrowser(false);
    }

    public void beautifier(AnActionEvent actionEvent, @NotNull String sql, Consumer<String> callback) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("query_src", sql);
        log.debug("POST JSON: " + jsonObject);
        browser.request(EXPLAIN_URL + API_BEAUTIFIER, jsonObject.toString(), (data) -> {
            log.debug("Beautifier result: " + data);
            if (data.equals("REPEAT")) {
                beautifier(actionEvent, sql, callback);
            } else {
                JsonElement jsonElement = JsonParser.parseString(data);
                JsonObject object = jsonElement.getAsJsonObject();
                String btf_query = object.get("btf_query").getAsString();
                String btf_query_text = object.get("btf_query_text").getAsString();
                if (btf_query.equals(btf_query_text)) {
                    showErrorMessage(actionEvent, btf_query_text);
                    callback.accept(null);
                } else {
                    callback.accept(btf_query_text);
                }
            }
        });
    }

    public void plan_archive(String plan, String query, Consumer<String> callback) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("plan", plan);
        jsonObject.addProperty("query", query);
        log.debug("POST JSON: " + jsonObject + " URL: " + EXPLAIN_URL + API_PLANARCHIVE);

        browser.request(EXPLAIN_URL + API_PLANARCHIVE, jsonObject.toString(), (data) -> {
            if (data.equals("REPEAT")) {
                plan_archive(plan, query, callback);
            } else {
                callback.accept(EXPLAIN_URL + data);
            }
        });
    }

    private void showErrorMessage(@NotNull AnActionEvent e, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            HintManager.getInstance().showErrorHint(Objects.requireNonNull(e.getData(CommonDataKeys.EDITOR)), msg);
        });
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
        browser.dispose();
    }
}
