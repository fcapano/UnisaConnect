package it.fdev.unisaconnect;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Libretto;
import it.fdev.unisaconnect.data.Libretto.CorsoLibretto;
import it.fdev.unisaconnect.data.LibrettoDB;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyDateUtils;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LineGraph.OnPointClickedListener;
import com.echo.holographlibrary.LinePoint;

public class FragmentLibretto extends MySimpleFragment {

	private SharedPrefDataManager mDataManager;
	private boolean alreadyStarted = false;
	private LibrettoDB librettoDB;
	private boolean sortByName = true;
	private boolean isUpdatingAfterResort = false;

	private LinearLayout riepilogoContainerView;
	private LinearLayout listaCorsiView;

	private TextView avgWeightedView;
	private TextView avgArithView;
	private TextView baseMarkView;

	private View librettoLineGraphContainer;
	private LineGraph librettoLineGraph;
	private LinearLayout librettoGraphEsameContainerView;
	private TextView librettoGraphEsameNomeView;
	private TextView librettoGraphEsameSuperatoView;

	private TextView librettoNDView;
	private TextView lastUpdateTextView;
	private View lastUpdateIconView;
	private View sorterButtonView;
	private View sorterIconView;
	private ProgressBar sorterLoadingView;
	private TextView sorterTextView;

	private Comparator<CorsoLibretto> sorterCorsiByName = new Comparator<CorsoLibretto>() {
		// Compara per [voto esiste] [nome]
		public int compare(CorsoLibretto c1, CorsoLibretto c2) {
			String nome1 = c1.getName();
			String nome2 = c2.getName();

			int voto1 = 1;
			if (c1.getMark().isEmpty()) {
				voto1 = -1;
			}
			int voto2 = 1;
			if (c2.getMark().isEmpty()) {
				voto2 = -1;
			}

			if (voto1 == voto2) {
				return nome1.compareTo(nome2);
			}
			if (voto1 < 0) {
				return 1;
			} else if (voto2 < 0) {
				return -1;
			}
			return nome1.compareTo(nome2);
		}
	};

