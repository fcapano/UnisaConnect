package it.fdev.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

/**
 * Fix Receiver not registered: android.widget.ZoomButtonsController
 * http://stackoverflow.com/questions/4908794/webview-throws-receiver-not-registered-android-widget-zoombuttonscontroller
 */
public class MyWebView extends WebView {
	
	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setVisibility(View.VISIBLE);	// Workaround for NullPointerException
		setFocusable(true);				// http://stackoverflow.com/questions/12325720/nullpointerexception-in-webview-java-android-webkit-webviewprivatehandler-hand
		requestFocus();					//
	}
	
	@Override
	public void destroy() {
		getSettings().setBuiltInZoomControls(true);
		super.destroy();
	}
	
}
