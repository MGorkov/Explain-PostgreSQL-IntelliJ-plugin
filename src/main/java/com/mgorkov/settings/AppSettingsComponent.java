package com.mgorkov.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AppSettingsComponent {
    private final JPanel panel;
    private final JBTextField explainUrlTextField = new JBTextField();


    public AppSettingsComponent() {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Explain PostgreSQL site: "), explainUrlTextField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public JComponent getPreferredFocusedComponent() {
        return explainUrlTextField;
    }

    public String getExplainUrl() {
        return explainUrlTextField.getText();
    }

    public void setExplainUrl(@NotNull String url) {
        explainUrlTextField.setText(url);
    }
}
