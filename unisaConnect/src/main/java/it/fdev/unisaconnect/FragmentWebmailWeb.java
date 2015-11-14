package it.fdev.unisaconnect;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;

import java.util.HashSet;
import java.util.Set;

import it.fdev.mailSync.MailChecker;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.MyWebView;
import it.fdev.utils.Utils;

@SuppressLint("SetJavaScriptEnabled")
public class FragmentWebmailWeb extends MySimpleFragment {

	private final String URL_CMD_IDENTIFIER = "UNISA_CONNECT-CMD-ID";
	private final String URL_CMD_DELIMITER = "||^$||";
	private static final String URL_STRING = "https://webmail.studenti.unisa.it/";

	private SharedPrefDataManager mDataManager;
	private ProgressBar progressBar;
	private MyWebView mWebView;
	private Fragment thisFragment;
	private CheckBox checkMailCheckbox;

	private JavascriptBridge jsBridge = new JavascriptBridge();

    private boolean isPaused = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_webmail_web, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		checkMailCheckbox = (CheckBox) view.findViewById(R.id.check_mail_option);
		progressBar = (ProgressBar) view.findViewById(R.id.progress__bar);
		mWebView = (MyWebView) view.findViewById(R.id.webview);
		mWebView.setVisibility(View.VISIBLE);		// Workaround for nullpointerexception
		mWebView.setFocusable(true);				// http://stackoverflow.com/questions/12325720/nullpointerexception-in-webview-java-android-webkit-webviewprivatehandler-hand
		mWebView.requestFocus();					//
		thisFragment = this;

		mDataManager = new SharedPrefDataManager(mActivity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(mActivity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}

		checkMailCheckbox.setChecked(mDataManager.getMailDoCheck());
		checkMailCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mDataManager.setMailDoCheck(isChecked);
				MailChecker.autoSetAlarm(mActivity);
			}
		});

		CookieSyncManager.createInstance(mActivity);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setAcceptCookie(true);
		startWebView();
		return;
	}

	@SuppressWarnings("deprecation")
	private void startWebView() {
		if(!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, thisFragment);
			return;
		}

		mWebView.setBackgroundColor(resources.getColor(android.R.color.white));
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setSavePassword(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		mWebView.addJavascriptInterface(jsBridge, "UnisaConnectInterface");

//		mWebView.setDownloadListener(new DownloadListener() {
//			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setData(Uri.parse(url));
//				startActivity(i);
//			}
//		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different scales.
				// The progress meter will automatically disappear when we reach 100%
				progressBar.setProgress(progress);
			}
		});

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onFormResubmission(WebView view, Message dontResend, Message resend)
			{
			  resend.sendToTarget();
			}
	        @Override
	        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	        	Utils.goToInternetError(mActivity, thisFragment);
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
			}
	    });
		mWebView.loadUrl(URL_STRING);
		mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    if (isPaused) {
                        return;
                    }
                	if (!URL_STRING.equals(view.getUrl())) {
						return;
					}
        			if (mDataManager.loginDataExists()) { // Sono memorizzati i dati utente
	        			String user = mDataManager.getUser();
	        			user += "@studenti.unisa.it";
	        			String pass = mDataManager.getPass();
	        	    	mWebView.loadUrl(
        	    			"javascript: { " +
	        	    			"if( document.getElementById('username') != null && " +
	        	    			    "document.getElementById('password') != null && " +
	        	    			    "document.getElementById('client')   != null && " +
	        	    			    "document.getElementsByName('loginForm') != null ) { " +
	        	    			    	"document.getElementById('username').value = '"+user+"'; " +
	        			                "document.getElementById('password').value = '"+pass+"'; " +
	        			                "document.getElementById('client').value = 'mobile'; " +
	        			                "document.getElementsByName('loginForm')[0].submit(); " +
    	                		"} " +
        	    			"}; "
	        	    	);
					}

                }
            }
        });
	}

	public class JavascriptBridge {
		@JavascriptInterface
        public void dismissDialog(){
			if (!isAdded()) {
				return;
			}
			mActivity.runOnUiThread(new Runnable() {
			    public void run() {
			    	progressBar.setVisibility(View.GONE);
			    }
		    });
        }
		@JavascriptInterface
        public void wrongDataDialog(){
			if (!isAdded()) {
				return;
			}
			mActivity.runOnUiThread(new Runnable() {
			    public void run() {
			    	progressBar.setVisibility(View.GONE);
					try {
						Utils.createAlert(mActivity, getString(R.string.dati_errati), null, false);
					} catch(Exception e) {
						Log.w(Utils.TAG, "Exception in JavascriptBridge wrongDataDialog");
						e.printStackTrace();
					}
			    }
		    });
        }
    }

	@Override
	public boolean goBack() {
		if(mWebView.canGoBack()) {
	        mWebView.goBack();
	        return false;
		} else
			return super.goBack();
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		return actionsToShow;
	}

	@Override
	public void actionRefresh() {
		if (!isAdded() || mWebView == null) {
			return;
		}
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, thisFragment);
			return;
		}
//		mWebView.reload();
	}

	@Override
	public void onPause() {
		try {
            isPaused = true;
			mWebView.setVisibility(View.GONE);	// Workaround for nullpointerexception
			mWebView.stopLoading();
		} catch (Exception e) {
			// Ignore
		} finally {
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		try {
            isPaused = false;
			mWebView.setVisibility(View.VISIBLE);
			mWebView.reload();
		} catch (Exception e) {
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		try {
			mWebView.setVisibility(View.GONE);	// Workaround for nullpointerexception
			mWebView.stopLoading();
			mWebView.destroy();
		} catch (Exception e) {
		}
		super.onStop();
	}

	@Override
	public int getTitleResId() {
		return R.string.webmail;
	}
}
