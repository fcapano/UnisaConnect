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
	private String ricevimento;

	private final static String IMG_DATE = "2013.06.03-15.54.30";
	private final static String RESIZED_PATH = "http://idw.altervista.org/img_prof/" + IMG_DATE + "/RES/";
	private final static String ORIGINAL_PATH = "http://idw.altervista.org/img_prof/" + IMG_DATE + "/ORIG/";

	public StaffMember(String matricola, String fullname, String imgUrl, String role, String department, 
			String mapInfo, ArrayList<String> phoneList, ArrayList<String> faxList, String email, String website, String ricevimento) {
		this.matricola = matricola;
		this.fullname = fullname;
		// this.imgUrl = imgUrl;
		this.imgUrl = getOrigImgUrl(matricola, imgUrl);
		this.smallImgUrl = getSmallImgUrl(matricola, imgUrl);
		this.role = role;
		this.department = department;
		this.mapInfo = mapInfo;
		this.phoneList = phoneList;
		this.faxList = faxList;
		this.email = email;
		this.website = website;
		this.ricevimento = ricevimento;
	}

	public String getWebsite() {
		return website;
	}
	
	public String getRicevimento() {
		return ricevimento;
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
//		if (fullImgUrl.contains("foto_default"))
		if (fullImgUrl == null) {
			return null;
		}
		return ORIGINAL_PATH + matricola + ".png";
	}

	public static String getSmallImgUrl(String matricola, String fullImgUrl) {
//		if (fullImgUrl.contains("foto_default"))
		if (fullImgUrl == null) {
			return null;
		}
		return RESIZED_PATH + matricola + ".png";
	}

	@Override
	public String toString() {
		return "fullname: " + fullname + "   matricola: " + matricola + "   img_url: " + imgUrl + 
			   "   role: " + role + "   department: " + department + "   mapInfo: " + mapInfo + 
			   "   phoneList: " + phoneList + "   faxList: " + faxList + "   email: " + email;
	}

}
