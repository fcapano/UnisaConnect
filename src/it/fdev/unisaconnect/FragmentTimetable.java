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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentTimetable extends MySimpleFragment {

	private RelativeLayout lessonsContainer;
	private int containerHeight = 0;
	private OnGlobalLayoutListener mLayoutObserver;
	private boolean observerRunning = false;
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
	private boolean measuresTaken = false;

	private final int[] dayIDs = new int[] { R.id.d1, R.id.d2, R.id.d3, R.id.d4, R.id.d5 };
	// private final int[] timeIDs = new int[] { R.id.h8, R.id.h9, R.id.h10, R.id.h11, R.id.h12, R.id.h13, R.id.h14, R.id.h15, R.id.h16, R.id.h17, R.id.h18 };
	private final int[] sepIDs = new int[] { R.id.sh8, R.id.sh9, R.id.sh10, R.id.sh11, R.id.sh12, R.id.sh13, R.id.sh14, R.id.sh15, R.id.sh16, R.id.sh17, R.id.sh18 };

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_timetable1, container, false);
		ttDB = new TimetableDB(mActivity);
		ttDB.open();
		return mainView;
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		lessonsContainer = ((RelativeLayout) view.findViewById(R.id.lessons_container));
		hContainer = ((LinearLayout) view.findViewById(R.id.hcontainer));

		// Wait for items loaded to take the needed position measures
		mLayoutObserver = new OnGlobalLayoutListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onGlobalLayout() {
				if (observerRunning) {
					return;
				}
				observerRunning = true;
				int newContainerHeight = lessonsContainer.getHeight();
				if (newContainerHeight != containerHeight) {
					containerHeight = newContainerHeight;
					setMeasures();
					loadLessons();
				}
				observerRunning = false;
			}
		};

		resumeLayoutObserver();
	}

	@Override
	public void onResume() {
		super.onResume();
		resumeLayoutObserver();
		try {
			if (measuresTaken) {
				loadLessons();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		pauseLayoutObserver();
	}

	public void resumeLayoutObserver() {
		if (mLayoutObserver == null) {
			return;
		}
		ViewTreeObserver vto = lessonsContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(mLayoutObserver);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void pauseLayoutObserver() {
		if (mLayoutObserver == null) {
			return;
		}
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			lessonsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutObserver);
		} else {
			lessonsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutObserver);
		}
	}

	public void setMeasures() {
		int xOffset = mActivity.findViewById(dayIDs[0]).getLeft();
		daysWidth = (lessonsContainer.getWidth() - xOffset) / ((float) daysList.length);
		for (int i = 0; i < dayIDs.length; i++) {
			daysList[i] = mActivity.findViewById(dayIDs[i]);
			dayXLoc[i] = daysList[i].getLeft();
		}

		int yOffset = mActivity.findViewById(sepIDs[0]).getTop();
		hoursHeight = (lessonsContainer.getHeight() - yOffset) / ((float) timeYLoc.length);
		minHeight = hoursHeight / (float) 60;
		for (int i = 0; i < sepIDs.length; i++) {
			timesList[i] = mActivity.findViewById(sepIDs[i]);
			timeYLoc[i] = timesList[i].getTop();// + (timesList[i].getHeight()/2);

			// RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) hContainer.getLayoutParams();
			// p.setMargins(0, Math.round(-hoursHeight/2), 0, Math.round(hoursHeight/2));
			// hContainer.setLayoutParams(p);
		}
		measuresTaken = true;
	}

	/**
	 * 
	 * @param nome
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

		LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lessonView = (LinearLayout) layoutInflater.inflate(R.layout.timetable_lesson_view, lessonsContainer, false);
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
				mActivity.switchContent(addLessonFragment);
			}
		});

		// TextView textView = new TextView(activity);
		// textView.setTextColor(0xFFFFFFFF);
		// textView.setTextSize(14f);
		// textView.setBackgroundColor(lesson.getColor());
		// textView.setGravity(Gravity.CENTER);
		// textView.setText(text);
		// textView.setTag(i);
		// textView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// FragmentTimetableAddLesson addLessonFragment = new FragmentTimetableAddLesson();
		// addLessonFragment.editLesson(lesson);
		// activity.switchContent(addLessonFragment);
		// }
		// });

		int marginTop = timesList[lesson.getStartHour() - 8].getTop() + (int) (minHeight * lesson.getStartMinutes());
		int marginLeft = daysList[lesson.getDay()].getLeft();
		int height = (int) (minHeight * lesson.getDuration());
		int width = (int) daysWidth;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.setMargins(marginLeft, marginTop, 0, 0);
		lessonView.setLayoutParams(params);

		lessonsContainer.addView(lessonView);
	}

	private void loadLessons() {
		lessonsContainer.removeAllViews();
		lessonList = ttDB.getLessons();
		for (int i = 0; i < lessonList.size(); i++) {
			addLesson(lessonList.get(i), i);
		}
	}

	@Override
	public void actionAdd() {
		FragmentTimetableAddLesson addLessonFragment = new FragmentTimetableAddLesson();
		mActivity.switchContent(addLessonFragment);
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
