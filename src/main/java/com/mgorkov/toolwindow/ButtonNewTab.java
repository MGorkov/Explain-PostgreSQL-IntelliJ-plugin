package com.mgorkov.toolwindow;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.mgorkov.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ButtonNewTab extends DumbAwareAction {

	public ButtonNewTab(String text, String descr, Icon icon) {
		super(text, descr, icon);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (e.getInputEvent() != null && e.getInputEvent().isControlDown()) {
			BrowserUtil.browse(AppSettingsState.getInstance().getExplainUrl());
		} else {
			ExplainToolWindowFactory.createNewTab(e.getProject());
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		boolean enabled = ExplainBrowser.isSupported && 
						 ExplainToolWindowFactory.getToolWindow(e.getProject()) != null && 
						 ExplainToolWindowFactory.getToolWindow(e.getProject()).isVisible();
		e.getPresentation().setEnabled(enabled);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

}