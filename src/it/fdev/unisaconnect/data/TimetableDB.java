package it.fdev.unisaconnect.data;

import it.fdev.unisaconnect.data.TimetableSubject.Lesson;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimetableDB {
	
	private DBHelper ourHelper; //Create a Helper object
	private final Context ourContext; //Create a Context object
	
	public static final int DB_VERSION = 13; //Version number, can be any number
	public static final String DB_NAME = "timetable.db"; //Name of the database
	
	
	public class DBHelper extends SQLiteOpenHelper { //Helps create DB		
		//Constructor
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) { //Called once and only once for a User to create a DB
			String query = "CREATE TABLE Subjects"
					+ "("
					+ "name TEXT PRIMARY KEY," //name - Paper number 
					+ "color INTEGER" //color
					+ ");";
				db.execSQL(query);
				query = "CREATE TABLE Hours"
						+ "("
						+ "lesson_id INTEGER PRIMARY KEY AUTOINCREMENT," //id
						+ "name TEXT," //
						+ "day INTEGER," // Day
						+ "room INTEGER," //Class
						+ "start_hour INTEGER," //Hour Start
						+ "start_minutes INTEGER," //
						+ "end_hour INTEGER," //Hour Start
						+ "end_minutes INTEGER" //
						+ ");";
				db.execSQL(query);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //Runs only if a new version of DB (maybe you've added new tables or rows in an update)
			db.execSQL("DROP TABLE IF EXISTS Hours");
			db.execSQL("DROP TABLE IF EXISTS Subjects");
		    this.onCreate(db);
		}

	}
	//Class Constructor
	public TimetableDB(Context c) {
		ourContext = c; //Initialize the context with passed in context
	}
	
	//Open method
	public TimetableDB open() throws SQLException {
		ourHelper = new DBHelper(ourContext);
		return this;
	}
	
	public void close() {
		ourHelper.close();
	}
	
	public void insertSubject(String name, int color) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("color", color);
			db.insert("Subjects", "", values);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void updateSubject(String name, int color) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put("color", color);
			db.update("Subjects", values, "name = ?", new String[] { name });
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void deleteSubject(String name) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			db.delete("Subjects", "name = ?", new String[] { name });
			db.delete("Hours", "name = ?", new String[] { name });
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void deleteSubjectsWOLessons() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			db.delete("Subjects", "Subjects.name NOT IN (SELECT name FROM Hours)", null);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
//	public void updateLesson(Lesson oldLesson, Lesson newLesson) {
//		SQLiteDatabase db = ourHelper.getWritableDatabase();
//		try {
//			ContentValues values = new ContentValues();
//			values.put("name", newLesson.getSubjectName());
//			values.put("day", newLesson.getDay());
//			values.put("room", newLesson.getRoom());
//			values.put("start_hour", newLesson.getStartHour());
//			values.put("start_minutes", newLesson.getStartMinutes());
//			values.put("end_hour", newLesson.getEndHour());
//			values.put("end_minutes", newLesson.getEndMinutes());
//			db.update("Hours", values, "name='"+oldLesson.getSubjectName()+"'" +
//					"AND room='"+oldLesson.getRoom()+"'" +
//					"AND day='"+oldLesson.getDay()+"'" +
//					"AND start_hour='"+oldLesson.getStartHour()+"'" +
//					"AND start_minutes='"+oldLesson.getStartMinutes()+"'" +
//					"AND end_hour='"+oldLesson.getEndHour()+"'" +
//					"AND end_minutes='"+oldLesson.getEndMinutes()+"'", null);
//		} finally {
//			if(db!= null)
//				db.close();
//		}
//	}
	
	public HashMap<String, TimetableSubject> selectSubjects() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			HashMap<String, TimetableSubject> subjectMap = new HashMap<String, TimetableSubject>();
			Cursor c = db.rawQuery("SELECT * FROM Subjects",null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				subjectMap.put(c.getString(0),new TimetableSubject(c.getString(0),c.getString(1)));
				c.moveToNext();
			}
			c.close();
			return subjectMap;
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void insertLesson(Lesson lesson) {
		if (lesson.getId() >= 0) {
			deleteLesson(lesson);
		}
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			if (lesson.getId() >= 0) {
				values.put("lesson_id", lesson.getId());
			}
			values.put("name", lesson.getSubjectName());
			values.put("day", lesson.getDay());
			values.put("room", lesson.getRoom());
			values.put("start_hour", lesson.getStartHour());
			values.put("start_minutes", lesson.getStartMinutes());
			values.put("end_hour", lesson.getEndHour());
			values.put("end_minutes", lesson.getEndMinutes());
			db.insert("Hours", null, values);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void deleteLesson(Lesson lesson) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			db.delete( "Hours",
					   "lesson_id = ?",
					   new String[] { String.valueOf(lesson.getId()) });
//			db.delete("Hours",
//					"name = '"+lesson.getSubjectName()+"' " +
//					"AND day = '"+lesson.getDay()+"' " +
//					"AND room = '"+lesson.getRoom()+"' " +
//					"AND start_hour = '"+lesson.getStartHour()+"' " +
//					"AND start_minutes = '"+lesson.getStartMinutes()+"' " +
//					"AND end_hour = '"+lesson.getEndHour()+"' " +
//					"AND end_minutes = '"+lesson.getEndMinutes()+"' ", null);
		} finally {
			if(db!= null)
				db.close();
		}
		deleteSubjectsWOLessons();
	}
	
