package it.fdev.unisaconnect;

import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@Deprecated
public class MensaOldWebFragment extends MySimpleFragment {
	
	private WebView webView;
	private Fragment thisFragment;
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.web_fragment, container, false);
		return mainView;
	}
		
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		thisFragment = this;
		
		webView = (WebView) view.findViewById(R.id.webview);
		webView.getSettings().setLoadsImagesAutomatically(false);
		webView.setBackgroundColor(resources.getColor(android.R.color.white));
		webView.getSettings().setJavaScriptEnabled(false);
//		webView.clearCache(true);
		webView.setWebViewClient(new WebViewClient() {
	        @Override
	        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
	            // Handle the error
	        	Utils.goToInternetError(activity, thisFragment);
	        }

	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	        }
	    });
		webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                	Utils.dismissDialog();
                }
            }
        }); 
		actionRefresh();
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
		if(!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, thisFragment);
			return;
		}
		Utils.createDialog(activity, getString(R.string.caricamento), false);
		webView.loadUrl("http://www.unisamenu.it/");
	}
	
	@Override
	public void onStop() {
		webView.stopLoading();
		super.onStop();
	}
}
