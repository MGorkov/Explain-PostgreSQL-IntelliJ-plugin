package com.mgorkov.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import kotlinx.coroutines.CoroutineScope;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ExplainBrowser implements Disposable {
	public static final String EXPLAIN_URL = "https://explain.tensor.ru";
	public static final String EMPTY_URL = "about:blank";

	public static final boolean isSupported = JBCefApp.isSupported();
	private JBCefBrowser browser = null;
	private CefBrowser cefBrowser;
	private CefClient cefClient;
	private JPanel panel = null;
	public ExplainBrowser(boolean loadOnStart) {
		if (isSupported) {
			browser = new JBCefBrowser(EMPTY_URL);
			cefBrowser = browser.getCefBrowser();
			cefClient = browser.getJBCefClient().getCefClient();
			if (loadOnStart) load(EXPLAIN_URL);
		} else {
			panel = new JPanel(new BorderLayout());
			JLabel label = new JLabel("JCEF browser is not supported");
			panel.add(label, BorderLayout.CENTER);
		}
	}

	public void load(@Nullable String url) {
		if (this.browser == null) return;
		if (url == null) {
			browser.loadURL(EMPTY_URL);
		} else {
			browser.loadURL(url);
		}
	}

	@Nullable
	public JComponent getComponent() {
		if (browser == null) {
			return panel;
		} else {
			return browser.getComponent();
		}
	}

	@Override
	public void dispose() {
		cefClient.removeLoadHandler();
		cefBrowser.stopLoad();
		cefBrowser.close(false);
		browser.dispose();
	}
}
