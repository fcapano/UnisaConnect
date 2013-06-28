package it.fdev.utils;

import java.util.Set;


public interface MyFragment {
//	public void setVisibleActions();
	public void actionRefresh();
	public boolean goBack();
	public void actionAdd();
	public void actionEdit();
	public void actionAccept();
	public Set<Integer> getActionsToShow();
}
