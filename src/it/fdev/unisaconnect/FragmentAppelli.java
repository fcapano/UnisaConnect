package it.fdev.unisaconnect;

import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Appelli;
import it.fdev.unisaconnect.data.Appelli.Appello;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
import android.widget.TextView;

public class FragmentAppelli extends MySimpleFragment {

	private boolean alreadyStarted = false;
	
	private ArrayList<Appello> listaAppelliDisponibili;
	private ArrayList<Appello> listaAppelliPrenotati;
	private LayoutInflater layoutInflater;
	private SharedPrefDataManager mDataManager;
	
	private LinearLayout appelliDisponibiliContainer;
	private LinearLayout appelliPrenotatiContainer;
	private View appelliNDView;
	private TextView lastUpdateView;
	private View lastUpdateIconView;
//	private View lastUpdateSepView;

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
		mDataManager = new SharedPrefDataManager(activity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}
		
		activity.setLoadingVisible(true, true);
		
		mIntentFilter.addAction(Esse3ScraperService.BROADCAST_STATE_E3_PAGAMENTI);

		layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		appelliDisponibiliContainer = (LinearLayout) view.findViewById(R.id.appelli_disponibili_list);
		appelliPrenotatiContainer = (LinearLayout) view.findViewById(R.id.appelli_prenotati_list);
		appelliNDView = view.findViewById(R.id.appelli_vuoto);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
//		lastUpdateSepView = (View) view.findViewById(R.id.last_update_sep);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		activity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getAppelli(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterReceiver(mHandlerBroadcast);
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
					Utils.createAlert(activity, activity.getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
					break;
				case UNKNOWN_PROBLEM:
				default:
					Utils.createAlert(activity, activity.getString(R.string.problema_di_connessione_generico), null, true);
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
		
		activity.setLoadingVisible(true, true);
		
		if (!force && mDataManager.getAppelli()!=null) {
			alreadyStarted = true;
			mostraAppelli();
			return;
		}
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
		
		if (force || !alreadyStarted) {
			alreadyStarted = true;
			activity.startService(new Intent(activity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_APPELLI));
		} else {
			appelliNDView.setVisibility(View.VISIBLE);
			appelliDisponibiliContainer.setVisibility(View.GONE);
			appelliPrenotatiContainer.setVisibility(View.GONE);
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
			activity.setLoadingVisible(false, false);
			return;
		}
	}

	public void mostraAppelli() {
		if (!isAdded()) {
			return;			
		}
		if (appelliDisponibiliContainer == null || appelliPrenotatiContainer == null || appelliNDView == null) { 	// Dai report di crash sembra succedere a volte, non ho idea del perchè
			activity.setDrawerOpen(true);							   			// Quindi mostro lo slidingmenu per apparare
			activity.setLoadingVisible(false, false);
			return;
		}
		
		Appelli appelli = mDataManager.getAppelli();
		
		if (appelli == null || appelli.isEmpty()) {
			// Non ho appelli da mostrare
			appelliNDView.setVisibility(View.VISIBLE);
			appelliDisponibiliContainer.setVisibility(View.GONE);
			appelliPrenotatiContainer.setVisibility(View.GONE);
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
//			lastUpdateSepView.setVisibility(View.GONE);
			activity.setLoadingVisible(false, false);
			return;
		} 
		
		appelliNDView.setVisibility(View.GONE);
		if (appelli.getFetchTime().getTime() > 0) {
			String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(appelli.getFetchTime());
		    String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(appelli.getFetchTime());
		    String updateText = getString(R.string.aggiornato_il_alle, dateFirstPart, dateSecondPart);
			lastUpdateView.setText(updateText);
			lastUpdateView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
//			lastUpdateSepView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
//			lastUpdateSepView.setVisibility(View.GONE);
		}
		
		mostraAppelliDisponibili(appelli.getListaAppelliDisponibili());
		mostraAppelliPrenotati(appelli.getListaAppelliPrenotati());
		
		activity.setLoadingVisible(false, false);
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
				Utils.dismissDialog();
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
				Utils.dismissDialog();
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
