package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class WebmailFragment extends MySimpleFragment {
	
	private WebView webView;
	private Fragment thisFragment;
	
	private boolean javascriptInterfaceBroken = true;
	private JavascriptBridge jsBridge = new JavascriptBridge();
	private String bDelim = "||^$||";
	
	private static final String URL_STRING = "https://webmail.studenti.unisa.it/";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.web_fragment, container, false);
		return mainView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		webView = (WebView) view.findViewById(R.id.webview);
		thisFragment = this;
		
		SharedPrefDataManager dataManager = SharedPrefDataManager.getDataManager(activity);
		if (!dataManager.dataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
			return;
		}

		if(!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, thisFragment);
			return;
		}
		
		Utils.createDialog(activity, getString(R.string.caricamento), false);
		
		webView.setBackgroundColor(resources.getColor(android.R.color.white));
		WebSettings webSettings = webView.getSettings();
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
			webView.addJavascriptInterface(jsBridge, "UnisaConnectInterface");
		}
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onFormResubmission(WebView view, Message dontResend, Message resend)
			{
			  resend.sendToTarget();
			}
	        @Override
	        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	        	Utils.goToInternetError(activity, thisFragment);
	        }
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	if(url == null || url.length() == 0)
	                return false;
	        	
	        	if (javascriptInterfaceBroken) {
					if (url.contains("UnisaConnect")) {
						// Parse out the function and its parameter from the
						String[] spl = url.split("\\|\\|\\^\\$\\|\\|");
						String function = spl[1];
						Log.d(Utils.TAG, "Method: " + function);
						// Now, invoke the local function with reflection
						try {
							ArrayList<String> params = new ArrayList<String>();
							for (int i=2; i<spl.length; i++) {
								params.add(spl[i]);
								Log.d(Utils.TAG, "Param: " + spl[i]);
							}
							Method sMethod = null;
							if(params.size() == 0) {
								sMethod = JavascriptBridge.class.getMethod(function);
							} else {
								sMethod = JavascriptBridge.class.getMethod(function, new Class[] { String.class });
							}
							if(params.size() == 0) {
								sMethod.invoke(jsBridge);
							} else if(params.size() == 1) {
								sMethod.invoke(jsBridge, params.get(0));
							} else if(params.size() == 2) {
								sMethod.invoke(jsBridge, params.get(0), params.get(1));
							} else if(params.size() == 3) {
								sMethod.invoke(jsBridge, params.get(0), params.get(1), params.get(2));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return true;
				}
	        	
	        	if(! (url.startsWith("https://") || url.startsWith("http://")) )
	        		return false;
	        	int doubleslash = url.indexOf("//");
	            if(doubleslash == -1)
	                doubleslash = 0;
	            else
	                doubleslash += 2;
	            int end = url.indexOf('/', doubleslash);
	            end = end >= 0 ? end : url.length();
	            String domain = url.substring(doubleslash, end);
	            if(domain.contains("webmail") && domain.contains("unisa.it")) {
	            	view.loadUrl(url);
	            	return true;
	            } else {
	            	Uri uri = Uri.parse(url);
	            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	            	startActivity(intent);
	            	return true;
	            }
	        }
	        
	     // Quando il caricamento si completa rimuovi il dialog
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			// If running on 2.3, send javascript to the WebView to handle
			// the functions we used to use in the Javascript-to-Java bridge.
			if (javascriptInterfaceBroken) {
				String handleGingerbreadStupidity = "javascript:function wrongDataDialog() 	{ window.location='http://UnisaConnect"+bDelim+"wrongDataDialog'; }; " + 
													"javascript:function dismissDialog()	{ window.location='http://UnisaConnect"+bDelim+"dismissDialog'; }; " + 
													"javascript:function goBack()			{ window.location='http://UnisaConnect"+bDelim+"goBack'; }; " + 
													"javascript: function UC() { 	this.wrongDataDialog=wrongDataDialog;" +
													"								this.dismissDialog=dismissDialog;" +
													"								this.goBack=goBack;" +
													"}; " +
													"javascript: var UnisaConnectInterface = new UC(); ";
				view.loadUrl(handleGingerbreadStupidity);
			}
		}
	    });
		webView.loadUrl(URL_STRING);
		webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                	SharedPrefDataManager dataManager = SharedPrefDataManager.getDataManager(activity);
        			if (dataManager.dataExists()) { // Non sono memorizzati i dati utente
	        			String user = dataManager.getUser();
	        			user += dataManager.getTipoAccountIndex() == 0 ? "@studenti.unisa.it" : "";
	        			String pass = dataManager.getPass();
	        	    	webView.loadUrl(
        	    			"javascript: { " +
        	    				"if( document.getElementById('ZLoginErrorPanel') != null ) { " +
        	    					"UnisaConnectInterface.wrongDataDialog(); " +
        	    				"} else { " +
		        	    			"if( document.getElementById('username') != null && " +
		        	    			    "document.getElementById('password') != null && " +
		        	    			    "document.getElementById('client')   != null && " +
		        	    			    "document.getElementsByName('loginForm') != null ) { " +
		        	    			    	"document.getElementById('username').value = '"+user+"'; " +
		        			                "document.getElementById('password').value = '"+pass+"'; " +
		        			                "document.getElementById('client').value = 'mobile'; " +
		        			                "document.getElementsByName('loginForm')[0].submit(); " +
		        	                "} else { " +
        	                			"UnisaConnectInterface.dismissDialog(); " +
        	                		"} " +
	        	                "} " +
        	    			"}; "
	        	    	);
        			}
                	
                }
            }
        });
		return;
	}
	
	public class JavascriptBridge {
		@JavascriptInterface
        public void dismissDialog(){
			if (!isAdded()) {
				return;
			}
			Utils.dismissDialog();
        }
		@JavascriptInterface
        public void wrongDataDialog(){
			if (!isAdded()) {
				return;
			}
			try {
				Utils.createAlert(activity, getString(R.string.dati_errati), null, false);
				dismissDialog();
			} catch(Exception e) {
				Log.w(Utils.TAG, "Exception in JavascriptBridge wrongDataDialog");
				e.printStackTrace();
			}
        }
    }
	
	@Override
	public boolean goBack() {
		if(webView.canGoBack()) {
			Utils.createDialog(activity, getString(R.string.caricamento), false);
	        webView.goBack();
	        webView.reload();
	        return false;
		} else
			return super.goBack();
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
		try {
			webView.stopLoading();
			webView.destroy();
		} catch (Exception e) {
		}
		super.onStop();
	}
}
