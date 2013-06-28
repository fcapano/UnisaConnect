package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.TimetableDB;
import it.fdev.unisaconnect.data.TimetableSubject.Lesson;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


public class TimetableAddLessonFragment extends MySimpleFragment {
	
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
	private final int minEndHour = minStartHour+1;
	private final int maxEndHour = maxStartHour+1;
	
	private ArrayAdapter<String> roomsAdapter;
	
	private ArrayList<LessonEntry> lessonList = new ArrayList<LessonEntry>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.timetable_add_lesson, container, false);
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
		
//		activity.setActionAcceptVisible(true);
//		activity.setActionRefreshVisible(false);
		
		TypedArray colorsResources = resources.obtainTypedArray(R.array.timetable_lesson_colors);
		colorsArray = new int[colorsResources.length()];
		for (int i = 0; i < colorsResources.length(); i++) {
			colorsArray[i] = resources.getColor(colorsResources.getResourceId(i, R.color.unisa_orange));
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
			if(i==0)
				color = colorsArray[i];
		}
		
		addLesson.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addLessonLayout();
			}
		});

		roomsAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, ttDB.getRoomNames());
		ArrayAdapter<String> subjectsAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, ttDB.getSubjectsNames());
		subjectName.setAdapter(subjectsAdapter);
		
		addLessonLayout();
    }
	
	private void addLessonLayout() {
		View newLesson = lInflater.inflate(R.layout.lesson_data, null);
		lessonsParent.addView(newLesson);
		LessonEntry le = new LessonEntry(newLesson);
		lessonList.add(le);
	}
	
	private void checkTimeCorrectness(LessonEntry le, boolean updatedStart) {
		if(le.startHour > le.endHour) {
			if(updatedStart) {
				le.endHour = le.startHour+1;
				le.endMin = le.startMin;
			} else {
				le.startHour = le.endHour-1;
				le.startMin = le.endMin;
			}
		}
		if(le.startHour < minStartHour) {
			le.startHour = minStartHour;
			le.startMin = 0;
		} else if(le.startHour > maxStartHour) {
			le.startHour = maxStartHour;
			le.startMin = 0;
		}
		if (le.endHour < minEndHour) {
			le.endHour = minEndHour;
			le.endMin = 0;
		} else if(le.endHour >= maxEndHour) {
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
		String h = (hour>=10) ? ""+hour : "0"+hour;
		String m = (min>=10) ? ""+min: "0"+min;
		return h + ":" + m;
	}
	
	@Override
	public void actionAccept() {
		String name = subjectName.getText().toString();
		ttDB.insertSubject(name, color);
		for(LessonEntry le : lessonList) {
			int day = le.daysSpinner.getSelectedItemPosition();
			String room = le.roomName.getText().toString();
			Lesson newLesson = new Lesson(name, day, le.startHour, le.startMin, le.endHour, le.endMin, room, color);
			ttDB.insertLesson(newLesson);
		}
	}
	
	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_accept_button);
		return actionsToShow;
	}
	
	@Override
	public void actionRefresh() {
	}
	
	
	private class LessonEntry {
		private LessonEntry thisLesson;
		private int startHour = 8;
		private int startMin = 0;
		private int endHour = 9;
		private int endMin = 0;
		private Spinner daysSpinner;
		private TextView startTimeView;
		private TextView endTimeView;
		private AutoCompleteTextView roomName;
		
		public LessonEntry(View lessonView) {
			this.daysSpinner = (Spinner) lessonView.findViewById(R.id.week_days_spinner);
			this.startTimeView = (TextView) lessonView.findViewById(R.id.start_time);
			this.endTimeView = (TextView) lessonView.findViewById(R.id.end_time);
			this.roomName = (AutoCompleteTextView) lessonView.findViewById(R.id.aula);
			this.thisLesson = this;
			setEvents();
		}
		
		private void setEvents() {
			roomName.setAdapter(roomsAdapter);
			startTimeView.setOnClickListener(new OnClickListener() {
				TimePickerDialog.OnTimeSetListener onStartTimeChanged = new TimePickerDialog.OnTimeSetListener() {
			        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			        	startHour = hourOfDay;
			        	startMin = minute;
			        	updateTime(thisLesson, true);
			        }
				};
				@Override
				public void onClick(View v) {
					Log.d(Utils.TAG,"ITEM CLICKED!!!");
					TimePickerDialog tp = new TimePickerDialog(activity, onStartTimeChanged, startHour, startMin, true);
					tp.show();
				}
			});
			endTimeView.setOnClickListener(new OnClickListener() {
				TimePickerDialog.OnTimeSetListener onEndTimeChanged = new TimePickerDialog.OnTimeSetListener() {
			        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			        	endHour = hourOfDay;
			        	endMin = minute;
			        	updateTime(thisLesson, false);
			        }
				};
				@Override
				public void onClick(View v) {
					TimePickerDialog tp = new TimePickerDialog(activity, onEndTimeChanged, endHour, endMin, true);
					tp.show();
				}
			});
		}
	}

}
