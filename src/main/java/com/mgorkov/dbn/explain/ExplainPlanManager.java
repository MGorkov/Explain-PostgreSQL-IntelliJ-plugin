package com.mgorkov.dbn.explain;

import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.routine.Consumer;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.Resources;
import com.dbn.connection.SchemaId;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.dbn.common.Priority.HIGH;
import static com.dbn.common.component.Components.projectService;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dbn.nls.NlsResources.txt;

public class ExplainPlanManager extends ProjectComponentBase {
    public static final String COMPONENT_NAME = "PGExplain.Project.ExplainPlanManager";
    private static final String explainAnalyzePrefix = "EXPLAIN (FORMAT JSON, BUFFERS, ANALYZE)\n";
    private static final String explainPrefix = "EXPLAIN (FORMAT JSON, VERBOSE)\n";

    protected ExplainPlanManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
    }

    public static ExplainPlanManager getInstance(@NotNull Project project) {
        return projectService(project, ExplainPlanManager.class);
    }

    @Override
    public void disposeInner() {
        super.disposeInner();
    }

    public void executeExplainPlan(
            @NotNull ExecutablePsiElement executable,
            @NotNull DataContext dataContext,
            boolean run,
            @Nullable Consumer<ExplainPlanResult> callback) {

        Project project = getProject();
        String elementDescription = executable.getSpecificElementType().getDescription();
        DBLanguagePsiFile databaseFile = executable.getFile();
        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        contextManager.selectConnectionAndSchema(
                databaseFile.getVirtualFile(),
                dataContext,
                ()-> ConnectionAction.invoke(txt("msg.execution.title.GeneratingExplainPlan"), true, executable,
                        action -> Progress.prompt(getProject(), action, true,
                                txt("prc.execution.title.ExtractingExplainPlan"),
                                txt("prc.execution.text.ExtractingExplainPlanFor", elementDescription),
                                progress -> {
                                    ConnectionHandler connection = action.getConnection();
                                    ExplainPlanResult explainPlanResult = createExplainPlan(executable, connection, run);
                                    if (callback != null) {
                                        callback.accept(explainPlanResult);
                                    }
                                })));
    }

    private static ExplainPlanResult createExplainPlan(
            @NotNull ExecutablePsiElement executable,
            ConnectionHandler connection,
            boolean run
    ) {
        try {
            return DatabaseInterfaceInvoker.load(HIGH,
                "Creating explain plan",
                "Running explain plan for SQL statement",
                connection.getProject(),
                connection.getConnectionId(),
                conn -> {
                    SchemaId currentSchema = executable.getFile().getSchemaId();
                    connection.setCurrentSchema(conn, currentSchema);
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        String explainPlanQuery = (run ? explainAnalyzePrefix : explainPrefix) + executable.prepareStatementText();
                        statement = conn.createStatement();
                        statement.setFetchSize(500);
                        System.out.println("QUERY: " + explainPlanQuery);
                        resultSet = statement.executeQuery(explainPlanQuery);
                        return new ExplainPlanResult(executable, resultSet);
                    } finally {
                        Resources.close(resultSet);
                        Resources.close(statement);
                        Resources.rollbackSilently(conn);
                    }
                });
        } catch (SQLException e) {
            conditionallyLog(e);
            return new ExplainPlanResult(executable, e.getMessage());
        }
    }

}