//	public void deleteAllLessons() {
//		SQLiteDatabase db = ourHelper.getWritableDatabase();
//		try {
//			db.delete("Hours", null, null);
//		} finally {
//			if(db!= null)
//				db.close();
//		}
//	}
	
//	public ArrayList<Lesson> getLessons(int day) {
//		SQLiteDatabase db = ourHelper.getWritableDatabase();
//		try {
//			ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
//			Cursor c = db.rawQuery("SELECT * FROM Subjects, Hours WHERE Subjects.name=Hours.name and day="+day+" order by hour",null);
//			c.moveToFirst();
//			while(!c.isAfterLast()) {
//				String name = c.getString(c.getColumnIndex("name"));
//				String room = c.getString(c.getColumnIndex("room"));
//				int color = c.getInt(c.getColumnIndex("color"));
//				int id = c.getInt(c.getColumnIndex("lesson_id"));
//				int startHour = c.getInt(c.getColumnIndex("start_hour"));
//				int startMinutes = c.getInt(c.getColumnIndex("start_minutes"));
//				int endHour = c.getInt(c.getColumnIndex("end_hour"));
//				int endMinutes = c.getInt(c.getColumnIndex("end_minutes"));
//				Lesson lesson = new Lesson(id, name, day, startHour, startMinutes, endHour, endMinutes, room, color);
//				lessonList.add(lesson);
//				c.moveToNext();
//			}
//			c.close();
//			return lessonList;
//		} finally {
//			if(db!= null)
//				db.close();
//		}
//	}
	
	public ArrayList<Lesson> getLessons() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
			Cursor c = db.rawQuery("SELECT * FROM Subjects, Hours WHERE Subjects.name=Hours.name", null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String name = c.getString(c.getColumnIndex("name"));
				String room = c.getString(c.getColumnIndex("room"));
				int color = c.getInt(c.getColumnIndex("color"));
				int id = c.getInt(c.getColumnIndex("lesson_id"));
				int day = c.getInt(c.getColumnIndex("day"));
				int startHour = c.getInt(c.getColumnIndex("start_hour"));
				int startMinutes = c.getInt(c.getColumnIndex("start_minutes"));
				int endHour = c.getInt(c.getColumnIndex("end_hour"));
				int endMinutes = c.getInt(c.getColumnIndex("end_minutes"));
				Lesson lesson = new Lesson(id, name, day, startHour, startMinutes, endHour, endMinutes, room, color);
				lessonList.add(lesson);
				c.moveToNext();
			}
			c.close();
			return lessonList;
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public ArrayList<Lesson> getLessonsByName(String name) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
			// TODO correggi sqlinject
			Cursor c = db.rawQuery("SELECT * FROM Subjects, Hours WHERE Subjects.name=? AND Subjects.name=Hours.name", new String[] { name });
			c.moveToFirst();
			while(!c.isAfterLast()) {
//				String name = c.getString(c.getColumnIndex("name"));
				String room = c.getString(c.getColumnIndex("room"));
				int color = c.getInt(c.getColumnIndex("color"));
				int id = c.getInt(c.getColumnIndex("lesson_id"));
				int day = c.getInt(c.getColumnIndex("day"));
				int startHour = c.getInt(c.getColumnIndex("start_hour"));
				int startMinutes = c.getInt(c.getColumnIndex("start_minutes"));
				int endHour = c.getInt(c.getColumnIndex("end_hour"));
				int endMinutes = c.getInt(c.getColumnIndex("end_minutes"));
				Lesson lesson = new Lesson(id, name, day, startHour, startMinutes, endHour, endMinutes, room, color);
				lessonList.add(lesson);
				c.moveToNext();
			}
			c.close();
			return lessonList;
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public String[] getSubjectsNames() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			Cursor c = db.rawQuery("select name from Subjects", null);
			String[] subjectNames = new String[c.getCount()];
			c.moveToFirst();
			int cSubjIndex = 0;
			while(!c.isAfterLast()) {
				String name = c.getString(c.getColumnIndex("name"));
				subjectNames[cSubjIndex] = name;
				cSubjIndex++;
				c.moveToNext();
			}
			c.close();
			return subjectNames;
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public String[] getRoomNames() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			Cursor c = db.rawQuery("SELECT DISTINCT room FROM Hours", null);
			String[] roomNames = new String[c.getCount()];
			c.moveToFirst();
			int cRoomIndex = 0;
			while(!c.isAfterLast()) {
				String name = c.getString(c.getColumnIndex("room"));
				roomNames[cRoomIndex] = name;
				cRoomIndex++;
				c.moveToNext();
			}
			c.close();
			return roomNames;
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
}
