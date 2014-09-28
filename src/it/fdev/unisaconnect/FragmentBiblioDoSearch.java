package it.fdev.unisaconnect;

import it.fdev.scraper.BiblioSearchScraper;
import it.fdev.unisaconnect.data.Book;
import it.fdev.utils.CardsAdapter;
import it.fdev.utils.CardsAdapter.CardItem;
import it.fdev.utils.MyListFragment;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentBiblioDoSearch extends MyListFragment {

	private CardsAdapter adapter;
	private boolean alreadyStarted = false;
	private TextView listEmptyView;
	private ListView listCardsView;
	private String mSearchURL;

	ArrayList<CardItem> mCardsList;

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
		adapter = new CardsAdapter(mActivity, R.layout.card_book, new ArrayList<CardItem>());
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

		mIntentFilter.addAction(BiblioSearchScraper.BROADCAST_STATE_BIBLIO_SEARCH);

		mActivity.setLoadingVisible(true, true);

		if (mSearchURL == null) {
			return;
		}

		listEmptyView = (TextView) view.findViewById(R.id.card_list_empty);
		listCardsView = (ListView) view.findViewById(android.R.id.list);

		/* Metto animazione */
		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
		/* Indico che la listView di questo ListFragment deve avere il mio controller per l'animazione */
		getListView().setLayoutAnimation(controller);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getLibri(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	public void onNewBroadcast(Context context, Intent intent) {
		try {
			if (BiblioSearchScraper.BROADCAST_STATE_BIBLIO_SEARCH.equals(intent.getAction())) {
				if (intent.hasExtra("status")) {
					ArrayList<Book> list = intent.getParcelableArrayListExtra("status");
					if (list != null) {
						ArrayList<CardItem> cardsList = new ArrayList<CardsAdapter.CardItem>();
						for (Book book : list) {
							String text = "";
							if (!book.getAuthor().isEmpty()) {
								text += getString(R.string.autore) + ": " + book.getAuthor();
							}
							if (!book.getPosition().isEmpty()) {
								if (!text.isEmpty()) {
									text += "\n";
								}
								text += getString(R.string.posizione) + ": " + book.getPosition();
							}
							CardItem cCard = new CardItem(book.getTitle(), book.getDetailsUrl(), text, book.getYear(), false);
							cardsList.add(cCard);
						}
						showLibri(cardsList);
					} else {
						showLibri(null);
					}
				} else {
					showLibri(null);
				}
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
			showLibri(null);
		}
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted || BiblioSearchScraper.isRunning) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			String url = mCardsList.get(position).getLink();
//			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//	        startActivity(webIntent);
	        
	        FragmentBiblioShowBook fragmentShowBook = new FragmentBiblioShowBook();
	        fragmentShowBook.setURL(url);
			mActivity.switchContent(fragmentShowBook);
		} catch(Exception e) {
			Toast.makeText(mActivity, R.string.problema_aprire_link, Toast.LENGTH_SHORT).show();
			Log.w(Utils.TAG, e);
		}
	}

	public void getLibri(boolean force) {
		if (!isAdded()) {
			return;
		}

		// Lo scraper è in esecuzione
		if (BiblioSearchScraper.isRunning) {
			return;
		}

		listCardsView.setSelectionAfterHeaderView();
		mActivity.setLoadingVisible(true, true);

		if (!force && mCardsList != null) {
			alreadyStarted = true;
			showLibri(null);
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}

		if (force || !alreadyStarted) {
			alreadyStarted = true;
			Log.d(Utils.TAG, "Starting biblio search...");
			mActivity.startService(new Intent(mActivity, BiblioSearchScraper.class).putExtra("URL", mSearchURL));
		} else {
			listEmptyView.setVisibility(View.VISIBLE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}

	public void showLibri(ArrayList<CardItem> cardsList) {
		if (!isAdded()) {
			return;
		}

		if (listEmptyView == null || listCardsView == null) { // Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true); // Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}

		if (cardsList != null) {
			this.mCardsList = cardsList;
		}

		if (this.mCardsList == null || this.mCardsList.isEmpty()) { // Non ho un menu da mostrare
			listEmptyView.setVisibility(View.VISIBLE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}

		listEmptyView.setVisibility(View.GONE);
		listCardsView.setVisibility(View.VISIBLE);

		adapter.clear();
		adapter.addAll(mCardsList);
		adapter.notifyDataSetChanged();

		mActivity.setLoadingVisible(false, false);
	}

	@Override
	public void actionRefresh() {
		super.actionRefresh();
		getLibri(true);
	}

	public void setURL(String url) {
		this.mSearchURL = url;
	}

	@Override
	public int getTitleResId() {
		return R.string.risultati_ricerca;
	}

}
