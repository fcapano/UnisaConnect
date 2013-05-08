package it.fdev.unisaconnect;

import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * Frammento da usare in caso di errore di connessione o internet assente
 * @author francesco
 *
 */
public class ErrorInternetFragment extends MySimpleFragment {
	
	private Fragment backFragment = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.error_internet_missing, container, false);
	}
	
	@Override
	public void setVisibleActions() {
		activity.setActionRefreshVisible(true);
	}
	
	@Override
	public void actionRefresh() {
		if (!isAdded()) {
			return;
		}
		if(Utils.hasConnection(activity)) {
//			activity.goToLastFrame();
			activity.getSupportFragmentManager().popBackStack();
			if(backFragment != null) {
				activity.switchContent(backFragment);
			}
		}
	}

	public void setBackFragment(Fragment fragment) {
		backFragment = fragment;
	}
}