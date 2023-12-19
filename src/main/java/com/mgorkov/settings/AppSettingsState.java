package com.mgorkov.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@State(
        name = "com.mgorkov.settings.AppSettingsState",
        storages = @Storage("ExplainPostgreSQLSettingsPlugin.xml")
)
public final class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    private String ExplainUrl = "https://explain.tensor.ru";
    private final String EXPLAIN_URL_TOPIC = "EXPLAIN_URL";

    private final HashMap<String, ArrayList<Subscriber>> subscribers = new HashMap<>();

    public AppSettingsState() {
        subscribers.put(EXPLAIN_URL_TOPIC, new ArrayList<>());
    }

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    public String getExplainUrl() {
        return ExplainUrl;
    }

    public void setExplainUrl(String explainUrl) {
        ExplainUrl = explainUrl;
        for (Subscriber s: subscribers.get(EXPLAIN_URL_TOPIC)) {
            s.handleSettings("EXPLAIN_URL", explainUrl);
        }
    }

    public void addSubscriber(String t, Subscriber s) {
        subscribers.get(t).add(s);
    }

    public void delSubscriber(String t, Subscriber s) {
        subscribers.get(t).remove(s);
    }

    @Override
    public @Nullable AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
