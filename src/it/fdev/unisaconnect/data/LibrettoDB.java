package it.fdev.unisaconnect.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibrettoDB {
	
	private DBHelper ourHelper; //Create a Helper object
	private final Context ourContext; //Create a Context object
	
	public static final int DB_VERSION = 6; //Version number, can be any number
	public static final String DB_NAME = "libretto.db"; //Name of the database
	
	
	public class DBHelper extends SQLiteOpenHelper { //Helps create DB		
		//Constructor
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) { //Called once and only once for a User to create a DB
			String query = "CREATE TABLE Libretto"
					+ "(name TEXT PRIMARY KEY," 
					+ "cfu TEXT,"
					+ "mark TEXT"
				+ ");";
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //Runs only if a new version of DB (maybe you've added new tables or rows in an update)
			db.execSQL("DROP TABLE IF EXISTS Libretto");
		    this.onCreate(db);
		}

	}
	//Class Constructor
	public LibrettoDB(Context c) {
		ourContext = c; //Initialize the context with passed in context
		
	}
	
	//Open method
	public LibrettoDB open() throws SQLException {
		ourHelper = new DBHelper(ourContext);
		return this;
	}
	
	public void close() {
		ourHelper.close();
	}
	
	public void insertCourse(String name, String cfu, String mark) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("cfu", cfu);
			values.put("mark", mark);
			db.insert("Libretto", "", values);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void insertCourses(ArrayList<LibrettoCourse> list) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		for(LibrettoCourse c : list) {
			try {
				ContentValues values = new ContentValues();
				values.put("name", c.getName());
				values.put("cfu", c.getCFU());
				values.put("mark", c.getMark());
				db.insert("Libretto", "", values);
			} catch (Exception e) {
//				Log.d(Utils.TAG, "");
			}
		}
		if(db!= null)
			db.close();
	}
	
	public void deleteAllCourses() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			db.delete("Libretto", null, null);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public ArrayList<LibrettoCourse> getCourses() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ArrayList<LibrettoCourse> courseList = new ArrayList<LibrettoCourse>();
			Cursor c = db.rawQuery("SELECT * FROM Libretto ORDER BY name", null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String name = c.getString(c.getColumnIndex("name"));
				String cfu = c.getString(c.getColumnIndex("cfu"));
				String mark = c.getString(c.getColumnIndex("mark"));
				LibrettoCourse course = new LibrettoCourse(name, cfu, mark);
				courseList.add(course);
				c.moveToNext();
			}
			c.close();
			return courseList;
		} finally {
			if(db!= null)
				db.close();
		}
	}
}
