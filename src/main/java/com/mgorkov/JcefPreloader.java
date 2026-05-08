package com.mgorkov;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.ui.jcef.JBCefApp;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JcefPreloader implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                if (JBCefApp.isSupported()) {
                    // JCEF init
                    JBCefApp.getInstance();
                }
            } catch (Throwable t) {
                Logger.getInstance(JcefPreloader.class).debug("JCEF preloading failed", t);
            }
        });

        return Unit.INSTANCE;
    }
}