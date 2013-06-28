package it.fdev.utils;

import it.fdev.unisaconnect.MainActivity;

import java.util.Set;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.google.analytics.tracking.android.EasyTracker;

public abstract class MyListFragment extends ListFragment implements MyFragment {
	protected MainActivity activity;
	protected Resources resources;
	
	private boolean firstRun = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		resources = getResources();
		try {
			EasyTracker.getTracker().sendView(this.getClass().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		activity.reloadActionButtons(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!firstRun) {
			activity.reloadActionButtons(this);
		} else {
			firstRun = false;
		}
	}

	public void actionRefresh() {
	}

	public boolean goBack() {
		return true;
	}

	public void actionAdd() {
	}

	public void actionEdit() {
	}

	public void actionAccept() {
	}

	public Set<Integer> getActionsToShow() {
		return null;
	}
}