package it.fdev.unisaconnect;

import it.fdev.scraper.PresenzeScraper;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Presenze;
import it.fdev.unisaconnect.data.Presenze.Esame;
import it.fdev.unisaconnect.data.Presenze.RiepilogoPresenzeEsame;
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

public class FragmentPresenze extends MySimpleFragment {
	
	private SharedPrefDataManager mDataManager;
	private PresenzeScraper presenzeScraper;
	private boolean alreadyStarted = false;
	private Presenze presenze;
	
	private View presenzeContainerView;
	private View presenzeNDView;
	private TextView lastUpdateView;
	private View lastUpdateIconView;
//	private View lastUpdateSepView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_presenze, container, false);
		return mainView;
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
		
		presenzeContainerView = view.findViewById(R.id.presenze_list_container);
		presenzeNDView = view.findViewById(R.id.presenze_non_disponibili);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
//		lastUpdateSepView = (View) view.findViewById(R.id.last_update_sep);
		
		presenze = mDataManager.getPresenze();
		if (presenze != null) {
			Log.d(Utils.TAG, "Presenze salvate trovate!");
//		} else {
//			ArrayList<RiepilogoPresenzeEsame> listaRiepilogoPresenze = new ArrayList<RiepilogoPresenzeEsame>();
//			listaRiepilogoPresenze.add(new RiepilogoPresenzeEsame("Laboratorio", "10", "50", "50%"));
//			listaRiepilogoPresenze.add(new RiepilogoPresenzeEsame("Laboratorio", "10", "50", "50%"));
//			listaRiepilogoPresenze.add(new RiepilogoPresenzeEsame("Laboratorio", "10", "50", "50%"));
//			ArrayList<Esame> esami = new ArrayList<Esame>();
//			esami.add(new Esame("Architettura", "2013", listaRiepilogoPresenze));
//			esami.add(new Esame("Architettura", "2013", listaRiepilogoPresenze));
//			esami.add(new Esame("Architettura", "2013", listaRiepilogoPresenze));
//			esami.add(new Esame("Architettura", "2013", listaRiepilogoPresenze));
//			presenze = new Presenze(esami);
//			mostraPresenze(presenze);
//			return;
		}
		getPresenze(false);
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
		getPresenze(true);
	}

	public void getPresenze(boolean force) {
		if (!isAdded()) {
			return;
		}
		
		activity.setLoadingVisible(true, true);
		
		if (!force && presenze != null) {
			mostraPresenze(null);
			activity.setLoadingVisible(false, false);
			return;
		}
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}
//		if (force || !alreadyStarted) {
			alreadyStarted = true;
			if (presenzeScraper != null && presenzeScraper.isRunning) {
				activity.setLoadingVisible(true);
				return;
			}
			presenzeScraper = new PresenzeScraper();
			presenzeScraper.setCallerPresenzeFragment(this);
			presenzeScraper.execute(activity);
			return;
//		}
//		mostraPresenze(null);
	}

	public void mostraPresenze(Presenze presenze) {
		if (!isAdded()) {
			return;			
		}
		if (presenzeContainerView == null || presenzeNDView == null) { 	// Dai report di crash sembra succedere a volte, non ho idea del perchè
			activity.setDrawerOpen(true);							   			// Quindi mostro lo slidingmenu per apparare
			activity.setLoadingVisible(false, false);
			return;
		}
		if (presenze != null) {
			this.presenze = presenze;
		}
		if (this.presenze == null) {				// Non ho un menu da mostrare
			presenzeNDView.setVisibility(View.VISIBLE);
			presenzeContainerView.setVisibility(View.GONE);
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
//			lastUpdateSepView.setVisibility(View.GONE);
			activity.setLoadingVisible(false, false);
			return;
		} 
		
		presenzeNDView.setVisibility(View.GONE);
		presenzeContainerView.setVisibility(View.VISIBLE);
		if (this.presenze.getFetchTime().getTime() > 0) {
			String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(this.presenze.getFetchTime());
		    String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(this.presenze.getFetchTime());
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
		
		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout presenzeListView = (LinearLayout) activity.findViewById(R.id.presenze_list);
		presenzeListView.removeAllViews();
		
		ArrayList<Esame> listaEsami = this.presenze.getListaEsami();
		Esame cEsame;
		for(int i=0; i<listaEsami.size(); i++) {
			cEsame = listaEsami.get(i);
			LinearLayout view = inflatePresenza(cEsame, layoutInflater);
			presenzeListView.addView(view);
		}
		
		if (presenze != null) {
			// Il metodo è stato chiamato con le presenze aggiornate da salvare
			Log.d(Utils.TAG, "Salvo le presenze!");
			mDataManager.setPresenze(presenze);
//			pref.saveData();
		}
		
		activity.setLoadingVisible(false, false);

	}

	private LinearLayout inflatePresenza(Esame esame, LayoutInflater layoutInflater) {
		LinearLayout courseView = (LinearLayout) layoutInflater.inflate(R.layout.mensa_course, null);
		TextView labelView = (TextView) courseView.findViewById(R.id.course_label);
//		labelView.setVisibility(View.GONE);
		labelView.setText(esame.getNomeEsame());
		
		ArrayList<RiepilogoPresenzeEsame> listaRiepilogo = esame.getListaRiepilogoPresenze();
		RiepilogoPresenzeEsame cRiepilogo;
		for (int i=0; i<listaRiepilogo.size(); i++) {
			cRiepilogo = listaRiepilogo.get(i);
		
			String nomeAttivita = cRiepilogo.getAttivita();
			String presenze = cRiepilogo.getPercentualePresenza();
			
			LinearLayout detailsView = (LinearLayout) layoutInflater.inflate(R.layout.mensa_course_details, null);
			TextView nameView = (TextView) detailsView.findViewById(R.id.course_name);
			TextView ingredientsITView = (TextView) detailsView.findViewById(R.id.course_ingredients_it);
			TextView ingredientsENView = (TextView) detailsView.findViewById(R.id.course_ingredients_en);
			
			nameView.setText(nomeAttivita);
			ingredientsITView.setText("Percentuale Frequenza: " + presenze);
			ingredientsENView.setVisibility(View.GONE);
			
			courseView.addView(detailsView);
		}
		return courseView;
	}

	@Override
	public void onStop() {
		if (presenzeScraper != null && presenzeScraper.isRunning) {
			presenzeScraper.cancel(true);
		}
		super.onStop();
	}
	
	@Override
	public int getTitleResId() {
		return R.string.presenze;
	}
}
