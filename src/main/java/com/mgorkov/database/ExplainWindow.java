package com.mgorkov.database;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefApp;
import com.mgorkov.toolwindow.ExplainBrowser;
import com.mgorkov.api.ExplainApiService;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExplainWindow {
    private final Logger log = Logger.getInstance(ExplainWindow.class);

    private final ExplainContext context;

    private final AtomicReference<ExplainBrowser> browserRef = new AtomicReference<>();
    private volatile boolean browserCreated = false;

    private String plan;
    private String query;
    private String pendingUrl = null;

    ExplainApiService explainApi;

    public ExplainWindow(String plan, ExplainContext context) {
        this.context = context;
        this.explainApi = ApplicationManager.getApplication().getService(ExplainApiService.class);
        this.setPlan(plan);
    }

    public JComponent getContent(){
        return getOrCreateBrowser().getComponent();
    }

    private ExplainBrowser getOrCreateBrowser() {
        if (!browserCreated) {
            synchronized (this) {
                if (!browserCreated) {
                    if (JBCefApp.isSupported()) {
                        ExplainBrowser browser = new ExplainBrowser(false);
                        browserRef.set(browser);
                        Disposer.register(context.getEditor(), browser);
                        
                        if (pendingUrl != null) {
                            browser.load(pendingUrl);
                            pendingUrl = null;
                        }
                    }
                    browserCreated = true;
                }
            }
        }
        return browserRef.get();
    }

    public void updatePlan(String plan){
        log.debug("Updating plan");
        this.setPlan(plan);
    }

    private void setPlan(String plan){
        this.plan = plan;
        this.query = this.context.file.getQuery();

        explainApi.plan_archive(plan, query, (url) -> {
            if (browserCreated && browserRef.get() != null) {
                browserRef.get().load(url);
            } else {
                pendingUrl = url;
                getOrCreateBrowser();
            }
        });
    }
}