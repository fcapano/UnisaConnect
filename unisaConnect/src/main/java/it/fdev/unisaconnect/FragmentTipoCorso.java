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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.scraper.esse3.Esse3TipoCorsoScraper.TipoCorso;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyListFragment;
import it.fdev.utils.TipoCorsoAdapter;
import it.fdev.utils.Utils;

public class FragmentTipoCorso extends MyListFragment {

	private TipoCorsoAdapter adapter;
	private ArrayList<TipoCorso> tipoCorsoList = null;
	private TextView listEmptyView;
	private ListView listCardsView;

	private boolean alreadyStarted = false;

	private SharedPrefDataManager mDataManager;

	private IntentFilter mIntentFilter = new IntentFilter();
	private final BroadcastReceiver mHandlerBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onNewBroadcast(context, intent);
		}
	};
	
	public void onNewBroadcast(Context context, Intent intent) {
		try {
			Log.d(Utils.TAG, "BROADCAST RECEIVED IN FragmentTipoCorso: " + intent.getAction());
			if (Esse3ScraperService.BROADCAST_STATE_E3_TIPO_CORSO.equals(intent.getAction())) {
				LoadStates state = (LoadStates) intent.getSerializableExtra("status");
				switch (state) {
				case FINISHED:
					if(intent.hasExtra("list")) {
						ArrayList<TipoCorso> lista = intent.getParcelableArrayListExtra("list");
						showCards(lista);
					}
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
			Utils.createAlert(mActivity, mActivity.getString(R.string.problema_di_connessione_generico), null, true);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getTipoCorsi(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new TipoCorsoAdapter(mActivity, R.layout.card_news, new ArrayList<TipoCorso>());
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_cards_ui, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mIntentFilter.addAction(Esse3ScraperService.BROADCAST_STATE_E3_TIPO_CORSO);
		mDataManager = SharedPrefDataManager.getInstance(mActivity);
		
		listEmptyView = (TextView) view.findViewById(R.id.card_list_empty);
		listCardsView = (ListView) view.findViewById(android.R.id.list);

		/* Metto animazione */
		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
		/* Indico che la listView di questo ListFragment deve avere il mio controller per l'animazione */
		getListView().setLayoutAnimation(controller);

//		getTipoCorsi(false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			Log.d(Utils.TAG, "Position:" + position);
			mDataManager.setTipoCorso(position+1);
			Toast.makeText(mActivity, "Hai cambiato il corso di laurea da visualizzare", Toast.LENGTH_LONG).show();
			mActivity.startService(new Intent(mActivity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO));
			mActivity.switchContent(BootableFragmentsEnum.STUDENT_SERVICES, true);
		} catch (Exception e) {
			Log.w(Utils.TAG, e);
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
		getTipoCorsi(true);
	}

	@Override
	public int getTitleResId() {
		return R.string.tipo_corso_title;
	}
	
	public void getTipoCorsi(boolean force) {
		if (!isAdded()) {
			return;
		}
		
		if (!force && alreadyStarted) {
			return;
		}
		
		mActivity.setLoadingVisible(true, true);
		
		listCardsView.setSelectionAfterHeaderView();
		if (!force && tipoCorsoList != null) {
			showCards(null);
			mActivity.setLoadingVisible(false, false);
			return;
		}
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}
		
		alreadyStarted = true;
		if (Esse3ScraperService.isRunning) {
			mActivity.setLoadingVisible(true);
			return;
		}
		
		mActivity.startService(new Intent(mActivity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_TIPO_CORSO));
		return;
	}

	public void showCards(ArrayList<TipoCorso> tipoCorsoList) {
		if (!isAdded()) {
			return;			
		}
		
		if (listEmptyView == null || listCardsView == null) { 			// Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true);							   	// Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}
		
		if (tipoCorsoList != null) {
			this.tipoCorsoList = tipoCorsoList;
		}
		
		if (this.tipoCorsoList == null) {								// Non ho un menu da mostrare
			listEmptyView.setVisibility(View.GONE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		} else if (this.tipoCorsoList.size() == 0) {
			listEmptyView.setVisibility(View.VISIBLE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		} else {
			listEmptyView.setVisibility(View.GONE);
			listCardsView.setVisibility(View.VISIBLE);
		}
		
		adapter.clear();
		adapter.addAll(tipoCorsoList);
		adapter.notifyDataSetChanged();
		
		mActivity.setLoadingVisible(false, false);
	}

}