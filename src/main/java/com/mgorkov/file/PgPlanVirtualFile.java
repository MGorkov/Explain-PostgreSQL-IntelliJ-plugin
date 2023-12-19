package com.mgorkov.file;

import com.intellij.database.console.JdbcConsole;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PgPlanVirtualFile extends LightVirtualFile {
    //did we actually run the query with analyze
    private final boolean executed;

    //console that executed this explain query
    private final JdbcConsole console;

    private final String query;

    public PgPlanVirtualFile(@NotNull String name, @NotNull String planText, boolean executed, JdbcConsole console, String query) {
        super(name, PgPlanFileType.INSTANCE, planText);
        this.executed = executed;
        this.console = console;
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public JdbcConsole getConsole() {
        return console;
    }

    public boolean isExecuted() {
        return executed;
    }
}
