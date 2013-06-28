package it.fdev.unisaconnect;

import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slidingmenu.lib.SlidingMenu;
/**
 * Frammento da usare in caso di errore di connessione o internet assente
 * @author francesco
 *
 */
public class ErrorInternetFragment extends MySimpleFragment {
	
	private Fragment backFragment = null;
	private int initialTouchMode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initialTouchMode = activity.getSlidingMenu().getTouchModeAbove();
		return inflater.inflate(R.layout.error_internet_missing, container, false);
	}
	
	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		return actionsToShow;
	}
	
	@Override
	public void actionRefresh() {
		if (!isAdded()) {
			return;
		}
		if(Utils.hasConnection(activity)) {
			activity.getSupportFragmentManager().popBackStack();
			if(backFragment != null) {
				activity.switchContent(backFragment);
			}
		}
	}

	public void setBackFragment(Fragment fragment) {
		backFragment = fragment;
	}
	
	@Override
	public void onResume() {
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		activity.getSlidingMenu().setTouchModeAbove(initialTouchMode);
		super.onPause();
	}
}