package it.fdev.utils;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Set;

import it.fdev.unisaconnect.MainActivity;

//import com.google.analytics.tracking.android.EasyTracker;

public abstract class MySimpleFragment extends Fragment implements MyFragmentInterface {
	protected MainActivity mActivity;
	protected Resources resources;

	private boolean firstRun = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		resources = getResources();
		mActivity.setActionbarTitle(getTitleResId());
		mActivity.reloadActionButtons(this);
		try {
//			EasyTracker.getTracker().sendView(this.getClass().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (!firstRun) {
			mActivity.setActionbarTitle(getTitleResId());
			mActivity.reloadActionButtons(this);
		} else {
			firstRun = false;
		}
	}

	@Override
	public void actionRefresh() {
	}

	@Override
	public boolean goBack() {
		return true;
	}

	@Override
	public void actionAdd() {
	}

	@Override
	public void actionEdit() {
	}

	@Override
	public void actionAccept() {
	}
	
	@Override
	public void actionCancel() {
	}

	@Override
	public void actionTwitter() {
	}

	@Override
	public void actionFeed() {
	}

	@Override
	public Set<Integer> getActionsToShow() {
		return null;
	}
	
	@Override
	public int getTitleResId() {
		return -1;
	}
	
	@Override
	public boolean executeSearch(String query) {
		return true;
	}
}