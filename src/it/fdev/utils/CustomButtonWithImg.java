package it.fdev.utils;

import it.fdev.unisaconnect.R;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomButtonWithImg extends RelativeLayout {

	public CustomButtonWithImg(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomButtonWithImg, 0, 0);
		Drawable img = a.getDrawable(R.styleable.CustomButtonWithImg_img);
		String text = a.getString(R.styleable.CustomButtonWithImg_text);
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_button_with_img, this, true);

		ImageView imgView = (ImageView) findViewById(R.id.btn_icon);
		TextView textView = (TextView) findViewById(R.id.btn_text);

		imgView.setImageDrawable(img);
		textView.setText(text.toUpperCase(Locale.ITALY));
//		textView.setText(text);
	}
	
	public CustomButtonWithImg(Context context, int iconID, int textID) {
		super(context, null);
		
		Drawable img = context.getResources().getDrawable(iconID);
		String text = context.getResources().getString(textID);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.custom_button_with_img, this, true);

		ImageView imgView = (ImageView) findViewById(R.id.btn_icon);
		TextView textView = (TextView) findViewById(R.id.btn_text);
		
		imgView.setImageDrawable(img);
		textView.setText(text.toUpperCase(Locale.ITALY));
//		textView.setText(text);
	}

	public CustomButtonWithImg(Context context) {
		this(context, null);
	}
}