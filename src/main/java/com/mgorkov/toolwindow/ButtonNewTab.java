package com.mgorkov.toolwindow;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.ui.AnActionButton;
import com.mgorkov.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ButtonNewTab extends AnActionButton {

	public ButtonNewTab(String text, String descr, Icon icon) {
		super(text, descr, icon);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (e.getInputEvent().isControlDown()) {
			BrowserUtil.browse(AppSettingsState.getInstance().getExplainUrl());
		} else {
			ExplainToolWindowFactory.createNewTab(e.getProject());
		}
	}

	@Override
	public void updateButton(@NotNull AnActionEvent e) {
		super.updateButton(e);
		boolean enabled = ExplainBrowser.isSupported && ExplainToolWindowFactory.getToolWindow(e.getProject()).isVisible();
		e.getPresentation().setEnabled(enabled);
	}
}
