package com.mgorkov.toolwindow;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.mgorkov.settings.AppSettingsState;
import org.cef.CefClient;
import org.cef.callback.CefAuthCallback;
import org.cef.callback.CefURLRequestClient;
import org.cef.network.*;
import org.cef.browser.CefBrowser;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;


public class ExplainBrowser implements Disposable {
	public static final String EMPTY_URL = "about:blank";

	public static final boolean isSupported = JBCefApp.isSupported();
	final String fullApplicationName = ApplicationInfo.getInstance().getFullApplicationName();
	final String pluginVersion = PluginManagerCore.getPlugin(PluginId.getId("com.mgorkov.explainpostgresql")).getVersion();
	final String userAgent = "JetBrains " + fullApplicationName + " " + pluginVersion;
	private JBCefBrowser browser = null;
	private CefBrowser cefBrowser;
	private CefClient cefClient;
	private JPanel panel = null;
	AppSettingsState settings;
	public ExplainBrowser(boolean loadOnStart) {
		settings = AppSettingsState.getInstance();
		if (isSupported) {
			browser = new JBCefBrowser(EMPTY_URL);
			cefBrowser = browser.getCefBrowser();
			cefClient = browser.getJBCefClient().getCefClient();
			if (loadOnStart) load(settings.getExplainUrl());
		} else {
			panel = new JPanel(new BorderLayout());
			JLabel label = new JLabel("JCEF browser is not supported");
			panel.add(label, BorderLayout.CENTER);
		}
	}

	public CefBrowser getCefBrowser() {
		return cefBrowser;
	}

	public void request(String URL, String json, Consumer<String> callback) {
		CefRequest cefRequest = CefRequest.create();
		CefPostData cefPostData = CefPostData.create();
		CefPostDataElement cefPostDataElement = CefPostDataElement.create();
		byte[] bytes = json.getBytes();
		cefPostDataElement.setToBytes(bytes.length, bytes);
		cefPostData.addElement(cefPostDataElement);
		Map<String, String> headers = Map.of(
				"Content-Type", "application/json",
				"User-Agent", userAgent
		);
		cefRequest.setMethod("POST");
		cefRequest.setURL(URL);
		cefRequest.setHeaderMap(headers);
		cefRequest.setPostData(cefPostData);
		cefRequest.setFlags(1 << 3 | 1 << 7); // UR_FLAG_ALLOW_STORED_CREDENTIALS | UR_FLAG_STOP_ON_REDIRECT
		cefRequest.setFirstPartyForCookies(settings.getExplainUrl());
		APIClient client = new APIClient(callback);
		CefURLRequest.create(cefRequest, client);
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
		if (isSupported) {
			cefClient.removeLoadHandler();
			cefBrowser.stopLoad();
			cefBrowser.close(false);
			browser.dispose();
		}
	}
}

class APIClient implements CefURLRequestClient {
	private static final Logger log = Logger.getInstance(ExplainBrowser.class);

	public String data = "";
	Consumer<String> callback;

	public APIClient(Consumer<String> callback) {
		this.callback = callback;
	}

	@Override
	public void onRequestComplete(CefURLRequest cefURLRequest) {
		CefResponse response = cefURLRequest.getResponse();
		int responseStatus = response.getStatus();
		if (responseStatus == 200 && data.length() > 0) {
			this.callback.accept(data);
		} else if (responseStatus == 302) {
			this.callback.accept(response.getHeaderByName("Location"));
		} else if (responseStatus == 401) {
			ApplicationManager.getApplication().invokeLater(() -> {
				ExplainAuthDialog explainAuthDialog = new ExplainAuthDialog(callback);
				explainAuthDialog.show();
			});
		}
	}

	@Override
	public void onUploadProgress(CefURLRequest cefURLRequest, int i, int i1) {

	}

	@Override
	public void onDownloadProgress(CefURLRequest cefURLRequest, int i, int i1) {

	}

	@Override
	public void onDownloadData(CefURLRequest cefURLRequest, byte[] bytes, int i) {
		data += new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public boolean getAuthCredentials(boolean b, String s, int i, String s1, String s2, CefAuthCallback cefAuthCallback) {
		return false;
	}

	@Override
	public void setNativeRef(String s, long l) {

	}

	@Override
	public long getNativeRef(String s) {
		return 0;
	}
}

