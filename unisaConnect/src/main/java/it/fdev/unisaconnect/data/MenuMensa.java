package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class MenuMensa implements Serializable {
	private static final long serialVersionUID = -7048895143773661486L;
	private Date fetchTime;
	private String menuDate;
	private String menuDateMillis;
	private String pdfUrl;
	private ArrayList<PiattoMensa> firstCourses;
	private ArrayList<PiattoMensa> secondCourses;
	private ArrayList<PiattoMensa> sideCourses;
	private ArrayList<PiattoMensa> fruitCourse;
	private ArrayList<PiattoMensa> takeAwayBasketCourses;
	private ArrayList<PiattoMensa> otherCourses;

	public MenuMensa(String menuDate, String menuDateMillis, String pdfUrl, ArrayList<PiattoMensa> firstCourses, ArrayList<PiattoMensa> secondCourses, ArrayList<PiattoMensa> sideCourses, ArrayList<PiattoMensa> fruitCourse, ArrayList<PiattoMensa> takeAwayBasketCourses) {
		this.fetchTime = new Date();
		this.menuDate = menuDate;
		this.menuDateMillis = menuDateMillis;
		this.pdfUrl = pdfUrl;
		this.firstCourses = firstCourses;
		this.secondCourses = secondCourses;
		this.sideCourses = sideCourses;
		this.fruitCourse = fruitCourse;
		this.takeAwayBasketCourses = takeAwayBasketCourses;

		if (fruitCourse != null && fruitCourse.size() > 0 && takeAwayBasketCourses != null && takeAwayBasketCourses.size() > 0) {
			otherCourses = new ArrayList<PiattoMensa>();
			otherCourses.addAll(fruitCourse);
			otherCourses.addAll(takeAwayBasketCourses);
		}

	}
	
	public Date getFetchTime() {
		return fetchTime;
	}
	
	public String getDate() {
		return menuDate;
	}
	
	public String getDateMillis() {
		return menuDateMillis;
	}

	public void setFetchTime(Date fetchTime) {
		this.fetchTime = fetchTime;
	}

	public String getPdfUrl() {
		return pdfUrl;
	}

	public ArrayList<PiattoMensa> getFirstCourses() {
		return firstCourses;
	}

	public ArrayList<PiattoMensa> getSecondCourses() {
		return secondCourses;
	}

	public ArrayList<PiattoMensa> getSideCourses() {
		return sideCourses;
	}

	public ArrayList<PiattoMensa> getFruitCourses() {
		return fruitCourse;
	}

	public ArrayList<PiattoMensa> getTakeAwayBasketCourses() {
		return takeAwayBasketCourses;
	}
	
	public ArrayList<PiattoMensa> getOtherCourses() {
		return otherCourses;
	}
	
	public static class PiattoMensa implements Serializable {
		private static final long serialVersionUID = 7496262709862072594L;
		private String nomePiatto;
		private String ingredientiIt = null;
		private String ingradientiEn = null;

		public PiattoMensa(String nomePiatto, String ingredientiIt, String ingradientiEn) {
			this.nomePiatto = nomePiatto;
			this.ingredientiIt = ingredientiIt;
			this.ingradientiEn = ingradientiEn;
		}

		public PiattoMensa(String nomePiatto, String ingredientiIt) {
			this(nomePiatto, ingredientiIt, null);
		}

		public PiattoMensa(String nomePiatto) {
			this(nomePiatto, null, null);
		}

		public String getNomePiatto() {
			return nomePiatto;
		}

		public String getIngredientiIt() {
			return ingredientiIt;
		}

		public String getIngradientiEn() {
			return ingradientiEn;
		}
	}
}
