package it.fdev.unisaconnect;

import it.fdev.backgroundSync.MailChecker;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.MyWebView;
import it.fdev.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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

@SuppressLint("SetJavaScriptEnabled")
public class FragmentWebmail extends MySimpleFragment {
	
	private final String URL_CMD_IDENTIFIER = "UNISA_CONNECT-CMD-ID";
	private final String URL_CMD_DELIMITER = "||^$||";
	private static final String URL_STRING = "https://webmail.studenti.unisa.it/";

	private SharedPrefDataManager mDataManager;
	private ProgressBar progressBar;
	private MyWebView mWebView;
	private Fragment thisFragment;
	private CheckBox checkMailCheckbox;
	
	private boolean javascriptInterfaceBroken = true;
	private JavascriptBridge jsBridge = new JavascriptBridge();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_webmail, container, false);
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
		
		// http://www.jasonshah.com/handling-android-2-3-webviews-broken-addjavascriptinterface/
		// Determine if JavaScript interface is broken.
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			javascriptInterfaceBroken = true;
		} else {
			javascriptInterfaceBroken = false;
			mWebView.addJavascriptInterface(jsBridge, "UnisaConnectInterface");
		}
		
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
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url == null || url.length() == 0)
					return false;

				if (javascriptInterfaceBroken) {
					if (url.contains(URL_CMD_IDENTIFIER)) {
						// Parse out the function and its parameter from the
//						String[] spl = url.split("\\|\\|\\^\\$\\|\\|");
						String[] spl = url.split(Pattern.quote(URL_CMD_DELIMITER));
						String function = spl[1];
						// Now, invoke the local function with reflection
						try {
							ArrayList<String> params = new ArrayList<String>();
							for (int i = 2; i < spl.length; i++) {
								params.add(spl[i]);
							}
							Method sMethod = null;
							if (params.size() == 0) {
								sMethod = JavascriptBridge.class.getMethod(function);
							} else {
								sMethod = JavascriptBridge.class.getMethod(function, new Class[] { String.class });
							}
							if (params.size() == 0) {
								sMethod.invoke(jsBridge);
							} else if (params.size() == 1) {
								sMethod.invoke(jsBridge, params.get(0));
							} else if (params.size() == 2) {
								sMethod.invoke(jsBridge, params.get(0), params.get(1));
							} else if (params.size() == 3) {
								sMethod.invoke(jsBridge, params.get(0), params.get(1), params.get(2));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}
					return false;
				}

				if (!(url.startsWith("https://") || url.startsWith("http://")))
					return false;
				int doubleslash = url.indexOf("//");
				if (doubleslash == -1)
					doubleslash = 0;
				else
					doubleslash += 2;
				int end = url.indexOf('/', doubleslash);
				end = end >= 0 ? end : url.length();
				String domain = url.substring(doubleslash, end);
				if (domain.contains("webmail") && domain.contains("unisa.it")) {
					view.loadUrl(url);
					return true;
				} else {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					return true;
				}
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
				// If running on 2.3, send javascript to the WebView to handle
				// the functions we used to use in the Javascript-to-Java bridge.
				if (javascriptInterfaceBroken) {
					String handleGingerbreadStupidity = "javascript:function wrongDataDialog() 	{ window.location='http://"+URL_CMD_IDENTIFIER+URL_CMD_DELIMITER+"wrongDataDialog'; }; " + 
														"function dismissDialog()	{ window.location='http://"+URL_CMD_IDENTIFIER+URL_CMD_DELIMITER+"dismissDialog'; }; " + 
														"function goBack()			{ window.location='http://"+URL_CMD_IDENTIFIER+URL_CMD_DELIMITER+"goBack'; }; " + 
														"function UC() { 				this.wrongDataDialog=wrongDataDialog;" +
														"								this.dismissDialog=dismissDialog;" +
														"								this.goBack=goBack;" +
														"}; " +
														"var UnisaConnectInterface = new UC(); ";
					view.loadUrl(handleGingerbreadStupidity);
				}
				progressBar.setVisibility(View.GONE);
				mActivity.setLoadingVisible(false, false);
			}
	    });
		mWebView.loadUrl(URL_STRING);
		mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
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
		mWebView.reload();
	}
	
	@Override
	public void onPause() {
		try {
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
