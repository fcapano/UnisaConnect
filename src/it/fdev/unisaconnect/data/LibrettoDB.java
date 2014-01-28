package it.fdev.unisaconnect.data;

import it.fdev.unisaconnect.data.Libretto.CorsoLibretto;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibrettoDB {
	
	private DBHelper ourHelper; //Create a Helper object
	private final Context mContext; //Create a Context object
	
	public static final int DB_VERSION = 7; //Version number, can be any number
	public static final String DB_NAME = "libretto.db"; //Name of the database
	
	private SharedPrefDataManager mDataManager;
	
	
	public class DBHelper extends SQLiteOpenHelper { //Helps create DB		
		//Constructor
		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) { //Called once and only once for a User to create a DB
			String query1 = "CREATE TABLE Libretto"
					+ "(name TEXT PRIMARY KEY," 
					+ "cfu TEXT,"
					+ "date TEXT,"
					+ "mark TEXT"
				+ ");";
			db.execSQL(query1);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //Runs only if a new version of DB (maybe you've added new tables or rows in an update)
			db.execSQL("DROP TABLE IF EXISTS Libretto");
		    this.onCreate(db);
		}

	}
	//Class Constructor
	public LibrettoDB(Context mContext) {
		this.mContext = mContext; //Initialize the context with passed in context
		mDataManager = new SharedPrefDataManager(mContext);
	}
	
	//Open method
	public LibrettoDB open() throws SQLException {
		ourHelper = new DBHelper(mContext);
		return this;
	}
	
	public void close() {
		ourHelper.close();
	}
	
	public void insertCourse(CorsoLibretto course) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put("name", course.getName());
			values.put("cfu", course.getCFU());
			values.put("date", course.getDate());
			values.put("mark", course.getMark());
			db.insert("Libretto", "", values);
		} finally {
			if(db!= null)
				db.close();
		}
	}
	
	public void insertCourses(ArrayList<CorsoLibretto> list) {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		for(CorsoLibretto c : list) {
			try {
				insertCourse(c);
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
	
	public void resetLibretto(Libretto libretto) {
		deleteAllCourses();
		insertCourses(libretto.getCorsi());
		mDataManager.setLibrettoFetchDate(libretto.getFetchTime());
	}
	
	public Libretto getLibretto() {
		ArrayList<CorsoLibretto> corsi = getCourses();
		Libretto l = new Libretto(mDataManager.getLibrettoFetchDate(), corsi);
		return l;
	}
	
	public ArrayList<CorsoLibretto> getCourses() {
		SQLiteDatabase db = ourHelper.getWritableDatabase();
		try {
			ArrayList<CorsoLibretto> courseList = new ArrayList<CorsoLibretto>();
			Cursor c = db.rawQuery("SELECT * FROM Libretto ORDER BY name", null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String name = c.getString(c.getColumnIndex("name"));
				String cfu = c.getString(c.getColumnIndex("cfu"));
				String date = c.getString(c.getColumnIndex("date"));
				String mark = c.getString(c.getColumnIndex("mark"));
				CorsoLibretto course = new CorsoLibretto(name, cfu, date, mark);
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
