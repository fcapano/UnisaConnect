package it.fdev.unisaconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import it.fdev.scraper.BiblioBookScraper;
import it.fdev.unisaconnect.data.BookDetails;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

public class FragmentBiblioShowBook extends MySimpleFragment {

	private boolean alreadyStarted = false;
	private String mSearchURL;
	private BookDetails mBook;

	private View libroND, libroContainer;

	private View urlView;
	private View titoloCard, autoreCard, posizioneCard, edizioneCard, pubblicazioneCard, descrizioneCard, serieCard, linguaCard, soggettoCard, cddCard, isbnCard;
	private TextView titoloView, autoreView, posizioneView, edizioneView, pubblicazioneView, descrizioneView, serieView, linguaView, soggettoView, cddView, isbnView;

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
		View view = inflater.inflate(R.layout.fragment_biblio_book_details, container, false);

		libroND = view.findViewById(R.id.bookND);
		libroContainer = view.findViewById(R.id.bookContainer);

		urlView = view.findViewById(R.id.bookWebCard);

		titoloCard = view.findViewById(R.id.bookTitoloCard);
		autoreCard = view.findViewById(R.id.bookAutoreCard);
		posizioneCard = view.findViewById(R.id.bookPosizioneCard);
		edizioneCard = view.findViewById(R.id.bookEdizioneCard);
		pubblicazioneCard = view.findViewById(R.id.bookPubblicazioneCard);
		descrizioneCard = view.findViewById(R.id.bookDescrizioneCard);
		serieCard = view.findViewById(R.id.bookSerieCard);
		linguaCard = view.findViewById(R.id.bookLinguaCard);
		soggettoCard = view.findViewById(R.id.bookSoggettoCard);
		cddCard = view.findViewById(R.id.bookCddCard);
		isbnCard = view.findViewById(R.id.bookIsbnCard);

		titoloView = (TextView) view.findViewById(R.id.titolo);
		autoreView = (TextView) view.findViewById(R.id.autore);
		posizioneView = (TextView) view.findViewById(R.id.posizione);
		edizioneView = (TextView) view.findViewById(R.id.edizione);
		pubblicazioneView = (TextView) view.findViewById(R.id.pubblicazione);
		descrizioneView = (TextView) view.findViewById(R.id.descrizione);
		serieView = (TextView) view.findViewById(R.id.serie);
		linguaView = (TextView) view.findViewById(R.id.lingua);
		soggettoView = (TextView) view.findViewById(R.id.soggetto);
		cddView = (TextView) view.findViewById(R.id.cdd);
		isbnView = (TextView) view.findViewById(R.id.isbn);

