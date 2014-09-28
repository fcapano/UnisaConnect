package it.fdev.utils;

import it.fdev.unisaconnect.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExpandablePanel extends LinearLayout {

	private final int mHandleId;
	private final int mTitleId;
	private final int mContent1Id, mContent2Id;

	// Contains references to the handle and content views
	private ImageView mHandle;
	private TextView mTitle;
	private TextView mContent1, mContent2;

	// Does the panel start expanded?
	private boolean mExpanded = false;

	public ExpandablePanel(Context context) {
		this(context, null);
	}

	/**
	 * The constructor simply validates the arguments being passed in and sets the global variables accordingly. Required attributes are 'handle' and 'content'
	 */
	public ExpandablePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandablePanel, 0, 0);

		int handleId = a.getResourceId(R.styleable.ExpandablePanel_handle, 0);
		if (handleId == 0) {
			throw new IllegalArgumentException("The handle attribute is required and must refer " + "to a valid child.");
		}

		int titleId = a.getResourceId(R.styleable.ExpandablePanel_expandable_title, 0);
		if (titleId == 0) {
			throw new IllegalArgumentException("The title attribute is required and must " + "refer to a valid child.");
		}

		int content1Id = a.getResourceId(R.styleable.ExpandablePanel_expandable_content1, 0);
		if (content1Id == 0) {
			throw new IllegalArgumentException("The content1 attribute is required and must " + "refer to a valid child.");
		}

		int content2Id = a.getResourceId(R.styleable.ExpandablePanel_expandable_content2, 0);
		if (content2Id == 0) {
			throw new IllegalArgumentException("The content2 attribute is required and must " + "refer to a valid child.");
		}

		mHandleId = handleId;
		mTitleId = titleId;
		mContent1Id = content1Id;
		mContent2Id = content2Id;

		a.recycle();
	}
	
	public void showHandle() {
		mHandle.setVisibility(View.VISIBLE);
	}
	
	public void hideHandle() {
		mHandle.setVisibility(View.GONE);
	}

	/**
	 * This method gets called when the View is physically visible to the user
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mHandle = (ImageView) findViewById(mHandleId);
		if (mHandle == null) {
			throw new IllegalArgumentException("The handle attribute is must refer to an" + " existing child.");
		}
		mHandle.setImageResource(R.drawable.ic_action_expand);
//		mHandle.setOnClickListener(new PanelToggler());

		mTitle = (TextView) findViewById(mTitleId);
		if (mTitle == null) {
			throw new IllegalArgumentException("The title attribute must refer to an" + " existing child.");
		}

		mContent1 = (TextView) findViewById(mContent1Id);
		if (mContent1 == null) {
			throw new IllegalArgumentException("The content1 attribute must refer to an" + " existing child.");
		}
		mContent1.setVisibility(View.GONE);

		mContent2 = (TextView) findViewById(mContent2Id);
		if (mContent2 == null) {
			throw new IllegalArgumentException("The content2 attribute must refer to an" + " existing child.");
		}
		mContent2.setVisibility(View.GONE);

//		boolean t1Empty = mContent1.getText().toString().isEmpty();
//		boolean t2Empty = mContent2.getText().toString().isEmpty();
//		
//		if (t1Empty && t2Empty) {
//			mHandle.setVisibility(View.GONE);
//		}

		setOnClickListener(new PanelToggler());
	}
	
	/**
	 * This is the on click listener for the handle. It basically just creates a new animation instance and fires animation.
	 */
	private class PanelToggler implements OnClickListener {
		public void onClick(View v) {
			
			boolean t1Empty = mContent1.getText().toString().isEmpty();
			boolean t2Empty = mContent2.getText().toString().isEmpty();
			if (t1Empty && t2Empty) {
				mHandle.setVisibility(View.GONE);
				return;
			}
			
			if (mExpanded) {
//				mContent1.setVisibility(View.GONE);
//				mContent2.setVisibility(View.GONE);
				Utils.collapse(mContent1);
				Utils.collapse(mContent2);
				mHandle.setImageResource(R.drawable.ic_action_expand);
			} else {
//				mContent1.setVisibility(View.VISIBLE);
//				mContent2.setVisibility(View.VISIBLE);
				Utils.expand(mContent1);
				Utils.expand(mContent2);
				mHandle.setImageResource(R.drawable.ic_action_collapse);
			}
			mExpanded = !mExpanded;
		}
	}

}