package it.fdev.unisaconnect.data;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StaffDB {
//	private static final String DB_UPDATE_URL = "http://idw.altervista.org/img_prof/latest.xml";
	private static final String DB_STAFF_DATE = "2014.01.17-20.17.29";	//First used: V19
	private static final String DB_STAFF_NAME = "uc_staff-" + DB_STAFF_DATE + ".db";
	
	private SQLiteDatabase dbStaff;
	
	public StaffDB(Context context) throws IOException {
		new DatabaseHelper(context, DB_STAFF_NAME);
		dbStaff = SQLiteDatabase.openDatabase(context.getDatabasePath(DB_STAFF_NAME).getAbsolutePath()
				, null, SQLiteDatabase.OPEN_READONLY + SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}
	
//	public ArrayList<StaffMemberSummary> getStaffSummaryByName(String name) {
//		String staffTableName = "staff";
//		String[] cols = new String[]{"matricola", "fullname", "img_small_url"};
//		String where = "fullname LIKE ?";
//		String[] args = new String[]{"%"+name+"%"};
//		String orderBy = "fullname";
//		
//		ArrayList<StaffMemberSummary> staffList = new ArrayList<StaffMemberSummary>();
//		Cursor results = dbStaff.query(staffTableName, cols, where, args, null, null, orderBy);
//		results.moveToFirst();
//		while(!results.isAfterLast()) {
//			staffList.add(new StaffMemberSummary(results.getString(0),results.getString(1),results.getString(2)));
//			results.moveToNext();
//		}
//		results.close();
//		return staffList;
//	}
	
	public ArrayList<StaffMemberSummary> searchStaffNameByWords(String words) {
		ArrayList<StaffMemberSummary> staffList = new ArrayList<StaffMemberSummary>();
		String[] wordsArray = words.split(" ");
		if (wordsArray.length <= 0) {
			return staffList;
		}
		
		String staffTableName = "staff";
		String[] cols = new String[]{"matricola", "fullname", "img_small_url"};
		
		String where = "fullname LIKE ";
		String[] args = new String[wordsArray.length];
		for (int i=0; i<wordsArray.length-1; i++) {
			where += "? AND fullname LIKE ";
			args[i] = "%"+wordsArray[i]+"%";
		}
		where += "?";
		args[wordsArray.length-1] = "%"+wordsArray[wordsArray.length-1]+"%";
		
		String orderBy = "fullname";
		
		Cursor results = dbStaff.query(staffTableName, cols, where, args, null, null, orderBy);
		results.moveToFirst();
		while(!results.isAfterLast()) {
			String matricola 	= results.getString(0);
			String fullname 	= results.getString(1);
			String imgSmallUrl 	= results.getString(2);
			StaffMemberSummary member = new StaffMemberSummary(matricola, fullname, imgSmallUrl);
			staffList.add(member);
			results.moveToNext();
		}
		results.close();
		return staffList;
	}
	
	public StaffMember getStaffMember(String id) {
		String staffTableName = "staff";
		String[] cols = new String[]{"matricola", "fullname", "img_big_url", "img_small_url", "role", 
				"department", "map_info", "email", "website", "ricevimento"};
		String where = "matricola=?";
		String[] args = new String[]{id};
		String orderBy = "fullname";
		
		Cursor results = dbStaff.query(staffTableName, cols, where, args, null, null, orderBy);
		
		if(results.getCount()<=0) {
			results.close();
			return null;
		}
		results.moveToFirst();
		
		String matricola 	= results.getString(0);
		String fullname 	= results.getString(1);
		String imgBigUrl 	= results.getString(2);
		String imgSmallUrl 	= results.getString(3);
		String role 		= results.getString(4);
		String department 	= results.getString(5);
		String mapInfo 		= results.getString(6);
		String email 		= results.getString(7);
		String website 		= results.getString(8);
		String ricevimento 	= results.getString(9);
		ArrayList<String> phoneList =  getPhone(matricola);
		ArrayList<String> faxList	= getFax(matricola);
		
		StaffMember member = new StaffMember(matricola, fullname, imgBigUrl, imgSmallUrl, role, 
				department, mapInfo, phoneList, faxList, email, website, ricevimento);
		results.close();
		return member;
	}
	
	private ArrayList<String> getPhone(String matricola) {
		String phoneTableName = "phonenum";
		String[] cols = new String[]{"number"};
		String where = "staff_matricola=?";
		String[] args = new String[]{matricola};
		ArrayList<String> phoneList = new ArrayList<String>();
		
		Cursor results = dbStaff.query(phoneTableName, cols, where, args, null, null, null);
		if(results.getCount()<=0) {
			results.close();
			return phoneList;
		}
		results.moveToFirst();
		while(!results.isAfterLast()) {
			phoneList.add(results.getString(0));
			results.moveToNext();
		}
		results.close();
		return phoneList;
	}
	
	private ArrayList<String> getFax(String matricola) {
		String faxTableName = "faxnum";
		String[] cols = new String[]{"number"};
		String where = "staff_matricola=?";
		String[] args = new String[]{matricola};
		ArrayList<String> faxList = new ArrayList<String>();
		
		Cursor results = dbStaff.query(faxTableName, cols, where, args, null, null, null);
		if(results.getCount()<=0) {
			results.close();
			return faxList;
		}
		results.moveToFirst();
		while(!results.isAfterLast()) {
			faxList.add(results.getString(0));
			results.moveToNext();
		}
		results.close();
		return faxList;
	}
	
	public void close() {
		dbStaff.close();
	}
	
//	private void checkDBUpdate() {
//		try {
//			Response response = Jsoup.connect(DB_UPDATE_URL).timeout(30000).execute();
//			Document document = response.parse();
//			Element root = document.getElementsByTag("db").first();
//			String version = root.attr("version");
//			String dbUrl = root.attr("url");
//			
//		} catch(Exception e) {
//			
//		}
//	}

}
