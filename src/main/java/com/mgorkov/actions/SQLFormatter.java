package com.mgorkov.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.mgorkov.api.ExplainApiService;
import org.jetbrains.annotations.NotNull;

public class SQLFormatter extends AnAction implements Runnable, DumbAware {
    private final Logger log = Logger.getInstance(SQLFormatter.class);

    private Document document = null;
    private String formatted = null;

    private final ExplainApiService explainApi;

    public SQLFormatter() {
        this.explainApi = ApplicationManager.getApplication().getService(ExplainApiService.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            document = editor.getDocument();
            String text = document.getText();

            explainApi.beautifier(e, text, (formatted) -> {
                log.debug("Get result from beautifier: " + formatted);
                if (formatted != null) {
                    this.formatted = formatted;
                    WriteCommandAction.runWriteCommandAction(e.getProject(), this);
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }

    @Override
    public void run() {
        document.deleteString(0, document.getTextLength());
        document.insertString(0, formatted);
    }
}
