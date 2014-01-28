package it.fdev.utils;

import it.fdev.unisaconnect.R;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


public class ExpandableTextView extends TextView {
    private static final int DEFAULT_MAX_LINES= 5;
 
    private CharSequence text;
    private BufferType bufferType;
    private boolean trim = true;
    private int maxLinesNum;
 
    public ExpandableTextView(Context context) {
        this(context, null);
    }
 
    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
 
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        this.maxLinesNum = typedArray.getInt(R.styleable.ExpandableTextView_maxLinesNum, DEFAULT_MAX_LINES);
        typedArray.recycle();
        
        this.setMaxLines(maxLinesNum);
 
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trim = !trim;
                toggleMaxLines();
                requestFocusFromTouch();
            }
        });
    }
 
	private void setText() {
    	super.setText(text, bufferType);
    }
    
	@SuppressLint("NewApi")
    private void toggleMaxLines() {
    	if (android.os.Build.VERSION.SDK_INT >= 11) {
    		ObjectAnimator animation = ObjectAnimator.ofInt(this, "maxLines", this.getLineCount(), trim ? maxLinesNum : 50);
        	animation.setDuration(500);
        	animation.start();
		} else {
			this.setMaxLines(trim ? maxLinesNum : 50);
		}
    }
 
    @Override
    public void setText(CharSequence text, BufferType type) {
        this.text = text;
        bufferType = type;
        setText();
    }
 
}