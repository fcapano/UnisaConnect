package it.fdev.unisaconnect;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.MyWebView;
import it.fdev.utils.Utils;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class FragmentEsse3Web extends MySimpleFragment {

	public final static String esse3LoginUrl = "https://esse3web.unisa.it/unisa/auth/Logon.do";

	private ProgressBar progressBar;
	private MyWebView mWebView;
	private SharedPrefDataManager mDataManager;
	private boolean didSendLoginData = false;
	private Fragment thisFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.web_fragment, container, false);
		return mainView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		progressBar = (ProgressBar) view.findViewById(R.id.progress__bar);
		mWebView = (MyWebView) view.findViewById(R.id.webview);

		thisFragment = this;

		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		mDataManager = new SharedPrefDataManager(mActivity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(mActivity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}

		// Mostro il dialog di caricamento
//		Utils.createDialog(activity, getString(R.string.caricamento), false);
		mActivity.setLoadingVisible(true, false);

		// Cancella i cookie in modo da evitare problemi vari di login
		CookieSyncManager.createInstance(mActivity);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setAcceptCookie(true);

		// On Gingerbread there are problems with the cookies and HTTP-Auth.
		// I make a request, get the session cookie and save it in
		// CookieManager,
		// which is used by the webView
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			Log.d(Utils.TAG, "Executing GB cookie compatibility code");
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(esse3LoginUrl);
			try {
				client.execute(request);
				List<Cookie> cookies = client.getCookieStore().getCookies();
				for (Cookie c : cookies) {
					String cUrl = (c.isSecure() ? "https" : "http") + "://" + c.getDomain() + c.getPath();
					cookieManager.setCookie(cUrl, c.getName() + "=" + c.getValue() + "; Domain=" + c.getDomain());
				}
			} catch (Exception e) {
				Log.d(Utils.TAG, "Error in preliminar load", e);
			}
		}

		// Inizializzo la webview
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setSupportZoom(true);
		webSettings.setJavaScriptEnabled(true);
		
		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different scales.
				// The progress meter will automatically disappear when we reach 100%
//				activity.setProgress(progress * 1000);
				progressBar.setProgress(progress);
			}
		});

		mWebView.setWebViewClient(new WebViewClient() {
			// Quando la pagina richiede l'autenticazione HTTP
			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				if (!isAdded()) {
					return;
				}
				if (mDataManager.loginDataExists() && !didSendLoginData) {
					// Evita un loop di login se i dati sono sbagliati
					didSendLoginData = true;
					Log.d(Utils.TAG, "Inviando dati esse3");
					handler.proceed(mDataManager.getUser(), mDataManager.getPass());
				} else {
					Log.d(Utils.TAG, "MMM");
					Utils.createAlert(mActivity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
				}
			}

			// Si verifica un errore durante il caricamento della pagina
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Utils.goToInternetError(mActivity, thisFragment);
			}

			// Non chiedere quale browser usare. Carica sempre la pagina nella webview
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(View.VISIBLE);
				mActivity.setLoadingVisible(true, false);
			}

			// Quando il caricamento si completa rimuovi il dialog
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				mActivity.setLoadingVisible(false, false);
				didSendLoginData = false;
//				if (!acceptedPrivacy) {
					view.loadUrl("javascript:cookies.set()");
//					acceptedPrivacy = true;
//				}
			}
		});

		mWebView.loadUrl(esse3LoginUrl);
	}

	@Override
	public boolean goBack() {
		// Quando premo il tasto indietro
		if (mWebView.canGoBack()) { 			// Se posso andare indietro nella cronologia della webview...
			mWebView.goBack();
			mWebView.reload();
			return false;
		} else { 							// Altrimenti vado al fragment precedente
			try {
				mWebView.stopLoading();
				mWebView.destroy();
			} catch (Exception e) {
			}
			return super.goBack();
		}
	}
	
	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		return actionsToShow;
	}

	@Override
	public void actionRefresh() {
		try {
			if (!isAdded() || mWebView == null) {
				return;
			}
			if (!Utils.hasConnection(mActivity)) {
				Utils.goToInternetError(mActivity, thisFragment);
				return;
			}
			didSendLoginData = false;
			mWebView.reload();
		} catch (Exception e) {
			// ho avuto java.lang.NullPointerException su webView.reload(); nonostante il controllo nel primo if
			// viene deferenziata giusto tra l'if e il reload? Forse perchè hasConnection() prende un po' di tempo
		}
	}

	@Override
	public void onPause() {
		try {
			mWebView.setVisibility(View.GONE);	// Workaround for nullpointerexception
			mWebView.stopLoading();
		} catch (Exception e) {
		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		try {
			mWebView.setVisibility(View.VISIBLE);	// Workaround for nullpointerexception
			mWebView.reload();
		} catch (Exception e) {
		}
		super.onResume();
	}

	@Override
	public int getTitleResId() {
		return R.string.esse3_pagina_web;
	}
}
