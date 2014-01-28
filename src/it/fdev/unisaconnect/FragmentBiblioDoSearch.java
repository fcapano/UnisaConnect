package it.fdev.unisaconnect;

import it.fdev.scraper.BiblioSearchScraperService;
import it.fdev.unisaconnect.data.Book;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentBiblioDoSearch extends MySimpleFragment {

	private String mSearchURL;

	private IntentFilter mIntentFilter = new IntentFilter();
	private final BroadcastReceiver mHandlerBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onNewBroadcast(context, intent);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_staff_details, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mSearchURL == null) {
			return;
		}
		mIntentFilter.addAction(BiblioSearchScraperService.BROADCAST_STATE_BIBLIO_SEARCH);
		activity.startService(new Intent(activity, BiblioSearchScraperService.class).putExtra("URL", mSearchURL));
		return;
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.registerReceiver(mHandlerBroadcast, mIntentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterReceiver(mHandlerBroadcast);
	}

	public void onNewBroadcast(Context context, Intent intent) {
		try {
			if (BiblioSearchScraperService.BROADCAST_STATE_BIBLIO_SEARCH.equals(intent.getAction())) {
				ArrayList<Book> list = intent.getParcelableArrayListExtra("status");
				if (list == null || list.size() == 0) {
					
				} else {
					showData(list);
				}
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
		}
	}

	public void showData(ArrayList<Book> list) {
		if (!isAdded()) {
			return;
		}
		
	}

	public void setURL(String url) {
		this.mSearchURL = url;
	}

	@Override
	public int getTitleResId() {
		return R.string.rubrica;
	}

}
