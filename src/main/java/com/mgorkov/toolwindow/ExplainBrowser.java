package com.mgorkov.toolwindow;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.io.HttpRequests;
import com.mgorkov.settings.AppSettingsState;
import org.cef.browser.CefBrowser;
import org.cef.network.CefCookieManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;

public class ExplainBrowser implements Disposable {
	public static final String EMPTY_URL = "about:blank";

	public static final boolean isSupported = JBCefApp.isSupported();
	final String fullApplicationName = ApplicationInfo.getInstance().getFullApplicationName();
	final String pluginVersion = PluginManagerCore.getPlugin(PluginId.getId("com.mgorkov.explainpostgresql")).getVersion();
	final String userAgent = "JetBrains " + fullApplicationName + " " + pluginVersion;
	private JBCefBrowser browser = null;
	private CefBrowser cefBrowser;
	private JPanel errorPanel = null;

	public ExplainBrowser(boolean loadOnStart) {
		if (isSupported) {
			try {
				createBrowser();
				if (loadOnStart) {
					load(AppSettingsState.getInstance().getExplainUrl());
				}
			} catch (Exception e) {
				Logger.getInstance(ExplainBrowser.class).error("Failed to create browser", e);
				createErrorPanel("Failed to create browser: " + e.getMessage());
			}
		} else {
			createErrorPanel("JCEF browser is not supported");
		}
	}

	private void createBrowser() {
		browser = new JBCefBrowser(EMPTY_URL);
		cefBrowser = browser.getCefBrowser();
	}

	private void createErrorPanel(String message) {
		errorPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(message);
		errorPanel.add(label, BorderLayout.CENTER);
	}

	public CefBrowser getCefBrowser() {
		return cefBrowser;
	}

	public void request(String URL, String json, Consumer<String> callback) {
		ProgressManager.getInstance().run(new Task.Backgroundable(null, "Sending plan for analysis...", true) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				CefCookieManager cookieManager = CefCookieManager.getGlobalManager();
				final StringBuilder cookieHeader = new StringBuilder();
				final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

				cookieManager.visitUrlCookies(URL, true, (cookie, count, total, delete) -> {
					if (cookie != null) {
						if (cookieHeader.length() > 0) cookieHeader.append("; ");
						cookieHeader.append(cookie.name).append("=").append(cookie.value);
					}
					if (count == total - 1) {
						latch.countDown(); // All cookies are set
					}
					return true;
				});

				try {
					latch.await(500, java.util.concurrent.TimeUnit.MILLISECONDS);
				} catch (InterruptedException ignored) {}

				if (indicator.isCanceled()) return;

				executeRequestSynchronously(URL, json, cookieHeader.toString(), callback, indicator);
			}
		});
	}

	private void executeRequestSynchronously(String URL, String json, String cookieString, Consumer<String> callback, ProgressIndicator indicator) {
		try {
			indicator.checkCanceled();
			indicator.setText("Connecting to server...");
			HttpRequests.post(URL, "application/json")
					.userAgent(userAgent)
					.connectTimeout(10000)
					.readTimeout(30000)
					.connect(request -> {
						java.net.HttpURLConnection conn = (java.net.HttpURLConnection) request.getConnection();
						conn.setInstanceFollowRedirects(false);
						if (cookieString != null && !cookieString.isEmpty()) {
							conn.setRequestProperty("Cookie", cookieString);
						}

						indicator.checkCanceled();
						indicator.setText("Sending data...");
						request.write(json);

						int status = conn.getResponseCode();
						indicator.checkCanceled();
						indicator.setText("Receiving response...");

						if (status == 200) {
							callback.accept(request.readString(indicator));
						} else if (status == 301 || status == 302) {
							callback.accept(conn.getHeaderField("Location"));
						} else if (status == 401) {
							ApplicationManager.getApplication().invokeLater(() ->
								new ExplainAuthDialog(callback).show()
							);
						}
						return null;
					});
		} catch (ProcessCanceledException e) {
			Logger.getInstance(ExplainBrowser.class).info("User canceled the request");
			throw e;
		} catch (java.net.SocketTimeoutException e) {
			Logger.getInstance(ExplainBrowser.class).warn("Request timeout for " + URL);
			indicator.setText("Operation timed out");
		} catch (IOException e) {
			Logger.getInstance(ExplainBrowser.class).error("Request failed", e);
			indicator.setText("Request failed");
		}
	}

	public void load(@Nullable String url) {
		if (browser != null) {
			browser.loadURL(url != null ? url : EMPTY_URL);
		}
	}

	@Nullable
	public JComponent getComponent() {
		if (browser != null) {
			return browser.getComponent();
		} else if (errorPanel != null) {
			return errorPanel;
		} else {
			JPanel panel = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Browser not available");
			panel.add(label, BorderLayout.CENTER);
			return panel;
		}
	}

	@Override
	public void dispose() {
		if (browser != null) {
			try {
				browser.dispose();
			} catch (Exception e) {
				Logger.getInstance(ExplainBrowser.class).warn("Error disposing browser", e);
			}
		}
	}
}