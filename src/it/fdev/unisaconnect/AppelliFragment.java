package it.fdev.unisaconnect;

import it.fdev.scrapers.Esse3AppelliScraper;
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppelliFragment extends MySimpleFragment {

	private boolean alreadyStarted = false;
//	private Esse3AppelliDisponibiliScraper appelliDisponibiliScraper;
//	private Esse3AppelliPrenotatiScraper appelliPrenotatiScraper;
	private Esse3AppelliScraper appelliScraper;
	private ArrayList<Appello> listaAppelliDisponibili;
	private ArrayList<Appello> listaAppelliPrenotati;
	private LinearLayout appelliDisponibiliContainer;
	private LinearLayout appelliPrenotatiContainer;
	private View appelliNDView;
	private TextView lastUpdateView;
	private View lastUpdateIconView;
	private View lastUpdateSepView;

	private LayoutInflater layoutInflater;
	private SharedPrefDataManager pref;
	private Appelli appelli;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.appelli, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		pref = SharedPrefDataManager.getDataManager(activity);
		if (!pref.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), BootableFragmentsEnum.WIFI_PREF, false);
			return;
		}

		layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		appelliDisponibiliContainer = (LinearLayout) view.findViewById(R.id.appelli_disponibili_list);
		appelliPrenotatiContainer = (LinearLayout) view.findViewById(R.id.appelli_prenotati_list);
		appelliNDView = view.findViewById(R.id.appelli_vuoto);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
		lastUpdateSepView = (View) view.findViewById(R.id.last_update_sep);
		
		appelli = pref.getAppelli();
		if (appelli != null) {
			Log.d(Utils.TAG, "Appelli salvati trovati!");
		}

		getAppelli(false);
	}

	@Override
	public void actionRefresh() {
		getAppelli(true);
	}
	
	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}

//	@Override
//	public void onPause() {
//		try {
//			if (appelliDisponibiliScraper != null && appelliDisponibiliScraper.isRunning) {
//				appelliDisponibiliScraper.cancel(true);
//			}
//			if (appelliPrenotatiScraper != null && appelliPrenotatiScraper.isRunning) {
//				appelliPrenotatiScraper.cancel(true);
//			}
//		} catch (Exception e) {
//			Log.d(Utils.TAG, "Error onPause AppelliFragment", e);
//		}
//		super.onPause();
//	}
	
	public void getAppelli(boolean force) {
		if (!isAdded()) {
			return;
		}
		
		activity.setLoadingVisible(true, true);
		
		if (!force && appelli != null) {
			mostraAppelli(null);
			activity.setLoadingVisible(false, false);
			return;
		}
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
//		if (force || !alreadyStarted) {
			alreadyStarted = true;
			if (appelliScraper != null && appelliScraper.isRunning) {
				activity.setLoadingVisible(true);
				return;
			}
			appelliScraper = new Esse3AppelliScraper();
			appelliScraper.setCallerAppelliFragment(this);
			appelliScraper.execute(activity);
			return;
//		}
//		mostraPresenze(null);
	}

//	public void mostraAppelli(boolean force) {
//		// Se non c'è internet rimando al fragment di errore
//		if (!Utils.hasConnection(activity)) {
//			Utils.goToInternetError(activity, this);
//			return;
//		}
//
//		if (force) {
//			alreadyStarted = false;
//		}
//
//		if (!alreadyStarted) {
//			alreadyStarted = true;
//			if (appelliPrenotatiScraper == null || !appelliPrenotatiScraper.isRunning) {
//				appelliPrenotatiScraper = new Esse3AppelliPrenotatiScraper();
//				appelliPrenotatiScraper.setCallerAppelliFragment(this);
//				appelliPrenotatiScraper.execute(activity);
//			}
//			if (appelliDisponibiliScraper == null || !appelliDisponibiliScraper.isRunning) {
//				appelliDisponibiliScraper = new Esse3AppelliDisponibiliScraper();
//				appelliDisponibiliScraper.setCallerAppelliFragment(this);
//				appelliDisponibiliScraper.execute(activity);
//			}
//		} else {
//			// activity.goToLastFrame();
//			mostraAppelliDisponibili(listaAppelliDisponibili);
//			mostraAppelliPrenotati(listaAppelliPrenotati);
//		}
//	}
	
	public void mostraAppelli(Appelli appelli) {
		if (!isAdded()) {
			return;			
		}
		if (appelliDisponibiliContainer == null || appelliPrenotatiContainer == null || appelliNDView == null) { 	// Dai report di crash sembra succedere a volte, non ho idea del perchè
			activity.showMenu();							   			// Quindi mostro lo slidingmenu per apparare
			activity.setLoadingVisible(false, false);
			return;
		}
		if (appelli != null) {
			this.appelli = appelli;
		}
		if (this.appelli == null || (this.appelli.getListaAppelliDisponibili().size() == 0 && this.appelli.getListaAppelliPrenotati().size() == 0)) {
			// Non ho appelli da mostrare
			appelliNDView.setVisibility(View.VISIBLE);
			appelliDisponibiliContainer.setVisibility(View.GONE);
			appelliPrenotatiContainer.setVisibility(View.GONE);
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
			lastUpdateSepView.setVisibility(View.GONE);
			activity.setLoadingVisible(false, false);
			return;
		} 
		
		appelliNDView.setVisibility(View.GONE);
		if (this.appelli.getFetchTime().getTime() > 0) {
			String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(this.appelli.getFetchTime());
		    String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(this.appelli.getFetchTime());
		    String updateText = getString(R.string.aggiornato_il_alle, dateFirstPart, dateSecondPart);
			lastUpdateView.setText(updateText);
			lastUpdateView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
			lastUpdateSepView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
			lastUpdateSepView.setVisibility(View.GONE);
		}
		
		mostraAppelliDisponibili(this.appelli.getListaAppelliDisponibili());
		mostraAppelliPrenotati(this.appelli.getListaAppelliPrenotati());
		
		if (appelli != null) {
			// Il metodo è stato chiamato con gli appelli aggiornati da salvare
			Log.d(Utils.TAG, "Salvo gli appelli!");
			pref.setAppelli(appelli);
			pref.saveData();
		}
		
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
		View rowView = layoutInflater.inflate(R.layout.appelli_row, null);
		TextView nameView = (TextView) rowView.findViewById(R.id.appello_name);
		TextView descriptionView = (TextView) rowView.findViewById(R.id.appello_description);
		TextView dateView = (TextView) rowView.findViewById(R.id.appello_date);
		TextView nSubsView = (TextView) rowView.findViewById(R.id.appello_subscribed_num);
		nameView.setText(appello.getName());
		dateView.setText(appello.getDate());
		descriptionView.setText(appello.getDescription());
		nSubsView.setText(appello.getSubscribedNum());
		return rowView;
	}
}
