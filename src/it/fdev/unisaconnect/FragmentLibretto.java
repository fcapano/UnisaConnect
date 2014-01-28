package it.fdev.unisaconnect;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.scraper.esse3.Esse3ScraperService;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.Libretto;
import it.fdev.unisaconnect.data.Libretto.CorsoLibretto;
import it.fdev.unisaconnect.data.LibrettoDB;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LineGraph.OnPointClickedListener;
import com.echo.holographlibrary.LinePoint;

public class FragmentLibretto extends MySimpleFragment {

	private boolean alreadyStarted = false;
	private LibrettoDB librettoDB;

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
	private TextView lastUpdateView;
	private View lastUpdateIconView;

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
		SharedPrefDataManager mDataManager = new SharedPrefDataManager(activity);
		if (!mDataManager.loginDataExists()) { // Non sono memorizzati i dati utente
			Utils.createAlert(activity, getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			return;
		}
		activity.setLoadingVisible(true, true);

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
//		librettoGraphEsameVotoView = (TextView) librettoGraphEsameContainerView.findViewById(R.id.libretto_graph_esame_voto);

		librettoNDView = (TextView) view.findViewById(R.id.libretto_vuoto);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (View) view.findViewById(R.id.last_update_icon);

		librettoDB = new LibrettoDB(activity);
		librettoDB.open();

		// Line l = new Line();
		// LinePoint p = new LinePoint();
		// p.setX(0);
		// p.setY(5);
		// l.addPoint(p);
		// p = new LinePoint();
		// p.setX(8);
		// p.setY(8);
		// l.addPoint(p);
		// p = new LinePoint();
		// p.setX(10);
		// p.setY(4);
		// l.addPoint(p);
		// l.setColor(Color.parseColor("#FFBB33"));
		// LineGraph li = (LineGraph) view.findViewById(R.id.libretto_line_graph);
		// li.addLine(l);
		// li.setRangeY(0, 10);
		// li.setLineToFill(0);
		//
		// PieGraph pg = (PieGraph) view.findViewById(R.id.graph1);
		// // pg.setThickness(150);
		// // pg.setFull(true);
		// PieSlice slice = new PieSlice();
		// slice.setColor(Color.parseColor("#FF0000"));
		// slice.setValue(13);
		// pg.addSlice(slice);
		// slice = new PieSlice();
		// slice.setColor(Color.parseColor("#00FF00"));
		// slice.setValue(12);
		// pg.addSlice(slice);
		// slice = new PieSlice();
		// slice.setColor(Color.parseColor("#0000FF"));
		// slice.setValue(25);
		// pg.addSlice(slice);
		// slice = new PieSlice();
		// slice.setColor(Color.parseColor("#AA66CC"));
		// slice.setValue(50);
		// pg.addSlice(slice);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.registerReceiver(mHandlerBroadcast, mIntentFilter);
		getLibretto(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterReceiver(mHandlerBroadcast);
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
					Utils.createAlert(activity, activity.getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
					break;
				case UNKNOWN_PROBLEM:
				default:
					Utils.createAlert(activity, activity.getString(R.string.problema_di_connessione_generico), null, true);
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

		activity.setLoadingVisible(true, true);

		Libretto libretto = librettoDB.getLibretto();
		// Il libretto c'è e non devo forzare l'aggiornamento
		if (!force && libretto.getCorsi().size() > 0) {
			mostraCorsi();
			activity.setLoadingVisible(false, false);
			alreadyStarted = true;
			return;
		}

		// Se non c'è internet rimando al fragment di errore
		if (!Utils.hasConnection(activity)) {
			Utils.goToInternetError(activity, this);
			return;
		}

		if (force || !alreadyStarted) {
			alreadyStarted = true;
			activity.startService(new Intent(activity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO));
		} else {
			riepilogoContainerView.setVisibility(View.GONE);
			librettoNDView.setVisibility(View.VISIBLE);
			activity.setLoadingVisible(false, false);
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

		if (libretto.getFetchTime().getTime() > 0) {
			String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(libretto.getFetchTime());
			String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(libretto.getFetchTime());
			String updateText = getString(R.string.aggiornato_il_alle, dateFirstPart, dateSecondPart);
			lastUpdateView.setText(updateText);
			lastUpdateView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		}

		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listaCorsiView.removeAllViews();
		
		ArrayList<CorsoLibretto> corsiByState = (ArrayList<CorsoLibretto>) corsi.clone();
		Collections.sort(corsiByState, new Comparator<CorsoLibretto>() {
			// Compara per [voto esiste] [nome]
			@Override
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
		});
		
		for (CorsoLibretto corso : corsiByState) {
			View rowView;

			if (corso.getMark().isEmpty()) {
				rowView = inflater.inflate(R.layout.libretto_row_with_slider, null);
			} else {
				rowView = inflater.inflate(R.layout.libretto_row, null);
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
							markView.setText(Integer.toString(progress+17));
							newMark = Integer.toString(progress+17);
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
		int minVoto=Integer.MAX_VALUE, maxVoto=Integer.MIN_VALUE;

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
			librettoLineGraph.setRangeY(minVoto-1, maxVoto+1);
			librettoLineGraph.setOnPointClickedListener(new OnPointClickedListener() {
				@Override
				public void onClick(int lineIndex, int pointIndex) {
					CorsoLibretto corso = coursesInsertedInGraph.get(pointIndex);
					librettoGraphEsameContainerView.setVisibility(View.VISIBLE);
					librettoGraphEsameNomeView.setText(corso.getName());
					librettoGraphEsameSuperatoView.setText(getString(R.string.superato_il_con, corso.getDate(), corso.getMark()));
				}
			});
//			librettoLineGraph.setPointClicked(0,coursesInsertedInGraph.size()-1);
		}
		riepilogoContainerView.setVisibility(View.VISIBLE);
		librettoNDView.setVisibility(View.GONE);
		activity.setLoadingVisible(false, false);
	}
	
	private void aggiornaRiepilogo(Libretto libretto) {
		String formatType = "%.3f";
		
		float mediaAritmetica = libretto.getMediaAritmetica();
		avgArithView.setText(String.format(formatType, mediaAritmetica));
		float mediaPesata = libretto.getMediaPesata();
		avgWeightedView.setText(String.format(formatType, mediaPesata));
		double baseMark = Math.round( (mediaPesata * 110.0) / 30.0 );
		baseMarkView.setText(String.format("%.0f", baseMark));
	}

	@Override
	public int getTitleResId() {
		return R.string.libretto;
	}

}
