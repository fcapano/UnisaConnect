package it.fdev.unisaconnect;

import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.ListAdapter;
import it.fdev.utils.ListAdapter.ListItem;
import it.fdev.utils.MyListFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.content.res.TypedArray;
import android.os.Bundle;
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
		
		View settingsRow = view.findViewById(R.id.settings_row);
		settingsRow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.PREFERENCES, true);
			}
		});
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		BootableFragmentsEnum newContent = positionToAction((MainActivity) getActivity(), position);
		if (newContent != null) {
			activity.switchContent(newContent, true);
		}
	}
	
	public static BootableFragmentsEnum positionToAction(MainActivity activity, int position) {
		BootableFragmentsEnum newContent = null;
		switch (position) {
		case 0:
			newContent = BootableFragmentsEnum.WIFI_PREF;
			break;
		case 1:
			newContent = BootableFragmentsEnum.MENSA;
			break;
		case 2:
			newContent = BootableFragmentsEnum.WEBMAIL;
			break;
		case 3:
			newContent = BootableFragmentsEnum.ESSE3_SERVICES;
			break;
		case 4:
			newContent = BootableFragmentsEnum.STAFF_SEARCH;
			break;
		case 5:
			newContent = BootableFragmentsEnum.WEATHER;
			break;
		case 6:
			Utils.sendSupportMail(activity, "Riguardo \"Unisa Connect\"...", "");
			break;
		case 7:
			newContent = BootableFragmentsEnum.TIMETABLE;
			break;
		case 8:
			newContent = BootableFragmentsEnum.MAP;
			break;
		}
		return newContent;
	}
	
}
