package it.fdev.unisaconnect.data;

import java.util.ArrayList;

public class StaffMember {

	private String matricola;
	private String fullname;
	private String imgUrl;
	private String smallImgUrl;
	private String role;
	private String department;
	private String mapInfo;
	private ArrayList<String> phoneList;
	private ArrayList<String> faxList;
	private String email;
	private String website;

	private final static String RESIZED_PATH = "http://idw.altervista.org/img_prof/2013.05.05-14.47.48/RES/";
	private final static String ORIGINAL_PATH = "http://idw.altervista.org/img_prof/2013.05.05-14.47.48/ORIG/";

	public StaffMember(String matricola, String fullname, String imgUrl, String role, String department, 
			String mapInfo, ArrayList<String> phoneList, ArrayList<String> faxList, String email, String website) {
		this.matricola = matricola;
		this.fullname = fullname;
		// this.imgUrl = imgUrl;
		this.imgUrl = getOrigImgUrl(matricola, imgUrl);
		this.smallImgUrl = getSmallImgUrl(matricola, imgUrl);
		this.role = role;
		this.department = department;
		if (mapInfo != null)
			this.mapInfo = mapInfo.replace("Ubicazione:", "").trim();
		else
			this.mapInfo = null;
		this.phoneList = phoneList;
		this.faxList = faxList;
		this.email = email;
		this.website = website;
	}

	public String getWebsite() {
		return website;
	}

	public String getDepartment() {
		return department;
	}

	public String getMapInfo() {
		return mapInfo;
	}

	public ArrayList<String> getPhoneList() {
		return phoneList;
	}

	public ArrayList<String> getFaxList() {
		return faxList;
	}

	public String getMatricola() {
		return matricola;
	}

	public String getFullname() {
		return fullname;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public String getRole() {
		return role;
	}

	public String getEmail() {
		return email;
	}

	public String getSmallImgUrl() {
		return smallImgUrl;
	}

	public static String getOrigImgUrl(String matricola, String fullImgUrl) {
		if (fullImgUrl.contains("foto_default"))
			return "";
		return ORIGINAL_PATH + matricola + ".png";
	}

	public static String getSmallImgUrl(String matricola, String fullImgUrl) {
		if (fullImgUrl.contains("foto_default"))
			return "";
		return RESIZED_PATH + matricola + ".png";
	}

	@Override
	public String toString() {
		return "fullname: " + fullname + "   matricola: " + matricola + "   img_url: " + imgUrl + 
			   "   role: " + role + "   department: " + department + "   mapInfo: " + mapInfo + 
			   "   phoneList: " + phoneList + "   faxList: " + faxList + "   email: " + email;
	}

}
