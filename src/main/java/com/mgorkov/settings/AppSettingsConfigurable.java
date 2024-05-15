package com.mgorkov.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.mgorkov.toolwindow.ExplainAuthDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;

public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent appSettingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Explain PostgreSQL";
    }

    @Override
    public @Nullable JComponent createComponent() {
        appSettingsComponent = new AppSettingsComponent();
        return appSettingsComponent.getPanel();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return appSettingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !settings.getExplainUrl().equals(appSettingsComponent.getExplainUrl());
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        AppSettingsState settings = AppSettingsState.getInstance();
        String explainUrl = appSettingsComponent.getExplainUrl();
        try {
            new URL(explainUrl).openStream().close();
            settings.setExplainUrl(explainUrl);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("401")) {
                settings.setExplainUrl(explainUrl);
                ExplainAuthDialog explainAuthDialog = new ExplainAuthDialog();
                explainAuthDialog.show();
            } else {
                throw new ConfigurationException(message);
            }
        }
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        appSettingsComponent.setExplainUrl(settings.getExplainUrl());
    }

    @Override
    public void disposeUIResources() {
        appSettingsComponent = null;
    }
}
