package com.mgorkov.dbn.explain;

import com.dbn.language.common.psi.ExecutablePsiElement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExplainPlanResult {
    public String statementText;
    public String planJson;
    public String errorMessage;

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, ResultSet resultSet) throws SQLException {
        this.statementText = executablePsiElement.getText();
        this.planJson = null;

        if (resultSet == null) {
            throw new SQLException("Result is null");
        }

        if (!resultSet.next()) {
            throw new SQLException("Empty result");
        }

        if (resultSet.getMetaData().getColumnCount() != 1) {
            throw new SQLException("Database returned data in unknown format");
        } else {
            this.planJson = resultSet.getString(1);
            if (this.planJson == null) {
                throw new SQLException("Database returned null plan");
            } else if (resultSet.next()) {
                throw new SQLException("Database returned too many data");
            }
        }

    }

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, String errorMessage) {
        this.statementText = executablePsiElement.getText();
        this.errorMessage = errorMessage;
        this.planJson = null;
    }

    public boolean isError() {
        return errorMessage != null;
    }

}
