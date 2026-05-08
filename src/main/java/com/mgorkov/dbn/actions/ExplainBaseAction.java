package com.mgorkov.dbn.actions;

import com.dbn.common.action.BackgroundUpdate;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.action.Lookups;
import com.dbn.common.util.Editors;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.DatabaseType;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.mgorkov.dbn.explain.ExplainPlanManager;
import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.language.common.psi.ExecutablePsiElement;
import com.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.mgorkov.file.PgPlanVirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


import static com.dbn.common.dispose.Checks.isNotValid;


@BackgroundUpdate
public class ExplainBaseAction extends ProjectAction {
    private final boolean myRun;

    public ExplainBaseAction(boolean run){
        super();
        this.myRun = run;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent event, @NotNull Project project) {
        Editor editor = Lookups.getEditor(event);
        if (isNotValid(editor)) {
            return;
        }

        FileEditor fileEditor = Editors.getFileEditor(editor);
        if (fileEditor == null) {
            return;
        }

        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (executable == null) {
            return;
        }

        ExplainPlanManager explainPlanManager = ExplainPlanManager.getInstance(project);
        explainPlanManager.executeExplainPlan(executable, event.getDataContext(), myRun, (explainPlanResult -> {
            if (!explainPlanResult.isError()) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    String statement = explainPlanResult.statementText;
                    String planJson = explainPlanResult.planJson;
                    String fileName = String.format("Explain: %s", StringUtils.substring(statement, 0, 6));
                    PgPlanVirtualFile file = new PgPlanVirtualFile(fileName, planJson, true, null, statement);
                    FileEditorManager.getInstance(project).openFile(file, true);
                });
            }
        }));

    }


    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();

        boolean visible = false;
        boolean enabled = false;

        Editor editor = Lookups.getEditor(e);
        if (editor != null) {
            PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
            if (psiFile instanceof DBLanguagePsiFile languagePsiFile) {
                ConnectionHandler connection = languagePsiFile.getConnection();
                if (connection == null) {
                    return;
                }
                DatabaseType databaseType = connection.getDatabaseType();
                visible = isVisible(e) && databaseType == DatabaseType.POSTGRES;
                if (visible) {
                    ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
                    if (executable != null) {
                        enabled = true;
                    }
                }
            }
        }
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }

}
