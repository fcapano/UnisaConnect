package it.fdev.unisaconnect.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class StaffMember implements Parcelable {

	private String matricola;
	private String fullname;
	private String imgBigUrl;
	private String imgSmallUrl;
	private String role;
	private String department;
	private String mapInfo;
	private ArrayList<String> phoneList;
	private ArrayList<String> faxList;
	private String email;
	private String website;
	private String ricevimento;
	private double ufficioLatitudine;
	private double ufficioLongitudine;

	public StaffMember(String matricola, String fullname, String imgBigUrl, String imgSmallUrl, 
						String role, String department,	String mapInfo, ArrayList<String> phoneList, 
						ArrayList<String> faxList, String email, String website, String ricevimento, 
						double ufficioLatitudine, double ufficioLongitudine) {
		this.matricola = matricola;
		this.fullname = fullname;
		this.imgBigUrl = imgBigUrl;
		this.imgSmallUrl = imgSmallUrl;
		this.role = role;
		this.department = department;
		this.mapInfo = mapInfo;
		this.phoneList = phoneList;
		this.faxList = faxList;
		this.email = email;
		this.website = website;
		this.ricevimento = ricevimento;
		this.ufficioLatitudine = ufficioLatitudine;
		this.ufficioLongitudine = ufficioLongitudine;
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

	public String getRole() {
		return role;
	}

	public String getEmail() {
		return email;
	}

	public String getImgBigURL() {
		return imgBigUrl;
	}
	
	public String getImgSmallURL() {
		return imgSmallUrl;
	}
	
	public double getLatitudine() {
		return ufficioLatitudine;
	}
	
	public double getLongitudine() {
		return ufficioLongitudine;
	}

	@Override
	public String toString() {
		return 	   "fullname: " 	+ fullname 			+
				"   matricola: " 	+ matricola 		+
				"   img_url: " 		+ imgBigUrl 		+
				"   role: " 		+ role 				+
				"   department: " 	+ department 		+
				"   mapInfo: " 		+ mapInfo 			+
				"   phoneList: " 	+ phoneList 		+
				"   faxList: " 		+ faxList 			+
				"   email: " 		+ email 			+ 
				"   website"		+ website 			+ 
				"   ricevimento"	+ ricevimento 		+
				"	latitudine"		+ ufficioLatitudine +
				"	longitudine"	+ ufficioLongitudine;
	}
	
	public StaffMember(Parcel in) {
		this.matricola = in.readString();
		this.fullname = in.readString();
		this.imgBigUrl = in.readString();
		this.imgSmallUrl = in.readString();
		this.role = in.readString();
		this.department = in.readString();
		this.mapInfo = in.readString();
		phoneList = new ArrayList<String>();
		in.readList(phoneList, null);
		faxList = new ArrayList<String>();
		in.readList(faxList, null);
		this.email = in.readString();
		this.website = in.readString();
		this.ricevimento = in.readString();
		this.ufficioLatitudine = in.readDouble();
		this.ufficioLongitudine = in.readDouble();
	}

	@Override
	public int describeContents() {
		return this.hashCode();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(matricola);
		dest.writeString(fullname);
		dest.writeString(imgBigUrl);
		dest.writeString(imgSmallUrl);
		dest.writeString(role);
		dest.writeString(department);
		dest.writeString(mapInfo);
		dest.writeList(phoneList);
		dest.writeList(faxList);
		dest.writeString(email);
		dest.writeString(website);
		dest.writeString(ricevimento);
		dest.writeDouble(ufficioLatitudine);
		dest.writeDouble(ufficioLongitudine);
	}
	
	public static final Parcelable.Creator<StaffMember> CREATOR = new Parcelable.Creator<StaffMember>() {
	    public StaffMember createFromParcel(Parcel in) {
	        return new StaffMember(in);
	    }

	    public StaffMember[] newArray(int size) {
	        return new StaffMember[size];
	    }
	};


}
