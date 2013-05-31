package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

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

import com.slidingmenu.lib.SlidingMenu;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3WebFragment extends MySimpleFragment {

	public final static String esse3url = "https://esse3web.unisa.it/unisa/auth/Logon.do";

	private ProgressBar progressBar;
	private WebView webView;
	private SharedPrefDataManager dataManager;
	private boolean didSendLoginData = false;
	private Fragment thisFragment;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		progressBar = (ProgressBar) view.findViewById(R.id.progress__bar);
		webView = (WebView) view.findViewById(R.id.webview);
		webView.setVisibility(View.VISIBLE);	// Workaround for nullpointerexception
		webView.setFocusable(true);				// http://stackoverflow.com/questions/12325720/nullpointerexception-in-webview-java-android-webkit-webviewprivatehandler-hand
		webView.requestFocus();					//

		thisFragment = this;

		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		dataManager = SharedPrefDataManager.getDataManager(activity);
		if (!dataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}

		// Mostro il dialog di caricamento
		Utils.createDialog(activity, getString(R.string.caricamento), false);

		// Cancella i cookie in modo da evitare problemi vari (si verificavano
		// ma non ricordo quali)
		CookieSyncManager.createInstance(activity);
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
			HttpGet request = new HttpGet(esse3url);
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
		WebSettings webSettings = webView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setSupportZoom(true);
		webSettings.setJavaScriptEnabled(false);
		
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different scales.
				// The progress meter will automatically disappear when we reach 100%
//				activity.setProgress(progress * 1000);
				progressBar.setProgress(progress);
			}
		});

		webView.setWebViewClient(new WebViewClient() {
			// Quando la pagina richiede l'autenticazione HTTP
			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				if (dataManager.loginDataExists() && !didSendLoginData) {
					// Evita un loop di login se i dati sono sbagliati
					didSendLoginData = true;
					Log.d(Utils.TAG, "Inviando dati esse3");
					handler.proceed(dataManager.getUser(), dataManager.getPass());
				} else {
					Utils.createAlert(activity, getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
					Utils.dismissDialog();
				}
			}

			// Si verifica un errore durante il caricamento della pagina
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Utils.goToInternetError(activity, thisFragment);
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
			}

			// Quando il caricamento si completa rimuovi il dialog
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				Utils.dismissDialog();
				didSendLoginData = false;
			}
		});

		webView.loadUrl(esse3url);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.web_fragment, container, false);
		return mainView;
	}

	@Override
	public boolean goBack() {
		// Quando premo il tasto indietro
		if (webView.canGoBack()) { // Se posso andare indietro nella cronologia della webview...
			Utils.createDialog(activity, getString(R.string.caricamento), false);
			webView.goBack();
			webView.reload();
			return false;
		} else { // Altrimenti vado al fragment precedente
			try {
				webView.stopLoading();
				webView.destroy();
			} catch (Exception e) {
			}
			return super.goBack();
		}
	}
	
	@Override
	public void setVisibleActions() {
		activity.setActionRefreshVisible(true);
	}

	@Override
	public void actionRefresh() {
		if (!isAdded() || webView == null) {
			return;
		}
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, thisFragment);
			return;
		}
		didSendLoginData = false;
		Utils.createDialog(activity, getString(R.string.caricamento), false);
		webView.reload();
	}

	@Override
	public void onPause() {
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		try {
			webView.setVisibility(View.GONE);	// Workaround for nullpointerexception
			webView.stopLoading();
		} catch (Exception e) {
		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		try {
			webView.setVisibility(View.VISIBLE);	// Workaround for nullpointerexception
		} catch (Exception e) {
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		try {
			webView.setVisibility(View.GONE);	// Workaround for nullpointerexception
			webView.stopLoading();
			webView.destroy();
		} catch (Exception e) {
		}
		super.onStop();
	}
}
