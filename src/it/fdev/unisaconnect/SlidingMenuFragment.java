package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.ListAdapter;
import it.fdev.utils.ListAdapter.ListItem;
import it.fdev.utils.MyListFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

public class SlidingMenuFragment extends MyListFragment {

	private static final int VALID_ELEMENTS_NUM = 7;	// When testing is disabled only these elements are shown
														// To enable testing in the wifipreferences as username enter
													 	// the string in Utils.TOGGLE_TESTING_STRING

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sliding_menu, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String[] menuText = resources.getStringArray(R.array.menu_items);
		TypedArray imgs = resources.obtainTypedArray(R.array.menu_icons);
		ArrayList<ListItem> listItem = new ArrayList<ListItem>();
		for (int i = 0; i < VALID_ELEMENTS_NUM; i++) {
			listItem.add(new ListItem(menuText[i], imgs.getResourceId(i, -1), R.color.menu_background));
		}
		if (SharedPrefDataManager.getDataManager(activity).isTestingingEnabled()) {
			for (int i = VALID_ELEMENTS_NUM; i < menuText.length; i++) {
				listItem.add(new ListItem(menuText[i], imgs.getResourceId(i, -1), true, R.color.menu_background));
			}
		}
		ListAdapter adapter = new ListAdapter(getActivity(), R.layout.row, listItem);
		imgs.recycle();
		setListAdapter(adapter);
		
		View settingsRow = activity.findViewById(R.id.settings_row);
		settingsRow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(new PreferencesFragment());
			}
		});
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = positionToAction((MainActivity) getActivity(), position);
		if (newContent != null) {
			activity.switchContent(newContent);
		}
	}
	
	public static Fragment positionToAction(MainActivity activity, int position) {
		Fragment newContent = null;
		switch (position) {
		case 0:
			newContent = new WifiPreferencesFragment();
			break;
		case 1:
			newContent = new MensaFragment();
			break;
		case 2:
			newContent = new WebmailFragment();
			break;
		case 3:
			newContent = new Esse3ServicesFragment();
			break;
		case 4:
			newContent = new StaffSearchFragment();
			break;
		case 5:
			newContent = new WeatherFragment();
			break;
		case 6:
			Utils.sendSupportMail(activity, "Riguardo \"Unisa Connect\"...", "");
			break;
		case 7:
			newContent = new TimetableFragment();
			break;
		case 8:
			newContent = new MapFragment();
			break;
		}
		return newContent;
	}
	
	@Override
	public void setVisibleActions() {
	}

	@Override
	public void actionRefresh() {
	}

}
