package com.mgorkov.database;

import com.intellij.database.console.JdbcConsole;
import com.mgorkov.file.PgPlanEditor;
import com.mgorkov.file.PgPlanVirtualFile;
import org.jetbrains.annotations.NotNull;

public class ExplainContext {
    @NotNull PgPlanVirtualFile file;
    @NotNull PgPlanEditor editor;

    @NotNull JdbcConsole console;

    public ExplainContext(PgPlanVirtualFile file, PgPlanEditor editor, JdbcConsole console) {
        this.file = file;
        this.editor = editor;
        this.console = console;
    }

    public PgPlanEditor getEditor() {
        return editor;
    }
}
