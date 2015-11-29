package it.fdev.unisaconnect;

import android.content.Intent;
import android.net.Uri;
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

import it.fdev.scraper.NewsScraper;
import it.fdev.utils.CardsAdapter;
import it.fdev.utils.CardsAdapter.CardItem;
import it.fdev.utils.MyListFragment;
import it.fdev.utils.Utils;

public class FragmentNews extends MyListFragment {

	private final String NEWS_URL_NEW = "http://unisanews-aleric.appspot.com/read";
	
	private CardsAdapter adapter;
	private boolean alreadyStarted = false;
	private NewsScraper rssScraper;
	private ArrayList<CardItem> cardsList = null;
	private TextView listEmptyView;
	private ListView listCardsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new CardsAdapter(mActivity, R.layout.card_news, new ArrayList<CardItem>());
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
		
		listEmptyView = (TextView) view.findViewById(R.id.card_list_empty);
		listCardsView = (ListView) view.findViewById(android.R.id.list);
		
		/* Metto animazione */
		LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
		/* Indico che la listView di questo ListFragment deve avere il mio controller per l'animazione */
		getListView().setLayoutAnimation(controller);
		
		getNews(false);
	}
	
	@Override
	public void onStop() {
		if (rssScraper != null && rssScraper.isRunning) {
			rssScraper.cancel(true);
		}
		super.onStop();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			String url = cardsList.get(position).getLink();
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        startActivity(webIntent);
		} catch(Exception e) {
			Toast.makeText(mActivity, R.string.problema_aprire_link, Toast.LENGTH_SHORT).show();
			Log.w(Utils.TAG, e);
		}
	}
	
	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_twitter_button);
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}
	
	@Override
	public void actionRefresh() {
		getNews(true);
	}
	
	@Override
	public void actionTwitter() {	
		try {
			String url = "https://twitter.com/UniSA_news";
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        startActivity(webIntent);
		} catch(Exception e) {
			Log.e(Utils.TAG, "Error opening twitter link", e);
		}
	}
	
	@Override
	public int getTitleResId() {
		return R.string.news;
	}

	public void getNews(boolean force) {
		if (!isAdded()) {
			return;
		}
		mActivity.setLoadingVisible(true, true);
		
		listCardsView.setSelectionAfterHeaderView();
		if (!force && cardsList != null) {
			showCards(null);
			mActivity.setLoadingVisible(false, false);
			return;
		}
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}
		
		alreadyStarted = true;
		if (rssScraper != null && rssScraper.isRunning) {
			mActivity.setLoadingVisible(true);
			return;
		}
		rssScraper = new NewsScraper(NEWS_URL_NEW);
//		rssScraper.setMaxItems(MAX_NEWS_NUMBER);
//		rssScraper.setMaxTextLength(MAX_TEXT_LENGTH);
		rssScraper.setCallerFragment(this);
		rssScraper.execute(mActivity);
		return;
	}
	
	public void showCards(ArrayList<CardItem> cardsList) {
		if (!isAdded()) {
			return;			
		}
		
		if (listEmptyView == null || listCardsView == null) { 			// Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true);							   	// Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}
		
		if (cardsList != null) {
			this.cardsList = cardsList;
		}
		
		if (this.cardsList == null) {				// Non ho un menu da mostrare
			listEmptyView.setVisibility(View.GONE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		} else if (this.cardsList.size() == 0) {
			listEmptyView.setVisibility(View.VISIBLE);
			listCardsView.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		} else {
			listEmptyView.setVisibility(View.GONE);
			listCardsView.setVisibility(View.VISIBLE);
		}
		
		adapter.clear();
		adapter.addAll(cardsList);
		adapter.notifyDataSetChanged();
		
		mActivity.setLoadingVisible(false, false);
	}

}