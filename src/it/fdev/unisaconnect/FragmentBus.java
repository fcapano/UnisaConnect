package it.fdev.unisaconnect;

import it.fdev.scraper.BusScraper;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusCorsa;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusTratta;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class FragmentBus extends MySimpleFragment {

	private LayoutInflater mLayoutInflater;

	private TextView cercaStazioneView;
	private View risultatiRicercaLabel;
	private LinearLayout risultatiRicerca;
	private LinearLayout tratteList;

	// private BusDB db;
	private BusScraper.CercaStazione cercaStazioneScraper;
	private BusScraper.GetTratteStazione tratteStazioneScraper;
	private ArrayList<String> mSearchResults = new ArrayList<String>();
	private ArrayList<BusTratta> mTratteResults = new ArrayList<BusTratta>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_bus, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		cercaStazioneView = (TextView) view.findViewById(R.id.cerca_destinazione);
		risultatiRicercaLabel = view.findViewById(R.id.risultati_ricerca_label);
		risultatiRicerca = (LinearLayout) view.findViewById(R.id.risultati_ricerca);
		tratteList = (LinearLayout) view.findViewById(R.id.tratte_list);

		cercaStazioneView.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				filterSearchResults();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// db = new BusDB(mActivity);
		// busScraper = new BusScraper();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public int getTitleResId() {
		return R.string.servizi_esse3;
	}

	private void filterSearchResults() {
		String testoStazione = cercaStazioneView.getText().toString().trim();
		if (testoStazione.isEmpty()) {
			risultatiRicercaLabel.setVisibility(View.GONE);
			risultatiRicerca.setVisibility(View.GONE);
			return;
		}
		risultatiRicerca.removeAllViews();

		try {
			if (cercaStazioneScraper != null) {
				cercaStazioneScraper.cancel(true);
			}
		} catch (Exception e) {
			Log.w(Utils.TAG, e);
		}
		Log.d(Utils.TAG, "---->  1");
		cercaStazioneScraper = new BusScraper.CercaStazione();
		cercaStazioneScraper.setCallerBusFragment(this);
		cercaStazioneScraper.execute(mActivity, testoStazione);

		Utils.expand(risultatiRicercaLabel);
		Utils.expand(risultatiRicerca);
	}

	public void showRisultatiRicerca(ArrayList<String> searhResults) {
		if (searhResults == null) {
			/** TODO **/
			return;
		}
		mSearchResults = searhResults;
		for (final String cStazione : mSearchResults) {
			LinearLayout stazioneView = (LinearLayout) mLayoutInflater.inflate(R.layout.bus_stazione_result, risultatiRicerca, false);
			TextView labelView = (TextView) stazioneView.findViewById(R.id.stazione);
			labelView.setText(cStazione);
			final FragmentBus fragment = this;
			risultatiRicerca.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						if (tratteStazioneScraper != null) {
							tratteStazioneScraper.cancel(true);
						}
					} catch (Exception e) {
						Log.w(Utils.TAG, e);
					}

					tratteStazioneScraper = new BusScraper.GetTratteStazione();
					tratteStazioneScraper.setCallerBusFragment(fragment);
					tratteStazioneScraper.execute(mActivity, cStazione);
				}
			});
			risultatiRicerca.addView(stazioneView);
		}
	}

	public void showTratteRicerca(ArrayList<BusTratta> tratteResults) {
		mTratteResults = tratteResults;
		tratteList.removeAllViews();
		for (BusTratta cTratta : mTratteResults) {
			View cTrattaView = getTrattaView(cTratta);
			tratteList.addView(cTrattaView);
		}
		tratteList.setVisibility(View.VISIBLE);
	}

	private View getTrattaView(BusTratta tratta) {
		LinearLayout trattaView = (LinearLayout) mLayoutInflater.inflate(R.layout.card_bus_tratta, null);
		TextView compagniaView = (TextView) trattaView.findViewById(R.id.compagnia);
		TextView capolineaVersoUniView = (TextView) trattaView.findViewById(R.id.capolinea_versouni);
		TableLayout tableVersoUniView = (TableLayout) trattaView.findViewById(R.id.tratta_verso_uni_table);

		compagniaView.setText(tratta.getCompagnia());
		capolineaVersoUniView.setText(tratta.getCapolinea());
		tableVersoUniView.removeAllViews();

//		View cellView;
//		List<BusCorsa> corse;
//		corse = tratta.getCorseVersoUni();
//		TableRow headerRow = new TableRow(mActivity);
//		cellView = createTableCell("Fermata", true);
//		headerRow.addView(cellView);
//		for (BusCorsa cCorsa : corse) {
//			String giorni = cCorsa.getGiorni();
//			cellView = createTableCell(giorni, true);
//			headerRow.addView(cellView);
//		}
//		tableVersoUniView.addView(headerRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//
//		List<String> stazioni = tratta.getStazioniVersoUni();
//		for (String cStazione : stazioni) {
//			TableRow cRow = new TableRow(mActivity);
//			cellView = createTableCell(cStazione, false);
//			cRow.addView(cellView);
//			for (BusCorsa cCorsa : corse) {
//				int ora = cCorsa.getOraFermata(cStazione);
//				String testo = "--";
//				if (ora >= 0) {
//					testo = ora + "";
//				}
//				cellView = createTableCell(testo, false);
//				cRow.addView(cellView);
//			}
//			tableVersoUniView.addView(cRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//		}

		return trattaView;
	}

	private View createTableCell(String text, boolean isBold) {
		TextView textView = new TextView(mActivity);
		textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		if (isBold) {
			textView.setTypeface(null, Typeface.BOLD);
		}
		textView.setPadding(3, 0, 3, 0);
		textView.setText(text);
		return textView;
	}

	@Override
	public void onDestroy() {
		// db.close();
		super.onDestroy();
	}

}