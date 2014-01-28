package it.fdev.unisaconnect.wifilogin;

import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySSLSocketFactory;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class LoginManager extends IntentService {
	
	static {
		HttpURLConnection.setFollowRedirects(false);
	}

	private final static String INTENT_TAG = Utils.TAG + " LoginManager";
	private final static String URL = "http://google.com/";
	private final static String FORM_URL = "https://wlc.unisa.it/login.html?buttonClicked=0&redirect_url=google.com";
	private final static String LOGOUT_URL = "https://wlc.unisa.it/logout.html";
	private final static String REDIRECT_PAGE_PATTERN = "Web Authentication Redirect";
	private final static String LOGIN_SUCCESSFUL_PATTERN = "You can now use all our regular network services over the wireless network";
	private final static String LOGOUT_SUCCESSFUL_PATTERN = "To complete the log off";

	private static final int CONNECTION_TIMEOUT = 4000;
	private static final int SOCKET_TIMEOUT = 4000;
	private static final int RETRY_COUNT = 2;
	
	// Serve per creare il toast
	private final Handler mHandler = new Handler();
	
	private static DefaultHttpClient mHttpClient;
	static {
		mHttpClient  = getNewHttpClient();
	}

	public LoginManager() {
		super(INTENT_TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// Viene lanciato in automatico dall'intent, cioè quando cambia lo stato della wifi
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			int response_connect = login(getApplicationContext());
			if (response_connect == 1) {
				//Update checher
			}
			if (response_connect == 1) {
				createToastNotification(R.string.login_ok, Toast.LENGTH_SHORT);
				Log.v(Utils.TAG, "Login successful");
			}
		} catch (Exception e) {
			// a bug in HttpClient library
			// thrown when there is a connection failure when handling a
			// redirect
			Log.w(Utils.TAG, "Login failed: Exception");
			Log.w(Utils.TAG, e.toString());
		}
	}

	
	/**
	 * Returns: 0 if already logged in / no action taken
	 * 			1 if login successful
	 * 			2 if unknown exception retrieving page
	 * 			3 if internet is not available even though login was supposedly successful
	 * @param context
	 * @return status result/problem
	 */
	protected static int login(Context context) {
		SharedPrefDataManager mDataManager = new SharedPrefDataManager(context);
		if (!mDataManager.loginDataExists()) // Non sono memorizzati i dati utente
			return 3;
		
		try {
			int wificode;
			if ((wificode = isWifiOk(context)) != 1)
				return wificode;
			if (!isLoginRequired()) // Gia si ha l'accesso ad internet
				return 0;
	
			String user = mDataManager.getUser();
			String pass = mDataManager.getPass();
			user += "@studenti.unisa.it";

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("referer", "https://captive.unisa.it/main.htm"));
			formparams.add(new BasicNameValuePair("err_flag", "0"));
			formparams.add(new BasicNameValuePair("username", user));
			formparams.add(new BasicNameValuePair("password", pass));	
			formparams.add(new BasicNameValuePair("buttonClicked", "4"));
			formparams.add(new BasicNameValuePair("redirect_url", ""));
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httppost = new HttpPost(FORM_URL);
			httppost.setEntity(entity);
			HttpResponse response = mHttpClient.execute(httppost);
			Log.v(Utils.TAG, "Post done...checking response");
			String strRes = EntityUtils.toString(response.getEntity());
			
			if (strRes.contains(LOGIN_SUCCESSFUL_PATTERN)) {
				// login successful
				return 1;
			} else {
				return 3;
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "CAUSA FALLIMENTO CONNESSIONE:" + e.getMessage());
			e.printStackTrace();
			return 2;
		}
	}
	
	protected static boolean logout(Context context) {
		if (isWifiOk(context) != 1) //Wifi non collegata  - spenta - altra rete
			return false;
		if(isLoginRequired()) // Non si è loggati
			return true;

		try {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("err_flag", "0"));
			formparams.add(new BasicNameValuePair("buttonClicked", "4"));
			formparams.add(new BasicNameValuePair("redirect_url", ""));
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httppost = new HttpPost(LOGOUT_URL);
			httppost.setEntity(entity);
			HttpResponse response = mHttpClient.execute(httppost);
			String strRes = EntityUtils.toString(response.getEntity());

			if (strRes.contains(LOGOUT_SUCCESSFUL_PATTERN)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "CAUSA FALLIMENTO CONNESSIONE LOGOUT:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// Controlla che il wifi sia acceso e che si sia collegati alla rete dell'università
	private static int isWifiOk(Context context) {
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (wifi != NetworkInfo.State.CONNECTED) {
			return 2;
		}

		// Check SSID
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi==null || wifiMan==null || wifiMan.getConnectionInfo()==null || wifiMan.getConnectionInfo().getSSID()==null || !wifiMan.getConnectionInfo().getSSID().contains(SharedPrefDataManager.SSID_STUDENTI)) {
			return 4;
		}
		return 1;
	}
	
	private static boolean isLoginRequired() {
		try {
			HttpResponse response = mHttpClient.execute(new HttpGet(URL));
			String strRes = EntityUtils.toString(response.getEntity());
			Log.v(Utils.TAG,	"Richiesta controllo login richiesta completata");
			if (strRes.contains(REDIRECT_PAGE_PATTERN)) {
				Log.v(Utils.TAG, "The device is not logged in");
				return true;
			} else {
				return false;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	private void createToastNotification(final int message, final int length) {
		mHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(LoginManager.this, message, length).show();
			}
		});
	}
	
	public static DefaultHttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        params.setParameter("http.protocol.handle-redirects",false);
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
	        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
	        DefaultHttpClient defHttpClient = new DefaultHttpClient(ccm, params);
	        // Also retry POST requests (normally not retried because it is not regarded idempotent)
	        defHttpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
				public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			        if (executionCount >= RETRY_COUNT) {
			            // Do not retry if over max retry count
			            return false;
			        }
			        if (exception instanceof UnknownHostException) {
			            // Unknown host
			            return false;
			        }
			        if (exception instanceof ConnectException) {
			            // Connection refused 
			            return false;
			        }
			        if (exception instanceof SSLHandshakeException) {
			            // SSL handshake exception
			            return false;
			        }
			        return true;
				}
			});
	        return defHttpClient;
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
}