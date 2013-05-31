package it.fdev.unisaconnect;

import it.fdev.scrapers.Esse3LibrettoScraper;
import it.fdev.unisaconnect.data.LibrettoCourse;
import it.fdev.unisaconnect.data.LibrettoDB;
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

public class LibrettoFragment extends MySimpleFragment {

	private LibrettoDB librettoDB;
	private boolean alreadyStarted = false;
	private Esse3LibrettoScraper librettoScraper;

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
			Utils.createAlert(activity, getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
			return;
		}
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
			ArrayList<LibrettoCourse> corsi = librettoDB.getCourses();
			if (corsi.size() == 0) {
				Log.d(Utils.TAG, "Errore nella creazione del libretto");
				activity.goToLastFrame();
			}
		}
	}

	@Override
	public void setVisibleActions() {
		activity.setActionRefreshVisible(true);
	}

	@Override
	public void actionRefresh() {
		startEsse3LibrettoScraper(true);
	}

	public void startEsse3LibrettoScraper(boolean force) {
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
		try {
			if (force || (!alreadyStarted && librettoDB.getCourses().size() == 0)) {
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
			ArrayList<LibrettoCourse> corsi = librettoDB.getCourses();
			if (corsi.size() == 0) {
				startEsse3LibrettoScraper(false);
			} else {
				mostraCorsi(corsi);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			librettoDB.close();
		}
	}

	public void mostraCorsi(ArrayList<LibrettoCourse> corsi) {
		int sommaCFU = 0;
		int sommaVotiPesati = 0;
		LinearLayout corsiContainer = (LinearLayout) activity.findViewById(R.id.courses_list);
		corsiContainer.setVisibility(View.INVISIBLE);

		while (corsiContainer.getChildCount() > 2) {
			corsiContainer.removeViewAt(2);
		}

		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			} catch (NumberFormatException e) { // Esami con ideneità. Compaiono
												// come "SUP"
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
			corsiContainer.addView(rowView);
		}

		float mediaPesata = sommaVotiPesati / (float) sommaCFU;
		mediaPesata = Math.round(mediaPesata * (float) 1000) / (float) 1000;
		TextView avgWeightedView = (TextView) corsiContainer.findViewById(R.id.avg_weighted);
		avgWeightedView.setText(Float.toString(mediaPesata));

		corsiContainer.setVisibility(View.VISIBLE);
	}
}
