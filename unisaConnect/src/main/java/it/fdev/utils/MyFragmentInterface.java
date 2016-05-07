package it.fdev.utils;

import java.util.Set;


public interface MyFragmentInterface {
//	public void setVisibleActions();
	public void actionRefresh();
	public boolean goBack();
	public void actionAdd();
	public void actionEdit();
	public void actionAccept();
	public void actionCancel();
	public void actionTwitter();
	public void actionFeed();
	public Set<Integer> getActionsToShow();
	public int getTitleResId();
	public boolean executeSearch(String query);
}
