package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.TimetableDB;
import it.fdev.unisaconnect.data.TimetableSubject.Lesson;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentTimetable extends MySimpleFragment {

	RelativeLayout containerRL;
	LinearLayout hContainer;
	private int[] dayXLoc = new int[5];
	private View[] daysList = new View[5];
	private int[] timeYLoc = new int[11];
	private View[] timesList = new View[11];
	private float daysWidth = 0;
	private float hoursHeight = 0;
	private float minHeight = 0;
	private TimetableDB ttDB;
	private ArrayList<Lesson> lessonList;

	private final int[] dayIDs = new int[] { R.id.d1, R.id.d2, R.id.d3, R.id.d4, R.id.d5 };
//	private final int[] timeIDs = new int[] { R.id.h8, R.id.h9, R.id.h10, R.id.h11, R.id.h12, R.id.h13, R.id.h14, R.id.h15, R.id.h16, R.id.h17, R.id.h18 };
	private final int[] sepIDs = new int[] { R.id.sh8, R.id.sh9, R.id.sh10, R.id.sh11, R.id.sh12, R.id.sh13, R.id.sh14, R.id.sh15, R.id.sh16, R.id.sh17, R.id.sh18 };

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_timetable1, container, false);
		ttDB = new TimetableDB(activity);
		ttDB.open();
		return mainView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		containerRL = ((RelativeLayout) view.findViewById(R.id.timetable_container));
		hContainer = ((LinearLayout) view.findViewById(R.id.hcontainer));
		final int sdk = android.os.Build.VERSION.SDK_INT;
		// Wait for items loaded to take the needed position measures
		ViewTreeObserver vto = containerRL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {
				if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					containerRL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					containerRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				setMeasures();
				loadLessons();

				try {
					if (sdk > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
						/*
						 * Quando si passa dall'activity di aggiunta lezione a questa con la tastiera aperta le misure vengono prese comprensive di tastiera. 
						 * Vanno riprese una volta che la tastiera viene chiusa dal sistema. 
						 * Come fare per Gingerbread?
						 */
						view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
							@Override
							public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
								setMeasures();
								loadLessons();
							}
						});
					}
				} catch (Exception e) {
					// Non ancora pronto per prendere le misure
				}
			}
		});
	}

	public void setMeasures() {
		int xOffset = activity.findViewById(dayIDs[0]).getLeft();
		daysWidth = (containerRL.getWidth() - xOffset) / ((float) daysList.length);
		for (int i = 0; i < dayIDs.length; i++) {
			daysList[i] = activity.findViewById(dayIDs[i]);
			dayXLoc[i] = daysList[i].getLeft();
		}

		int yOffset = activity.findViewById(sepIDs[0]).getTop();
		hoursHeight = (containerRL.getHeight() - yOffset) / ((float) timeYLoc.length);
		minHeight = hoursHeight / (float) 60;
		for (int i = 0; i < sepIDs.length; i++) {
			timesList[i] = activity.findViewById(sepIDs[i]);
			timeYLoc[i] = timesList[i].getTop();// + (timesList[i].getHeight()/2);
			
//			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) hContainer.getLayoutParams();
//	        p.setMargins(0, Math.round(-hoursHeight/2), 0, Math.round(hoursHeight/2));
//	        hContainer.setLayoutParams(p);
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
	public void addLesson(final Lesson lesson, int i) {
		String text = lesson.getSubjectName();
		if (text.length() > 10)
			text = text.substring(0, 8) + "...";
		text += "\n" + lesson.getRoom();
		if (lesson.getDay() < 0 || lesson.getDay() > 4) {
			Log.w(Utils.TAG, "Giorno non valido: " + lesson.getDay());
			return;
		}
		if (lesson.getStartHour() < 8 || lesson.getStartHour() > 19) {
			Log.w(Utils.TAG, "Ora non valida: " + lesson.getStartHour());
			return;
		}
		if (lesson.getDuration() < 30) {
			Log.w(Utils.TAG, "Durata non valida: " + lesson.getDuration());
			return;
		}
		
		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lessonView = (LinearLayout) layoutInflater.inflate(R.layout.timetable_lesson_view, null);
		TextView titleView = (TextView) lessonView.findViewById(R.id.title);
		TextView descriptionView = (TextView) lessonView.findViewById(R.id.description);
		
		lessonView.setTag(i);
		lessonView.setBackgroundColor(lesson.getColor());
		
		String title = lesson.getSubjectName();
		if (title.length() > 10)
			title = title.substring(0, 8) + "...";
		titleView.setText(title);
		
		if (lesson.getRoom().isEmpty()) {
			descriptionView.setVisibility(View.GONE);
		} else {
			descriptionView.setText(lesson.getRoom());
		}
		
		lessonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTimetableAddLesson addLessonFragment = new FragmentTimetableAddLesson();
				addLessonFragment.editLesson(lesson);
				activity.switchContent(addLessonFragment);
			}
		});

//		TextView textView = new TextView(activity);
//		textView.setTextColor(0xFFFFFFFF);
//		textView.setTextSize(14f);
//		textView.setBackgroundColor(lesson.getColor());
//		textView.setGravity(Gravity.CENTER);
//		textView.setText(text);
//		textView.setTag(i);
//		textView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				FragmentTimetableAddLesson addLessonFragment = new FragmentTimetableAddLesson();
//				addLessonFragment.editLesson(lesson);
//				activity.switchContent(addLessonFragment);
//			}
//		});

		int marginTop = timesList[lesson.getStartHour() - 8].getTop() + (int) (minHeight * lesson.getStartMinutes());
		int marginLeft = daysList[lesson.getDay()].getLeft();
		int height = (int) (minHeight * lesson.getDuration());
		int width = (int) daysWidth;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.setMargins(marginLeft, marginTop, 0, 0);
		lessonView.setLayoutParams(params);

		containerRL.addView(lessonView);
	}

	private void loadLessons() {
		lessonList = ttDB.getLessons();
		for (int i = 0; i < lessonList.size(); i++) {
			addLesson(lessonList.get(i), i);
		}
	}

	@Override
	public void actionAdd() {
		FragmentTimetableAddLesson addLessonFragment = new FragmentTimetableAddLesson();
		activity.switchContent(addLessonFragment);
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_add_button);
		return actionsToShow;
	}
	
	@Override
	public int getTitleResId() {
		return R.string.orari_lezioni;
	}

}
