package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.slidingmenu.lib.SlidingMenu;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3Fragment extends MySimpleFragment {

	public final static String esse3url = "https://esse3web.unisa.it/unisa/auth/Logon.do";

	private WebView webView;
	private SharedPrefDataManager dataManager;
	private boolean didSendLoginData = false;
	private Fragment thisFragment;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		webView = (WebView) view.findViewById(R.id.webview);

		thisFragment = this;

		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		dataManager = SharedPrefDataManager.getDataManager(activity);
		if (!dataManager.dataExists()) { // Non sono memorizzati i dati utente
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

		webView.setWebViewClient(new WebViewClient() {
			// Quando la pagina richiede l'autenticazione HTTP
			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				if (dataManager.dataExists() && !didSendLoginData) {
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

			// Quando il caricamento si completa rimuovi il dialog
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
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
		if (!isAdded()) {
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
		try {
			webView.stopLoading();
		} catch (Exception e) {
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		try {
			webView.setVisibility(View.GONE); 						// For bug:
			webView.getSettings().setBuiltInZoomControls(false);	// http://stackoverflow.com/questions/5267639/how-to-safely-turn-webview-zooming-on-and-off-as-needed
			
			webView.stopLoading();
			webView.destroy();
		} catch (Exception e) {
		}
		super.onStop();
	}
}
