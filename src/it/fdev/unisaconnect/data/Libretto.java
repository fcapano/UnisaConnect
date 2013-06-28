package it.fdev.unisaconnect.data;

import java.util.ArrayList;
import java.util.Date;

public class Libretto {
	private Date fetchTime;
	private ArrayList<LibrettoCourse> corsi;
	
	public Libretto() {
		this.fetchTime = new Date();
		this.corsi = new ArrayList<LibrettoCourse>();
	}

	public Libretto(Date fetchTime, ArrayList<LibrettoCourse> corsi) {
		this.fetchTime = fetchTime;
		this.corsi = corsi;
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public ArrayList<LibrettoCourse> getCorsi() {
		return corsi;
	}
	
	public void setCorsi(ArrayList<LibrettoCourse> corsi) {
		this.corsi = corsi;
	}

	public static class LibrettoCourse {
		private String name;
		private String cfu;
		private String mark;

		public LibrettoCourse(String name, String cfu, String mark) {
			this.name = name;
			this.cfu = cfu;
			this.mark = mark;
		}

		public String getName() {
			return name;
		}

		public String getCFU() {
			return cfu;
		}

		public String getMark() {
			return mark;
		}
	}
}
