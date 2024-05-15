package com.mgorkov.database;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;

import com.mgorkov.toolwindow.ExplainBrowser;
import com.mgorkov.api.ExplainApiService;

import javax.swing.*;

public class ExplainWindow {
    private final Logger log = Logger.getInstance(ExplainWindow.class);

    private final ExplainContext context;

    private ExplainBrowser browser;

    private String plan;
    private String query;

    ExplainApiService explainApi;

    public ExplainWindow(String plan, ExplainContext context) {
        this.context = context;
        this.browser = new ExplainBrowser(false);
        this.explainApi = ApplicationManager.getApplication().getService(ExplainApiService.class);

        this.setPlan(plan);

        Disposer.register(context.getEditor(), browser);
    }

    public JComponent getContent(){
        return browser.getComponent();
    }

    public void updatePlan(String plan){
        log.debug("Updating plan");
        this.setPlan(plan);
    }

    private void setPlan(String plan){
        this.plan = plan;
        this.query = this.context.file.getQuery();

        explainApi.plan_archive(plan, query, (url) -> {
            this.browser.load(url);
        });
    }

}