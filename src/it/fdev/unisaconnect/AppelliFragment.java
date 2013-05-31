package it.fdev.unisaconnect;

import it.fdev.scrapers.Esse3AppelliDisponibiliScraper;
import it.fdev.scrapers.Esse3AppelliPrenotatiScraper;
import it.fdev.unisaconnect.data.Appello;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

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
	private Esse3AppelliDisponibiliScraper appelliDisponibiliScraper;
	private Esse3AppelliPrenotatiScraper appelliPrenotatiScraper;
	private ArrayList<Appello> listaAppelliDisponibili;
	private ArrayList<Appello> listaAppelliPrenotati;
	private LinearLayout appelliDisponibiliContainer;
	private LinearLayout appelliPrenotatiContainer;
	private View appelliVuotoView;

	private LayoutInflater layoutInflater;

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
		SharedPrefDataManager dataManager = SharedPrefDataManager.getDataManager(activity);
		if (!dataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
			return;
		}

		layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		appelliDisponibiliContainer = (LinearLayout) activity.findViewById(R.id.appelli_disponibili_list);
		appelliPrenotatiContainer = (LinearLayout) activity.findViewById(R.id.appelli_prenotati_list);
		appelliVuotoView = activity.findViewById(R.id.appelli_vuoto);

		mostraAppelli(false);
	}

	@Override
	public void actionRefresh() {
		mostraAppelli(true);
	}
	
	@Override
	public void setVisibleActions() {
		activity.setActionRefreshVisible(true);
	}

	@Override
	public void onPause() {
		try {
			if (appelliDisponibiliScraper != null && appelliDisponibiliScraper.isRunning) {
				appelliDisponibiliScraper.cancel(true);
			}
			if (appelliPrenotatiScraper != null && appelliPrenotatiScraper.isRunning) {
				appelliPrenotatiScraper.cancel(true);
			}
		} catch (Exception e) {
			Log.d(Utils.TAG, "Error onPause AppelliFragment", e);
		}
		super.onPause();
	}

	public void mostraAppelli(boolean force) {
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}

		if (force) {
			alreadyStarted = false;
		}

		if (!alreadyStarted) {
			alreadyStarted = true;
			if (appelliPrenotatiScraper == null || !appelliPrenotatiScraper.isRunning) {
				appelliPrenotatiScraper = new Esse3AppelliPrenotatiScraper();
				appelliPrenotatiScraper.setCallerAppelliFragment(this);
				appelliPrenotatiScraper.execute(activity);
			}
			if (appelliDisponibiliScraper == null || !appelliDisponibiliScraper.isRunning) {
				appelliDisponibiliScraper = new Esse3AppelliDisponibiliScraper();
				appelliDisponibiliScraper.setCallerAppelliFragment(this);
				appelliDisponibiliScraper.execute(activity);
			}
		} else {
			// activity.goToLastFrame();
			mostraAppelliDisponibili(listaAppelliDisponibili);
			mostraAppelliPrenotati(listaAppelliPrenotati);
		}
	}

	public void mostraAppelliPrenotati(ArrayList<Appello> listaAppelli) {
		appelliPrenotatiContainer.setVisibility(View.GONE);

		if (listaAppelli != null && listaAppelli.size() > 0) {
			listaAppelliPrenotati = listaAppelli;
		}
		if (listaAppelliPrenotati == null) {
			listaAppelliPrenotati = new ArrayList<Appello>();
		}
		if (listaAppelliPrenotati.size() == 0) {
			if (listaAppelliDisponibili != null && listaAppelliDisponibili.size() == 0) {
				appelliVuotoView.setVisibility(View.VISIBLE);
				Utils.dismissDialog();
			}
			return;
		} else {
			appelliVuotoView.setVisibility(View.GONE);
		}

		while (appelliPrenotatiContainer.getChildCount() >= 2) {
			appelliPrenotatiContainer.removeViewAt(1);
		}
		for (Appello appello : listaAppelliPrenotati) {
			View rowView = setAppelliRow(appello, layoutInflater);
			appelliPrenotatiContainer.addView(rowView);
		}
		appelliPrenotatiContainer.setVisibility(View.VISIBLE);
		if (listaAppelliDisponibili != null) {
			Utils.dismissDialog();
		}
	}

	public void mostraAppelliDisponibili(ArrayList<Appello> listaAppelli) {
		appelliDisponibiliContainer.setVisibility(View.GONE);

		if (listaAppelli != null && listaAppelli.size() > 0) {
			listaAppelliDisponibili = listaAppelli;
		}
		if (listaAppelliDisponibili == null) {
			listaAppelliDisponibili = new ArrayList<Appello>();
		}
		if (listaAppelliDisponibili.size() == 0) {
			if (listaAppelliPrenotati != null && listaAppelliPrenotati.size() == 0) {
				appelliVuotoView.setVisibility(View.VISIBLE);
				Utils.dismissDialog();
			}
			return;
		} else {
			appelliVuotoView.setVisibility(View.GONE);
		}

		while (appelliDisponibiliContainer.getChildCount() >= 2) {
			appelliDisponibiliContainer.removeViewAt(1);
		}
		for (Appello appello : listaAppelliDisponibili) {
			View rowView = setAppelliRow(appello, layoutInflater);
			appelliDisponibiliContainer.addView(rowView);
		}
		appelliDisponibiliContainer.setVisibility(View.VISIBLE);
		if (listaAppelliPrenotati != null) {
			Utils.dismissDialog();
		}
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