	private IntentFilter mIntentFilter = new IntentFilter();
	private final BroadcastReceiver mHandlerBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onNewBroadcast(context, intent);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_libretto, container, false);
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

		mIntentFilter.addAction(Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO);

		listaCorsiView = (LinearLayout) view.findViewById(R.id.lista_corsi);

		riepilogoContainerView = (LinearLayout) view.findViewById(R.id.riepilogo_libretto);
		avgWeightedView = (TextView) riepilogoContainerView.findViewById(R.id.avg_weighted);
		avgArithView = (TextView) riepilogoContainerView.findViewById(R.id.avg_arithmetic);
		baseMarkView = (TextView) riepilogoContainerView.findViewById(R.id.base_mark);

		librettoLineGraphContainer = view.findViewById(R.id.libretto_line_graph_container);
		librettoLineGraph = (LineGraph) view.findViewById(R.id.libretto_line_graph);
		librettoGraphEsameContainerView = (LinearLayout) view.findViewById(R.id.libretto_graph_esame_container);
		librettoGraphEsameNomeView = (TextView) librettoGraphEsameContainerView.findViewById(R.id.libretto_graph_esame_nome);
		librettoGraphEsameSuperatoView = (TextView) librettoGraphEsameContainerView.findViewById(R.id.libretto_graph_esame_superato);
		// librettoGraphEsameVotoView = (TextView) librettoGraphEsameContainerView.findViewById(R.id.libretto_graph_esame_voto);

		librettoNDView = (TextView) view.findViewById(R.id.libretto_vuoto);
		lastUpdateTextView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = view.findViewById(R.id.last_update_icon);
		sorterButtonView = view.findViewById(R.id.sorter_button);
		sorterIconView = view.findViewById(R.id.sorter_icon);
		sorterLoadingView = (ProgressBar) view.findViewById(R.id.sorter_loading);
		sorterTextView = (TextView) view.findViewById(R.id.sorter_text);
		sorterButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isUpdatingAfterResort) {
					return;
				}
				isUpdatingAfterResort = true;
				sorterIconView.setVisibility(View.GONE);
				// sorterTextView.setVisibility(View.GONE);
				sorterLoadingView.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {	// Deve essere eseguito un un nuovo thread perche altrimenti il redraw del layout viene fatto a task finito
					@Override				// e quindi l'amimazione non verrebbe mostrata (sorterLoadingView.show -> sorterLoadingView.hide -> draw)
					public void run() {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								CharSequence text = "";
								sortByName = !sortByName;
								mDataManager.setLibrettoSortByName(sortByName);
								if (sortByName) {
									text = resources.getText(R.string.ordina_per_nome);
								} else {
									text = resources.getText(R.string.ordina_per_data);
								}
								sorterTextView.setText(text);
								mostraCorsi();
								sorterLoadingView.setVisibility(View.GONE);
								sorterIconView.setVisibility(View.VISIBLE);
								// sorterTextView.setVisibility(View.VISIBLE);
								isUpdatingAfterResort = false;
							}
						});
					}
				}).start();
			}
		});

		sortByName = mDataManager.getLibrettoSortByName();
		CharSequence text = "";
		if (sortByName) {
			text = resources.getText(R.string.ordina_per_nome);
		} else {
			text = resources.getText(R.string.ordina_per_data);
		}
		sorterTextView.setText(text);
		
		sorterLoadingView.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

		librettoDB = new LibrettoDB(mActivity);
		librettoDB.open();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getLibretto(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerBroadcast);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			librettoDB.close();
		} catch (Exception e) {
			// Failed to close database
		}
	}

	public void onNewBroadcast(Context context, Intent intent) {
		try {
			Log.d(Utils.TAG, "BROADCAST RECEIVED: " + intent.getAction());
			if (Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO.equals(intent.getAction())) {
				LoadStates state = (LoadStates) intent.getSerializableExtra("status");
				switch (state) {
				case FINISHED:
					mostraCorsi();
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
		getLibretto(true);
	}

	public void getLibretto(boolean force) {
		if (!isAdded()) {
			return;
		}

		// librettoDB not created -> onViewCreated stopped at data check
		if (librettoDB == null) {
			return;
		}

		// Lo scraper è in esecuzione
		if (Esse3ScraperService.isRunning) {
			return;
		}

		mActivity.setLoadingVisible(true, true);

		Libretto libretto = librettoDB.getLibretto();
		// Il libretto c'è e non devo forzare l'aggiornamento
		if (!force && libretto.getCorsi().size() > 0) {
			mostraCorsi();
			mActivity.setLoadingVisible(false, false);
			alreadyStarted = true;
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(mActivity)) {
			Utils.goToInternetError(mActivity, this);
			return;
		}

		if (force || !alreadyStarted) {
			alreadyStarted = true;
			mActivity.startService(new Intent(mActivity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO));
		} else {
			riepilogoContainerView.setVisibility(View.GONE);
			librettoNDView.setVisibility(View.VISIBLE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
	}

	public void mostraCorsi() {
		if (!isAdded()) {
			return;
		}

		final Libretto libretto = librettoDB.getLibretto();
		ArrayList<CorsoLibretto> corsi = libretto.getCorsi();
		if (corsi.size() == 0) {
			riepilogoContainerView.setVisibility(View.GONE);
			librettoNDView.setVisibility(View.VISIBLE);
			return;
		}

		aggiornaRiepilogo(libretto);

		String updateText = "";
		if (libretto != null) {
			updateText = MyDateUtils.getLastUpdateString(mActivity, libretto.getFetchTime().getTime(), false);
		}
		if (!updateText.isEmpty()) {
			lastUpdateTextView.setText(updateText);
			lastUpdateTextView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateTextView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		}

		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listaCorsiView.removeAllViews();

		ArrayList<CorsoLibretto> corsiSorted = (ArrayList<CorsoLibretto>) corsi.clone();
		if (sortByName) {
			Collections.sort(corsiSorted, sorterCorsiByName);
		} else {
			Collections.sort(corsiSorted);
		}

		for (CorsoLibretto corso : corsiSorted) {
			View rowView;

			if (corso.getMark().isEmpty()) {
				rowView = inflater.inflate(R.layout.libretto_row_with_slider, listaCorsiView, false);
			} else {
				rowView = inflater.inflate(R.layout.libretto_row, listaCorsiView, false);
			}

			TextView nameView = (TextView) rowView.findViewById(R.id.course_name);
			TextView cfuView = (TextView) rowView.findViewById(R.id.course_credits);
			final TextView markView = (TextView) rowView.findViewById(R.id.course_mark);

			final String name = corso.getName();
			nameView.setText(name);
			cfuView.setText(corso.getCFU() + " CFU");
			if (corso.getMark().isEmpty()) {
				markView.setText("ND");

				SeekBar seekBarView = (SeekBar) rowView.findViewById(R.id.course_seekbar);
				seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						String newMark = "";
						if (progress == 0) {
							newMark = "ND";
						} else {
							markView.setText(Integer.toString(progress + 17));
							newMark = Integer.toString(progress + 17);
						}
						markView.setText(newMark);
						libretto.getCorso(name).setMark(newMark);
						aggiornaRiepilogo(libretto);
					}
				});
			} else {
				TextView dateView = (TextView) rowView.findViewById(R.id.course_date);
				dateView.setText(corso.getDate());
				markView.setText(corso.getMark());
			}

			listaCorsiView.addView(rowView);
		}

		Line line = new Line();
		int position = 1;
		int minVoto = Integer.MAX_VALUE, maxVoto = Integer.MIN_VALUE;

		ArrayList<CorsoLibretto> corsiByDate = (ArrayList<CorsoLibretto>) corsi.clone();
		Collections.sort(corsiByDate);
		final ArrayList<CorsoLibretto> coursesInsertedInGraph = new ArrayList<Libretto.CorsoLibretto>();
		for (CorsoLibretto corso : corsiByDate) {
			int voto;
			try {
				voto = Integer.parseInt(corso.getMark());
				minVoto = Math.min(voto, minVoto);
				maxVoto = Math.max(voto, maxVoto);
			} catch (NumberFormatException e) {
				// Esame non superato o ad idoneità o 30L
				if (corso.getMark().equalsIgnoreCase("30L")) {
					voto = 30;
				} else {
					continue;
				}
			}
			LinePoint point = new LinePoint(position, voto);
			line.addPoint(point);
			position++;
			coursesInsertedInGraph.add(corso);
		}

		if (line.getSize() < 2) {
			librettoLineGraphContainer.setVisibility(View.GONE);
		} else {
			line.setColor(Color.parseColor("#F4842D"));
			librettoLineGraph.removeAllLines();
			librettoLineGraph.addLine(line);
			librettoLineGraph.setRangeY(minVoto - 1, maxVoto + 1);
			librettoLineGraph.setOnPointClickedListener(new OnPointClickedListener() {
				@Override
				public void onClick(int lineIndex, int pointIndex) {
					CorsoLibretto corso = coursesInsertedInGraph.get(pointIndex);
					librettoGraphEsameContainerView.setVisibility(View.VISIBLE);
					librettoGraphEsameNomeView.setText(corso.getName());
					librettoGraphEsameSuperatoView.setText(getString(R.string.superato_il_con, corso.getDate(), corso.getMark()));
				}
			});
			// librettoLineGraph.setPointClicked(0,coursesInsertedInGraph.size()-1);
		}
		riepilogoContainerView.setVisibility(View.VISIBLE);
		librettoNDView.setVisibility(View.GONE);
		mActivity.setLoadingVisible(false, false);
	}

	private void aggiornaRiepilogo(Libretto libretto) {
		String formatType = "%.3f";

		float mediaAritmetica = libretto.getMediaAritmetica();
		avgArithView.setText(String.format(formatType, mediaAritmetica));
		float mediaPesata = libretto.getMediaPesata();
		avgWeightedView.setText(String.format(formatType, mediaPesata));
		long baseMark = Math.round((mediaPesata * 110.0) / 30.0);
		baseMarkView.setText(Long.toString(baseMark));
	}

	@Override
	public int getTitleResId() {
		return R.string.libretto;
	}

}
