package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.TimetableDB;
import it.fdev.unisaconnect.data.TimetableSubject.Lesson;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimetableFragment extends MySimpleFragment {

	RelativeLayout containerRL;
	// private View daysContainer;
	// private View hoursContainer;
	private int[] dayXLoc = new int[5];
	private View[] daysList = new View[5];
	private int[] timeYLoc = new int[12];
	private View[] timesList = new View[12];
	private float daysWidth = 0;
	private float hoursHeight = 0;
	private float minHeight = 0;
//	private float dpPixel;
	private TimetableDB ttDB;
	private boolean isEditMode = false;
	private ArrayList<Lesson> lessonList;

	private final int[] dayIDs = new int[] { R.id.d1, R.id.d2, R.id.d3, R.id.d4, R.id.d5 };
	private final int[] timeIDs = new int[] { R.id.h8, R.id.h9, R.id.h10, R.id.h11, R.id.h12, R.id.h13, R.id.h14, R.id.h15, R.id.h16, R.id.h17, R.id.h18, R.id.h19 };

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.timetable1, container, false);
		ttDB = new TimetableDB(activity);
		ttDB.open();
//		dpPixel = Utils.convertDpToPixel(1, activity);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		 * final GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
		 * 
		 * @Override public void onLongPress(MotionEvent e) { Log.d(Utils.TAG, "Longpress"); }
		 * 
		 * @Override public boolean onSingleTapConfirmed(MotionEvent e) { Log.d(Utils.TAG, "Single tap"); cellClicked(e.getX(), e.getY());
		 * 
		 * addLesson("Algoritmi e Strutture Dati", 0, 10, 30, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 1, 10, 30, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 2, 10, 30, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 3, 10, 30, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 8, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 9, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 10, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 11, 0, 60, resources.getColor(R.color.holo_blue));
		 * addLesson("Algoritmi e Strutture Dati", 4, 12, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 13, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 14, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 15, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 16, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 17, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 18, 0, 60, resources.getColor(R.color.holo_blue)); addLesson("Algoritmi e Strutture Dati", 4, 19, 0, 60, resources.getColor(R.color.holo_blue));
		 * 
		 * return super.onSingleTapUp(e); }
		 * 
		 * @Override public boolean onDoubleTap(MotionEvent e) { Log.d(Utils.TAG, "Double tap"); return super.onDoubleTap(e); } // Workaround for gestures not recognized in frames // http://stackoverflow.com/questions/11421368/android-fragment-oncreateview-with-gestures
		 * 
		 * @Override public boolean onDown(MotionEvent e) { return true; } });
		 * 
		 * containerRL.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { return gestureDetector.onTouchEvent(event); } });
		 */

		containerRL = ((RelativeLayout) activity.findViewById(R.id.timetable_container));

		// Wait for items loaded to take the needed position measures
		ViewTreeObserver vto = containerRL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {
				int sdk = android.os.Build.VERSION.SDK_INT;
				if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					containerRL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					containerRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				setMeasures();
				loadLessons();
			}
		});
	}

	public int[] cellClicked(float x, float y) {
		int day;
		int hour;

		for (day = dayXLoc.length - 1; day >= 0; day--) {
			if (x > dayXLoc[day]) {
				break;
			}
		}
		for (hour = timeYLoc.length - 1; hour >= 0; hour--) {
			if (y > timeYLoc[hour]) {
				break;
			}
		}
		return new int[] { day, hour };
	}

	public void setMeasures() {
		int xOffset = activity.findViewById(dayIDs[0]).getLeft();
		daysWidth = (containerRL.getWidth() - xOffset) / ((float) daysList.length);
		for (int i = 0; i < dayIDs.length; i++) {
			daysList[i] = activity.findViewById(dayIDs[i]);
			dayXLoc[i] = daysList[i].getLeft();
		}

		int yOffset = activity.findViewById(timeIDs[0]).getTop();
		hoursHeight = (containerRL.getHeight() - yOffset) / ((float) timeYLoc.length);
		minHeight = hoursHeight / (float) 60;
		for (int i = 0; i < timeIDs.length; i++) {
			timesList[i] = activity.findViewById(timeIDs[i]);
			timeYLoc[i] = timesList[i].getTop();
		}
	}

	/**
	 * 
	 * @param text
	 *            Il testo da visualizzare
	 * @param day
	 *            Il giorno della settimana, dove 0 è lunedi e 4 è venerdi
	 * @param hour
	 *            L'ora di inizio della lezione, compresa tra le 8 e le 19
	 * @param minutes
	 *            I minuti dell'ora di inizio della lezione
	 * @param duration
	 *            La durata della lezione in minuti
	 */
	public void addLesson(Lesson lesson, int i) {
		String text = lesson.getSubjectName();
		if (text.length() > 10)
			text = text.substring(0, 8) + "...";
		text += "\n" + lesson.getRoom();
		Log.d(Utils.TAG, "Aggiungo lezione: " + text);
		if (lesson.getDay() < 0 || lesson.getDay() > 4) {
			Log.d(Utils.TAG, "Giorno non valido: " + lesson.getDay());
			return;
		}
		if (lesson.getStartHour() < 8 || lesson.getStartHour() > 19) {
			Log.d(Utils.TAG, "Ora non valida: " + lesson.getStartHour());
			return;
		}
		Log.d(Utils.TAG, "Start: " + lesson.getStartHour() + "   |   End: " + lesson.getEndHour());
		if (lesson.getDuration() < 30) {
			Log.d(Utils.TAG, "Durata non valida: " + lesson.getDuration());
			return;
		}

		TextView textView = new TextView(activity);
		textView.setTextColor(0xFFFFFFFF);
		textView.setTextSize(14f);
		textView.setBackgroundColor(lesson.getColor());
		textView.setGravity(Gravity.CENTER);
		textView.setText(text);
		textView.setTag(i);
		textView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return lessonLongClicked(v);
			}
		});

		int marginTop = timesList[lesson.getStartHour() - 8].getTop() + (int) (minHeight * lesson.getStartMinutes());
		int marginLeft = daysList[lesson.getDay()].getLeft();
		int height = (int) (minHeight * lesson.getDuration());
		int width = (int) daysWidth;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.setMargins(marginLeft, marginTop, 0, 0);
		textView.setLayoutParams(params);

		containerRL.addView(textView);
	}

	private void loadLessons() {
		lessonList = ttDB.getLessons();
		for (int i = 0; i < lessonList.size(); i++) {
			addLesson(lessonList.get(i), i);
		}
	}

	private boolean lessonLongClicked(View v) {
		if (isEditMode) {
			int index = (Integer) v.getTag();
			Lesson lesson = lessonList.get(index);
			ttDB.deleteLesson(lesson);
		}
		return true;
	}

	@Override
	public void actionAdd() {
		activity.switchContent(new TimetableAddLessonFragment());
	}

	@Override
	public void actionEdit() {
		isEditMode = true;
		activity.hideActions();
		setVisibleActions();
	}

	@Override
	public void actionAccept() {
		isEditMode = false;
		activity.hideActions();
		setVisibleActions();
	}

	@Override
	public void setVisibleActions() {
		if (isEditMode) {
			activity.setActionAddVisible(true);
			activity.setActionAcceptVisible(true);
		} else {
			activity.setActionEditVisible(true);
		}
	}

	@Override
	public void actionRefresh() {
	}

}
