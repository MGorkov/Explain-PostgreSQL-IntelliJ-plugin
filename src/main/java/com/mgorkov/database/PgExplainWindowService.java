package com.mgorkov.database;

public class PgExplainWindowService {
    public ExplainWindow createWindow(String plan, ExplainContext context) {
        return new ExplainWindow(plan, context);
    }
}
