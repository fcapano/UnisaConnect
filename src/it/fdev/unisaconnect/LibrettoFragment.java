package it.fdev.unisaconnect;

import it.fdev.scrapers.Esse3LibrettoScraper;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Libretto;
import it.fdev.unisaconnect.data.Libretto.LibrettoCourse;
import it.fdev.unisaconnect.data.LibrettoDB;
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

public class LibrettoFragment extends MySimpleFragment {

	private LibrettoDB librettoDB;
	private boolean alreadyStarted = false;
	private Esse3LibrettoScraper librettoScraper;
	
	private LinearLayout corsiContainerView;
	private TextView lastUpdateView;
	private View lastUpdateIconView;
	private View lastUpdateSepView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.libretto, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Se non sono stati salvati i dati utente rimando al fragment dei dati
		SharedPrefDataManager dataManager = SharedPrefDataManager.getDataManager(activity);
		if (!dataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), BootableFragmentsEnum.WIFI_PREF, false);
			return;
		}
		
		corsiContainerView = (LinearLayout) view.findViewById(R.id.courses_list);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
		lastUpdateSepView = (View) view.findViewById(R.id.last_update_sep);
		
		activity.setLoadingVisible(true, true);
		
		mostraCorsi();
	}

	@Override
	public void onStop() {
		if (librettoScraper != null && librettoScraper.isRunning) {
			librettoScraper.cancel(true);
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (alreadyStarted && librettoScraper != null && !librettoScraper.isRunning) {
			Libretto libretto = librettoDB.getLibretto();
			ArrayList<LibrettoCourse> corsi = libretto.getCorsi();
			if (corsi.size() == 0) {
				Log.d(Utils.TAG, "Errore nella creazione del libretto");
				activity.goToLastFrame();
			}
		}
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

	@Override
	public void actionRefresh() {
		activity.setLoadingVisible(true, false);
		startEsse3LibrettoScraper(true);
	}

	public void startEsse3LibrettoScraper(boolean force) {
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
		try {
			Libretto libretto = librettoDB.getLibretto();
			if (force || (!alreadyStarted && libretto.getCorsi().size() == 0)) {
				alreadyStarted = true;
				if (librettoScraper != null && librettoScraper.isRunning) {
					return;
				}
				librettoScraper = new Esse3LibrettoScraper();
				librettoScraper.setCallerLibrettoFragment(this);
				librettoScraper.execute(activity);
			} else {
				alreadyStarted = true;
				activity.goToLastFrame();
			}
		} finally {
			librettoDB.close();
		}
	}

	public void mostraCorsi() {
		librettoDB = new LibrettoDB(activity);
		librettoDB.open();
		try {
			Libretto libretto = librettoDB.getLibretto();
			ArrayList<LibrettoCourse> corsi = libretto.getCorsi();
			if (corsi.size() == 0) {
				startEsse3LibrettoScraper(false);
			} else {
				mostraCorsi(libretto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			librettoDB.close();
		}
	}

	public void mostraCorsi(Libretto libretto) {
		int sommaCFU = 0;
		int sommaVotiPesati = 0;

		while (corsiContainerView.getChildCount() > 2) {
			corsiContainerView.removeViewAt(2);
		}

		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		ArrayList<LibrettoCourse> corsi = libretto.getCorsi();
		
//		for (int i = 0; i < 2; i++)
		for (LibrettoCourse corso : corsi) {
			try { // Calcolo la media
				int cfu = Integer.parseInt(corso.getCFU());
				int mark;
				if (corso.getMark().equalsIgnoreCase("30L"))
					mark = 30;
				else
					mark = Integer.parseInt(corso.getMark());
				if (cfu > 0 && mark >= 18) {
					sommaCFU += cfu;
					sommaVotiPesati += mark * cfu;
				}
			} catch (NumberFormatException e) {
				// Esami con ideneità. Compaiono come "SUP" e non influiscono sulla media
			}
			if (corso.getMark().isEmpty()) // Esame non ancora fatto
				continue;
			View rowView = layoutInflater.inflate(R.layout.libretto_row, null);
			TextView nameView = (TextView) rowView.findViewById(R.id.course_name);
			TextView cfuView = (TextView) rowView.findViewById(R.id.course_credits);
			TextView markView = (TextView) rowView.findViewById(R.id.course_mark);
			nameView.setText(corso.getName());
			cfuView.setText(corso.getCFU());
			markView.setText(corso.getMark());
			corsiContainerView.addView(rowView);
		}

		float mediaPesata = sommaVotiPesati / (float) sommaCFU;
		mediaPesata = Math.round(mediaPesata * (float) 1000) / (float) 1000;
		TextView avgWeightedView = (TextView) corsiContainerView.findViewById(R.id.avg_weighted);
		avgWeightedView.setText(Float.toString(mediaPesata));
		
		corsiContainerView.setVisibility(View.VISIBLE);
		
		if (libretto.getFetchTime().getTime() > 0) {
		    String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(libretto.getFetchTime());
		    String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(libretto.getFetchTime());
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
		
		activity.setLoadingVisible(false, false);
	}
}