		urlView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (mBook != null && !mBook.getDetailsUrl().isEmpty()) {
						Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mBook.getDetailsUrl()));
						startActivity(webIntent);
					}
				} catch (Exception e) {
					Log.w(Utils.TAG, "Not a valid url");
				}
			}
		});

		mActivity.setLoadingVisible(true, true);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mIntentFilter.addAction(BiblioBookScraper.BROADCAST_STATE_BIBLIO_BOOK);

		mActivity.setLoadingVisible(true, true);

		mSearchURL = getArguments().getString(FragmentBiblioDoSearch.ARG_URI);
		if (mSearchURL == null) {
			return;
		}

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
		if (BiblioBookScraper.BROADCAST_STATE_BIBLIO_BOOK.equals(intent.getAction())) {
			try {
				BookDetails book = intent.getParcelableExtra("status");
				showLibro(book);
			} catch (Exception e) {
				Log.e(Utils.TAG, "onReceiveBroadcast exception", e);
				showLibro(null);
			}
		}
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted || BiblioBookScraper.isRunning) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}

	public void getLibri(boolean force) {
		if (!isAdded()) {
			return;
		}

		// Lo scraper è in esecuzione
		if (BiblioBookScraper.isRunning) {
			return;
		}

		mActivity.setLoadingVisible(true, true);

		if (!force && mBook != null) {
			alreadyStarted = true;
			showLibro(null);
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}

		if (force || !alreadyStarted) {
			alreadyStarted = true;
			Log.d(Utils.TAG, "Starting book search...");
			mActivity.startService(new Intent(mActivity, BiblioBookScraper.class).putExtra("URL", mSearchURL));
		} else {
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}

	public void showLibro(BookDetails book) {
		if (!isAdded()) {
			return;
		}

		if (libroND == null || libroContainer == null) { // Dai report di crash sembra succedere a volte, non ho idea del perchè
			mActivity.setDrawerOpen(true); // Quindi mostro lo slidingmenu per apparare
			mActivity.setLoadingVisible(false, false);
			return;
		}

		if (book != null) {
			mBook = book;
		}

		if (mBook == null) { // Non ho un menu da mostrare
			libroND.setVisibility(View.VISIBLE);
			libroContainer.setVisibility(View.GONE);
			mActivity.setLoadingVisible(false, false);
			return;
		}

		if (mBook.getTitle() == null || mBook.getTitle().isEmpty()) {
			titoloCard.setVisibility(View.GONE);
		} else {
			titoloCard.setVisibility(View.VISIBLE);
			titoloView.setText(mBook.getTitle());
		}

		if (mBook.getAuthor() == null || mBook.getAuthor().isEmpty()) {
			autoreCard.setVisibility(View.GONE);
		} else {
			autoreCard.setVisibility(View.VISIBLE);
			autoreView.setText(mBook.getAuthor());
		}

		if (mBook.getPosition() == null || mBook.getPosition().isEmpty()) {
			posizioneCard.setVisibility(View.GONE);
		} else {
			posizioneCard.setVisibility(View.VISIBLE);
			posizioneView.setText(mBook.getPosition());
		}

		if (mBook.getEdition() == null || mBook.getEdition().isEmpty()) {
			edizioneCard.setVisibility(View.GONE);
		} else {
			edizioneCard.setVisibility(View.VISIBLE);
			edizioneView.setText(mBook.getEdition());
		}

		if (mBook.getPublication() == null || mBook.getPublication().isEmpty()) {
			pubblicazioneCard.setVisibility(View.GONE);
		} else {
			pubblicazioneCard.setVisibility(View.VISIBLE);
			pubblicazioneView.setText(mBook.getPublication());
		}

		if (mBook.getDescr() == null || mBook.getDescr().isEmpty()) {
			descrizioneCard.setVisibility(View.GONE);
		} else {
			descrizioneCard.setVisibility(View.VISIBLE);
			descrizioneView.setText(mBook.getDescr());
		}

		if (mBook.getSeries() == null || mBook.getSeries().isEmpty()) {
			serieCard.setVisibility(View.GONE);
		} else {
			serieCard.setVisibility(View.VISIBLE);
			serieView.setText(mBook.getSeries());
		}

		if (mBook.getLang() == null || mBook.getLang().isEmpty()) {
			linguaCard.setVisibility(View.GONE);
		} else {
			linguaCard.setVisibility(View.VISIBLE);
			linguaView.setText(mBook.getLang());
		}

		if (mBook.getSubject() == null || mBook.getSubject().isEmpty()) {
			soggettoCard.setVisibility(View.GONE);
		} else {
			soggettoCard.setVisibility(View.VISIBLE);
			soggettoView.setText(mBook.getSubject());
		}

		if (mBook.getCdd() == null || mBook.getCdd().isEmpty()) {
			cddCard.setVisibility(View.GONE);
		} else {
			cddCard.setVisibility(View.VISIBLE);
			cddView.setText(mBook.getCdd());
		}

		if (mBook.getIsbn() == null || mBook.getIsbn().isEmpty()) {
			isbnCard.setVisibility(View.GONE);
		} else {
			isbnCard.setVisibility(View.VISIBLE);
			isbnView.setText(mBook.getIsbn());
		}

		libroND.setVisibility(View.GONE);
		libroContainer.setVisibility(View.VISIBLE);

		mActivity.setLoadingVisible(false, false);
	}

	@Override
	public void actionRefresh() {
		getLibri(true);
	}

	@Override
	public int getTitleResId() {
		return R.string.dettagli_libro;
	}

}
