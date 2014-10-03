package it.fdev.unisaconnect;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Pagamenti;
import it.fdev.unisaconnect.data.Pagamenti.Pagamento;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyDateUtils;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
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

public class FragmentPagamenti extends MySimpleFragment {

	private boolean alreadyStarted = false;
	
	private SharedPrefDataManager mDataManager;
	
	private TextView pagamentiVuotoView;
	private LinearLayout pagamentiContainerView;
	private TextView lastUpdateTextView;
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
		return inflater.inflate(R.layout.fragment_pagamenti, container, false);
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
		
		pagamentiVuotoView = (TextView) view.findViewById(R.id.pagamenti_vuoto);
		pagamentiContainerView = (LinearLayout) view.findViewById(R.id.pagamenti_list);
		lastUpdateTextView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateTextView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);
//		lastUpdateSepView = (View) view.findViewById(R.id.last_update_sep);
		
		Pagamenti pagamenti = mDataManager.getPagamenti();
		if (pagamenti != null) {
			Log.d(Utils.TAG, "Pagamenti salvati trovate!");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getPagamenti(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	public void onNewBroadcast(Context context, Intent intent) {
		try {
			Log.d(Utils.TAG, "BROADCAST RECEIVED: " + intent.getAction());
			if (Esse3ScraperService.BROADCAST_STATE_E3_PAGAMENTI.equals(intent.getAction())) {
				LoadStates state = (LoadStates) intent.getSerializableExtra("status");
				switch (state) {
				case FINISHED:
					mostraPagamenti();
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

	@Override
	public void actionRefresh() {
		getPagamenti(true);
	}
	
	public void getPagamenti(boolean force) {
		if (!isAdded()) {
			return;
		}
		
		// Lo scraper è in esecuzione
		if (Esse3ScraperService.isRunning) {
			return;
		}
		
		mActivity.setLoadingVisible(true, true);
		
		if (!force && mDataManager.getPagamenti()!=null) {
			alreadyStarted = true;
			mostraPagamenti();
			return;
		}
		
		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}
		
		if (force || !alreadyStarted) {
			alreadyStarted = true;
			mActivity.startService(new Intent(mActivity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_PAGAMENTI));
		} else {
			pagamentiVuotoView.setVisibility(View.VISIBLE);
			pagamentiContainerView.setVisibility(View.GONE);
			lastUpdateTextView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}
	
	
	public void mostraPagamenti() {
		if (!isAdded()) {
			return;			
		}
		if (pagamentiVuotoView == null || pagamentiContainerView == null) { 	// Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true);							   			// Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}

		Pagamenti pagamenti = mDataManager.getPagamenti();
		
//		ArrayList<Pagamento> p = new ArrayList<Pagamento>();
//		p.add(new Pagamento("RATA UNICA", "TASSA REGIONALE DIRITTO ALLO STUDIO", "140,00€", "30/09/2013"));
//		p.add(new Pagamento("1 DI 3", "TASSA IMMATRICOLAZIONE - ISCRIZIONI", "813,38€", "30/09/2013"));
//		p.add(new Pagamento("asd", "asdasdasd", "150€", "10/10/2020"));
//		p.add(new Pagamento("asd", "asdasdasd", "150€", "10/10/2020"));
//		p.add(new Pagamento("asd", "asdasdasd", "150€", "10/10/2020"));
//		Pagamenti pp = new Pagamenti(p);
//		pagamenti = pp;
		
		String updateText = "";
		if (pagamenti != null) {
			updateText = MyDateUtils.getLastUpdateString(mActivity, pagamenti.getFetchTime().getTime(), false);
		}
		if (!updateText.isEmpty()) {
			lastUpdateTextView.setText(updateText);
			lastUpdateTextView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateTextView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		}
		
		if (pagamenti == null || pagamenti.getListaPagamenti().size() == 0) {				// Non ho pagamenti da mostrare
			pagamentiVuotoView.setVisibility(View.VISIBLE);
			pagamentiContainerView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
		
		pagamentiVuotoView.setVisibility(View.GONE);
		pagamentiContainerView.setVisibility(View.VISIBLE);
		
		LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout pagamentiListView = (LinearLayout) mActivity.findViewById(R.id.pagamenti_list);
		pagamentiListView.removeAllViews();
		
		ArrayList<Pagamento> listaPagamenti = pagamenti.getListaPagamenti();
		Pagamento cPagamento;
		for(int i=0; i<listaPagamenti.size(); i++) {
			cPagamento = listaPagamenti.get(i);
			View view = inflatePagamento(cPagamento, layoutInflater);
			pagamentiListView.addView(view);
		}
		mActivity.setLoadingVisible(false, false);
	}
	
	public View inflatePagamento(Pagamento pagamento, LayoutInflater layoutInflater) {
		View rowView = layoutInflater.inflate(R.layout.pagamento_row, null);
		TextView descriptionView = (TextView) rowView.findViewById(R.id.pagamento_description);
		TextView expirationView = (TextView) rowView.findViewById(R.id.pagamento_expiration);
		TextView amountView = (TextView) rowView.findViewById(R.id.pagamento_amount);
		
		String description = "";
		for (String cCause : pagamento.getCausali()) {
			description += "• " + cCause + "\n";
		}
		
		descriptionView.setText(description.trim());
		expirationView.setText(getString(R.string.scadenza) + ": " + pagamento.getScadenza());
		amountView.setText(pagamento.getImporto());
		return rowView;
	}
	
	@Override
	public int getTitleResId() {
		return R.string.pagamenti;
	}
}
