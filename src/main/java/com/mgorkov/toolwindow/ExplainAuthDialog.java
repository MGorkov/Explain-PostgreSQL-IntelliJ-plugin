package com.mgorkov.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ExplainAuthDialog extends DialogWrapper {
    Consumer<String> callback;
    private static final String EXPLAIN_TITLE = "Explain PostgreSQL";

    public ExplainAuthDialog() {
        this(null);
    }

    public ExplainAuthDialog(Consumer<String> callback) {
        super(true); // use current window as parent
        this.callback = callback;
        setTitle("Explain Auth");
        init();
        setModal(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        ExplainBrowser browser = new ExplainBrowser(true);
        dialogPanel.add(browser.getComponent(), BorderLayout.CENTER);

        browser.getCefBrowser().getClient().addDisplayHandler(new CefDisplayHandler() {
            private String title = "";

            @Override
            public void onTitleChange(CefBrowser cefBrowser, String s) {
                if (!title.equals(s)) {
                    title = s;
                    if (s.equals(EXPLAIN_TITLE)) {
                        if (callback != null) {
                            callback.accept("REPEAT");
                        }
                        ApplicationManager.getApplication().invokeLater(() -> {
                            close(OK_EXIT_CODE);
                        });
                    }
                }
            }

            @Override
            public void onAddressChange(CefBrowser cefBrowser, CefFrame cefFrame, String s) {

            }

            @Override
            public boolean onTooltip(CefBrowser cefBrowser, String s) {
                return false;
            }

            @Override
            public void onStatusMessage(CefBrowser cefBrowser, String s) {

            }

            @Override
            public boolean onConsoleMessage(CefBrowser cefBrowser, CefSettings.LogSeverity logSeverity, String s, String s1, int i) {
                return false;
            }

            @Override
            public boolean onCursorChange(CefBrowser cefBrowser, int i) {
                return false;
            }
        });

        return dialogPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }
}
