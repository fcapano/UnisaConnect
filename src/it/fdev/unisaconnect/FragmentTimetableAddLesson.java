package it.fdev.unisaconnect;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.TimetableDB;
import it.fdev.unisaconnect.data.TimetableSubject.Lesson;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class FragmentTimetableAddLesson extends MySimpleFragment {

	private AutoCompleteTextView subjectName;
	private View choosenColorView;
	private LinearLayout colorLinearLayout;
	private LinearLayout addLesson;

	private LinearLayout lessonsParent;
	private LayoutInflater lInflater;

	private TimetableDB ttDB;

	private int[] colorsArray;
	private int color = 0;
	private final int minStartHour = 8;
	private final int maxStartHour = 18;
	private final int minEndHour = minStartHour + 1;
	private final int maxEndHour = maxStartHour + 1;

	private ArrayAdapter<String> roomsAdapter;

	private ArrayList<LessonEntry> lessonList = new ArrayList<LessonEntry>();

	private String lessonToEdit = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_timetable_add_lesson, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ttDB = new TimetableDB(activity);
		ttDB.open();

		subjectName = (AutoCompleteTextView) view.findViewById(R.id.subject_name);
		choosenColorView = view.findViewById(R.id.choosen_color);
		colorLinearLayout = (LinearLayout) view.findViewById(R.id.color_list);
		addLesson = (LinearLayout) view.findViewById(R.id.add_lesson);
		lessonsParent = (LinearLayout) view.findViewById(R.id.lesson_data_container);

		lInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		subjectName.requestFocus();

		TypedArray colorsResources = resources.obtainTypedArray(R.array.timetable_lesson_colors);
		colorsArray = new int[colorsResources.length()];
		for (int i = 0; i < colorsResources.length(); i++) {
			colorsArray[i] = resources.getColor(colorsResources.getResourceId(i, R.color.orange_dark));
		}
		colorsResources.recycle();

		int childcount = colorLinearLayout.getChildCount();
		for (int i = 0; i < childcount; i++) {
			final int indx = i;
			View v = colorLinearLayout.getChildAt(i);
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					color = colorsArray[indx];
					choosenColorView.setBackgroundColor(color);
				}
			});
			if (i == 0)
				color = colorsArray[i];
		}

		addLesson.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addLessonLayout(true);
			}
		});

		roomsAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, ttDB.getRoomNames());
		ArrayAdapter<String> subjectsAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, ttDB.getSubjectsNames());
		subjectName.setAdapter(subjectsAdapter);

		if (lessonToEdit != null) {
			ArrayList<Lesson> ll = ttDB.getLessonsByName(lessonToEdit);
			Collections.sort(ll);
			subjectName.setText(lessonToEdit);

			color = ll.get(0).getColor();
			choosenColorView.setBackgroundColor(color);

			for (int i = 0; i < ll.size(); i++) {
				Lesson cl = ll.get(i);
				LessonEntry le = addLessonLayout();
				le.id = cl.getId();
				le.daysSpinnerView.setSelection(cl.getDay());
				le.roomNameView.setText(cl.getRoom());
				le.startHour = cl.getStartHour();
				le.startMin = cl.getStartMinutes();
				le.endHour = cl.getEndHour();
				le.endMin = cl.getEndMinutes();
				updateTime(le, false);
			}
		} else {
			addLessonLayout();
		}

	}

	private LessonEntry addLessonLayout() {
		return addLessonLayout(false);
	}
	
	private LessonEntry addLessonLayout(boolean animate) {
		LinearLayout newLesson = (LinearLayout) lInflater.inflate(R.layout.timetable_lesson_data, null);
		if (animate) {
			// newLesson.startAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left));
			newLesson.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_down));
		}
		lessonsParent.addView(newLesson);
		LessonEntry le = new LessonEntry(newLesson);
		lessonList.add(le);
		updateTime(le, false);
		return le;
	}

	@Override
	public void actionAccept() {
		String name = subjectName.getText().toString().trim();
		if (name.isEmpty()) {
			subjectName.requestFocus();
			Utils.createAlert(activity, resources.getString(R.string.nome_corso_non_vuoto), null, false);
			return;
		}
		
		if (lessonToEdit != null) {
			ttDB.deleteSubject(lessonToEdit);
		}
		
		boolean atLeastOneLession = false;
		for (LessonEntry le : lessonList) {
			int day = le.daysSpinnerView.getSelectedItemPosition();
			
			String room = le.roomNameView.getText().toString().trim();
//			if (room.isEmpty()) {
//				le.roomNameView.requestFocus();
//				Utils.createAlert(activity, "L'aula non può essere vuota", null, false);
//				return;
//			}
			atLeastOneLession = true;
			Lesson newLesson = new Lesson(le.id, name, day, le.startHour, le.startMin, le.endHour, le.endMin, room, color);
			Log.d(Utils.TAG, "Adding lesson: " + le.id + " | " + name);
			ttDB.insertLesson(newLesson);
		}
		if (!atLeastOneLession) {
			Utils.createAlert(activity, resources.getString(R.string.almeno_una_lezione), null, false);
			return;
		}
		ttDB.insertSubject(name, color);
		activity.switchContent(BootableFragmentsEnum.TIMETABLE, true);
	}

	@Override
	public void actionCancel() {
		if (lessonToEdit != null) {
			ttDB.deleteSubject(lessonToEdit);
		}
		activity.switchContent(BootableFragmentsEnum.TIMETABLE, true);
	}
	
	@Override
	public int getTitleResId() {
		return R.string.aggiungi_corso;
	};

	public void editLesson(Lesson lesson) {
		lessonToEdit = lesson.getSubjectName();
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_accept_button);
		actionsToShow.add(R.id.action_cancel_button);
		return actionsToShow;
	}

	private void checkTimeCorrectness(LessonEntry le, boolean updatedStart) {
		if (le.startHour >= le.endHour) {
			if (updatedStart) {
				le.endHour = le.startHour + 1;
				le.endMin = le.startMin;
			} else {
				le.startHour = le.endHour - 1;
				le.startMin = le.endMin;
			}
		}
		
		if (le.startHour < minStartHour) {
			le.startHour = minStartHour;
			le.startMin = 0;
		} else if (le.startHour > maxStartHour) {
			le.startHour = maxStartHour;
			le.startMin = 0;
		}
		if (le.endHour < minEndHour) {
			le.endHour = minEndHour;
			le.endMin = 0;
		} else if (le.endHour >= maxEndHour) {
			le.endHour = maxEndHour;
			le.endMin = 0;
		}
	}

	private void updateTime(LessonEntry le, boolean updatedStart) {
		checkTimeCorrectness(le, updatedStart);
		le.startTimeView.setText(timeToText(le.startHour, le.startMin));
		le.endTimeView.setText(timeToText(le.endHour, le.endMin));
	}

	private String timeToText(int hour, int min) {
		String h = (hour >= 10) ? "" + hour : "0" + hour;
		String m = (min >= 10) ? "" + min : "0" + min;
		return h + ":" + m;
	}

	private class LessonEntry {
		private LessonEntry thisLesson;
		private View thisView;
		private int id = -1;
		private int startHour = 8;
		private int startMin = 0;
		private int endHour = 9;
		private int endMin = 0;
		private Spinner daysSpinnerView;
		private AutoCompleteTextView roomNameView;
		private TextView startTimeView;
		private TextView endTimeView;
		private ImageView cancelButton;

		public LessonEntry(View lessonView) {
			this.daysSpinnerView = (Spinner) lessonView.findViewById(R.id.week_days_spinner);
			this.startTimeView = (TextView) lessonView.findViewById(R.id.start_time);
			this.endTimeView = (TextView) lessonView.findViewById(R.id.end_time);
			this.roomNameView = (AutoCompleteTextView) lessonView.findViewById(R.id.aula);
			this.cancelButton = (ImageView) lessonView.findViewById(R.id.cancel_button);
			this.thisView = lessonView;
			this.thisLesson = this;
			setEvents();
		}

		private void setEvents() {
			roomNameView.setAdapter(roomsAdapter);
			startTimeView.setOnTouchListener(new OnTouchListener() {
				boolean menuShown = false;
				TimePickerDialog.OnTimeSetListener onStartTimeChanged = new TimePickerDialog.OnTimeSetListener() {
			        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			        	startHour = hourOfDay;
			        	startMin = minute;
			        	updateTime(thisLesson, true);
			        }
				};
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (menuShown) {
						return false;
					}
					menuShown = true;
					TimePickerDialog tp = new TimePickerDialog(activity, onStartTimeChanged, startHour, startMin, true);
					tp.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							menuShown = false;
						}
					});
					tp.show();
					return false;
				}
			});
			endTimeView.setOnTouchListener(new OnTouchListener() {
				boolean menuShown = false;
				TimePickerDialog.OnTimeSetListener onEndTimeChanged = new TimePickerDialog.OnTimeSetListener() {
			        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			        	endHour = hourOfDay;
			        	endMin = minute;
			        	updateTime(thisLesson, false);
			        }
				};
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (menuShown) {
						return false;
					}
					menuShown = true;
					TimePickerDialog tp = new TimePickerDialog(activity, onEndTimeChanged, endHour, endMin, true);
					tp.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							menuShown = false;
						}
					});
					tp.show();
					return false;
				}
			});
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					thisView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_up));
					lessonList.remove(thisLesson);
					Animation slideUp = AnimationUtils.loadAnimation(activity, R.anim.slide_up);
					slideUp.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationEnd(Animation animation) {
							thisView.setVisibility(View.GONE);
//							lessonsParent.removeView(thisView);
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						@Override
						public void onAnimationStart(Animation animation) {
						}
					});
					thisView.startAnimation(slideUp);
				}
			});
		}
	}

}
