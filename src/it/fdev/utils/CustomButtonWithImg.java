package it.fdev.utils;

import java.util.Locale;

import it.fdev.unisaconnect.R;
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
		inflater.inflate(R.layout.custom_button, this, true);

		ImageView imgView = (ImageView) findViewById(R.id.btn_icon);
		TextView textView = (TextView) findViewById(R.id.btn_text);

		imgView.setImageDrawable(img);
		textView.setText(text.toUpperCase(Locale.ITALY));
	}

	public CustomButtonWithImg(Context context) {
		this(context, null);
	}
}