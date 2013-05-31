package it.fdev.utils;

import it.fdev.unisaconnect.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomMapMarker extends RelativeLayout {
	
	public CustomMapMarker(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_map_marker, this, true);
	}
	
	public CustomMapMarker(Context context, int textRes) {
		super(context, null);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_map_marker, this, true);
		TextView textView = (TextView) findViewById(R.id.textview);
		textView.setText(getResources().getString(textRes));
	}
	
	public CustomMapMarker(Context context, String text) {
		super(context, null);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_map_marker, this, true);
		TextView textView = (TextView) findViewById(R.id.textview);
		textView.setText(text);
	}
}