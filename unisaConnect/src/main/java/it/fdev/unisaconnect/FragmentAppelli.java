package it.fdev.unisaconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Appelli;
import it.fdev.unisaconnect.data.Appelli.Appello;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyDateUtils;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

public class FragmentAppelli extends MySimpleFragment {

	private boolean alreadyStarted = false;

	private ArrayList<Appello> listaAppelliDisponibili;
	private ArrayList<Appello> listaAppelliPrenotati;
	private LayoutInflater layoutInflater;
	private SharedPrefDataManager mDataManager;

	private ScrollView appelliListContainer;
	private LinearLayout appelliDisponibiliContainer;
	private LinearLayout appelliPrenotatiContainer;
	private View appelliNDView;
	private TextView lastUpdateTextView;
	private View lastUpdateIconView;

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
		return inflater.inflate(R.layout.fragment_appelli, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		mDataManager = new SharedPrefDataManager(mActivity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(mActivity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}

		mActivity.setLoadingVisible(true, true);

		mIntentFilter.addAction(Esse3ScraperService.BROADCAST_STATE_E3_PAGAMENTI);

		layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		appelliListContainer = (ScrollView) view.findViewById(R.id.appelli_list_container);
		appelliDisponibiliContainer = (LinearLayout) view.findViewById(R.id.appelli_disponibili_list);
		appelliPrenotatiContainer = (LinearLayout) view.findViewById(R.id.appelli_prenotati_list);
		appelliNDView = view.findViewById(R.id.appelli_vuoto);
		lastUpdateTextView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getAppelli(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	@Override
	public void actionRefresh() {
		getAppelli(true);
	}

	public void onNewBroadcast(Context context, Intent intent) {
		try {
			Log.d(Utils.TAG, "BROADCAST RECEIVED: " + intent.getAction());
			if (Esse3ScraperService.BROADCAST_STATE_E3_PAGAMENTI.equals(intent.getAction())) {
				LoadStates state = (LoadStates) intent.getSerializableExtra("status");
				switch (state) {
				case FINISHED:
					mostraAppelli();
					break;
				case NO_DATA:
				case WRONG_DATA:
					Utils.createAlert(mActivity, mActivity.getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
					break;
				case UNKNOWN_PROBLEM:
				default:
					Utils.createAlert(mActivity, mActivity.getString(R.string.problema_di_connessione_generico), null, true);
					break;
				}
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
		}
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted || Esse3ScraperService.isRunning) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}

	public void getAppelli(boolean force) {
		if (!isAdded()) {
			return;
		}

		// Lo scraper è in esecuzione
		if (Esse3ScraperService.isRunning) {
			return;
		}

		mActivity.setLoadingVisible(true, true);

		if (!force && mDataManager.getAppelli() != null) {
			alreadyStarted = true;
			mostraAppelli();
			return;
		}
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}

		if (force || !alreadyStarted) {
			alreadyStarted = true;
			mActivity.startService(new Intent(mActivity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_APPELLI));
		} else {
			appelliNDView.setVisibility(View.VISIBLE);
			appelliDisponibiliContainer.setVisibility(View.GONE);
			appelliPrenotatiContainer.setVisibility(View.GONE);
			lastUpdateTextView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}

	public void mostraAppelli() {
		if (!isAdded()) {
			return;
		}
		if (appelliDisponibiliContainer == null || appelliPrenotatiContainer == null || appelliNDView == null) { // Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true); // Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}

		Appelli appelli = mDataManager.getAppelli();

		String updateText = "";
		if (appelli != null) {
			updateText = MyDateUtils.getLastUpdateString(mActivity, appelli.getFetchTime().getTime(), false);
		}
		if (!updateText.isEmpty()) {
			lastUpdateTextView.setText(updateText);
			lastUpdateTextView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateTextView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		}

		if (appelli == null || appelli.isEmpty()) {
			// Non ho appelli da mostrare
			appelliNDView.setVisibility(View.VISIBLE);
			appelliListContainer.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}

		appelliNDView.setVisibility(View.GONE);
		mostraAppelliDisponibili(appelli.getListaAppelliDisponibili());
		mostraAppelliPrenotati(appelli.getListaAppelliPrenotati());

		mActivity.setLoadingVisible(false, false);
	}

	private void mostraAppelliPrenotati(ArrayList<Appello> listaAppelli) {
		appelliPrenotatiContainer.setVisibility(View.GONE);

		if (listaAppelli != null && listaAppelli.size() > 0) {
			listaAppelliPrenotati = listaAppelli;
		}
		if (listaAppelliPrenotati == null) {
			listaAppelliPrenotati = new ArrayList<Appello>();
		}
		if (listaAppelliPrenotati.size() == 0) {
			if (listaAppelliDisponibili != null && listaAppelliDisponibili.size() == 0) {
				appelliNDView.setVisibility(View.VISIBLE);
			}
			return;
		} else {
			appelliNDView.setVisibility(View.GONE);
		}

		while (appelliPrenotatiContainer.getChildCount() >= 2) {
			appelliPrenotatiContainer.removeViewAt(1);
		}
		for (Appello appello : listaAppelliPrenotati) {
			View rowView = setAppelliRow(appello, layoutInflater);
			appelliPrenotatiContainer.addView(rowView);
		}
		appelliPrenotatiContainer.setVisibility(View.VISIBLE);
	}

	private void mostraAppelliDisponibili(ArrayList<Appello> listaAppelli) {
		appelliDisponibiliContainer.setVisibility(View.GONE);

		if (listaAppelli != null && listaAppelli.size() > 0) {
			listaAppelliDisponibili = listaAppelli;
		}
		if (listaAppelliDisponibili == null) {
			listaAppelliDisponibili = new ArrayList<Appello>();
		}
		if (listaAppelliDisponibili.size() == 0) {
			if (listaAppelliPrenotati != null && listaAppelliPrenotati.size() == 0) {
				appelliNDView.setVisibility(View.VISIBLE);
			}
			return;
		} else {
			appelliNDView.setVisibility(View.GONE);
		}

		while (appelliDisponibiliContainer.getChildCount() >= 2) {
			appelliDisponibiliContainer.removeViewAt(1);
		}
		for (Appello appello : listaAppelliDisponibili) {
			View rowView = setAppelliRow(appello, layoutInflater);
			appelliDisponibiliContainer.addView(rowView);
		}
		appelliDisponibiliContainer.setVisibility(View.VISIBLE);
	}

	private View setAppelliRow(Appello appello, LayoutInflater layoutInflater) {
		View rowView = layoutInflater.inflate(R.layout.appello_row, null);
		TextView nameView = (TextView) rowView.findViewById(R.id.appello_name);
		TextView descriptionView = (TextView) rowView.findViewById(R.id.appello_description);
		TextView dateView = (TextView) rowView.findViewById(R.id.appello_date);
		TextView nSubsView = (TextView) rowView.findViewById(R.id.appello_subscribed_num);
		TextView locationView = (TextView) rowView.findViewById(R.id.appello_location);
		nameView.setText(appello.getName());
		dateView.setText(appello.getDate() + " " + appello.getTime());
		descriptionView.setText(appello.getDescription());
		nSubsView.setText(appello.getSubscribedNum());
		if (appello.getLocation().isEmpty()) {
			locationView.setVisibility(View.GONE);
		} else {
			locationView.setText(appello.getLocation());
		}
		return rowView;
	}

	@Override
	public int getTitleResId() {
		return R.string.appelli;
	}
}
