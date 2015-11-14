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

import it.fdev.scraper.BiblioSearchScraper;
import it.fdev.unisaconnect.data.Book;
import it.fdev.utils.CardsAdapter;
import it.fdev.utils.CardsAdapter.CardItem;
import it.fdev.utils.MyListFragment;
import it.fdev.utils.Utils;

public class FragmentBiblioDoSearch extends MyListFragment {

	public static final String ARG_URI = "uri";
	public static final String BROADCAST_STATUS = "status";

	private CardsAdapter adapter;
	private CardItem moreResultsCard;
	private boolean alreadyStarted = false;
	private TextView listEmptyView;
	private ListView listCardsView;
	private String mSearchURL;
	private int searchArgumentJump = 1;

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

		mSearchURL = getArguments().getString(ARG_URI);
		if (mSearchURL == null) {
			return;
		}

		listEmptyView = (TextView) view.findViewById(R.id.card_list_empty);
		listCardsView = (ListView) view.findViewById(android.R.id.list);

		moreResultsCard = new CardItem("\nMostra altri risultati...\n", "", "", "", false);

		/* Metto animazione */
		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
		/* Indico che la listView di questo ListFragment deve avere il mio controller per l'animazione */
		getListView().setLayoutAnimation(controller);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getLibri(false, true);
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	public void onNewBroadcast(Context context, Intent intent) {
		try {
			if (BiblioSearchScraper.BROADCAST_STATE_BIBLIO_SEARCH.equals(intent.getAction())) {
				if (intent.hasExtra(BROADCAST_STATUS)) {
					ArrayList<Book> list = intent.getParcelableArrayListExtra(BROADCAST_STATUS);
					if (list != null && !list.isEmpty()) {
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
						showLibri(cardsList, false);
					} else {
						showLibri(null, false);
					}
				} else {
					showLibri(null, false);
				}
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
			showLibri(null, false);
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
			if (position == mCardsList.size()) {
				searchArgumentJump += 10;
				getLibri(true, false);
			} else {
				String url = mCardsList.get(position).getLink();
				FragmentBiblioShowBook fragmentShowBook = new FragmentBiblioShowBook();
				Bundle args = new Bundle();
				args.putString(ARG_URI, url);
				fragmentShowBook.setArguments(args);
				mActivity.switchContent(fragmentShowBook);
			}
		} catch (Exception e) {
			Toast.makeText(mActivity, R.string.problema_aprire_link, Toast.LENGTH_SHORT).show();
			Log.w(Utils.TAG, e);
		}
	}

	public void getLibri(boolean force, boolean clearList) {
		if (!isAdded()) {
			return;
		}

		// Lo scraper è in esecuzione
		if (BiblioSearchScraper.isRunning) {
			return;
		}

		if (clearList) {
			mActivity.setLoadingVisible(true, true);
			listCardsView.setSelectionAfterHeaderView();
		} else {
			mActivity.setLoadingVisible(true, false);
		}

		if (!force && mCardsList != null) {
			alreadyStarted = true;
			showLibri(null, clearList);
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

			String newUrl;
			if (searchArgumentJump > 1) {
				newUrl = mSearchURL.replace("func=find-b", "func=short-jump");
				newUrl += "&jump=" + searchArgumentJump;
			} else {
				newUrl = mSearchURL;
			}
			// Log.d(Utils.TAG, "newUrl: " + newUrl);

			Intent searchIntent = new Intent(mActivity, BiblioSearchScraper.class);
			searchIntent.putExtra(ARG_URI, newUrl);
			mActivity.startService(searchIntent);
		} else {
			listEmptyView.setVisibility(View.VISIBLE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}

	public void showLibri(ArrayList<CardItem> cardsList, boolean clearList) {
		if (!isAdded()) {
			return;
		}

		if (listEmptyView == null || listCardsView == null) { // Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true); // Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}
		
		if (cardsList == null) {
			cardsList = new ArrayList<CardsAdapter.CardItem>();
		}
		if (mCardsList == null) {
			mCardsList = new ArrayList<CardsAdapter.CardItem>();
		}
		
		if (cardsList.isEmpty()) {
			if (mCardsList.isEmpty()) {
				listEmptyView.setVisibility(View.VISIBLE);
				listCardsView.setVisibility(View.GONE);
				mActivity.setLoadingVisible(false, false);
				return;
			}
		} else {
			if (clearList) {
				mCardsList = cardsList;
			} else {
				mCardsList.addAll(cardsList);
			}
		}

		listEmptyView.setVisibility(View.GONE);
		listCardsView.setVisibility(View.VISIBLE);

		if (clearList) {
			adapter.clear();
			adapter.addAll(mCardsList);
		} else {
			adapter.remove(moreResultsCard);
			if (cardsList.isEmpty()) {
				
			}
			adapter.addAll(cardsList);
		}

		adapter.add(moreResultsCard);
		adapter.notifyDataSetChanged();
		mActivity.setLoadingVisible(false, false);
	}

	@Override
	public void actionRefresh() {
		super.actionRefresh();
		getLibri(true, true);
	}

	@Override
	public int getTitleResId() {
		return R.string.risultati_ricerca;
	}

}
