package com.mgorkov.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class ExplainToolWindowFactory implements ToolWindowFactory, DumbAware {

	public static final String explainWindow = "Explain PostgreSQL";

	public static void createNewTab(Project project) {
		ApplicationManager.getApplication().invokeLater((new Runnable() {
			@Override
			public void run() {
				ExplainBrowser browser = new ExplainBrowser(true);
				ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(explainWindow);
				Content content = ContentFactory.getInstance().createContent(browser.getComponent(), "Explain", false);
				content.setDisposer(browser);
				@NotNull ContentManager contentManager = toolWindow.getContentManager();
				contentManager.addContent(content);
				contentManager.setSelectedContent(content);
			}
		}));
	}

	public static ToolWindow getToolWindow(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(explainWindow);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		createNewTab(project);
		ButtonNewTab newTabBtn = new ButtonNewTab("New Tab", null, AllIcons.Toolbar.AddSlot);
		((ToolWindowEx) toolWindow).setTabActions(newTabBtn);
	}

}
