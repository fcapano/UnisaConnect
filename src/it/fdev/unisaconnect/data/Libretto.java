package it.fdev.unisaconnect.data;

import it.fdev.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class Libretto {
	private Date fetchTime;
	private ArrayList<CorsoLibretto> corsi;
	
	public Libretto() {
		this.fetchTime = new Date();
		this.corsi = new ArrayList<CorsoLibretto>();
	}

	public Libretto(Date fetchTime, ArrayList<CorsoLibretto> corsi) {
		this.fetchTime = fetchTime;
		this.corsi = corsi;
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public ArrayList<CorsoLibretto> getCorsi() {
		return corsi;
	}
	
	public void setCorsi(ArrayList<CorsoLibretto> corsi) {
		this.corsi = corsi;
	}
	
	public CorsoLibretto getCorso(String name) {
		for (CorsoLibretto corso : corsi) {
			if (corso.getName().equals(name)) {
				return corso;
			}
		}
		return null;
	}
	
	public float getMediaAritmetica(){
		int numeroEsami = 0;
		int sommaVoti = 0;
		for (CorsoLibretto corso : corsi) {
			try { // Calcolo la media
				int cfu = Integer.parseInt(corso.getCFU().trim());
				int mark;
				if (corso.getMark().equalsIgnoreCase("30L"))
					mark = 30;
				else
					mark = Integer.parseInt(corso.getMark().trim());
				if (cfu > 0 && mark >= 18) {
					numeroEsami++;
					sommaVoti += mark;
				}
			} catch (NumberFormatException e) {
				// Esami con ideneità. Compaiono come "SUP" e non influiscono sulla media
				Log.w(Utils.TAG, "Voto o cfu non numerico: '" + corso.getCFU() + "' | '" + corso.getMark() + "'", e);
			}
			if (corso.getMark().isEmpty()) // Esame non ancora fatto
				continue;
		}
		float mediaAritmetica = sommaVoti/ (float) numeroEsami;
		mediaAritmetica = Math.round(mediaAritmetica * (float) 1000) / (float) 1000;
		return mediaAritmetica;
	}
	
	public float getMediaPesata(){
		int sommaCFU = 0;
		int sommaVotiPesati = 0;
		for (CorsoLibretto corso : corsi) {
			try { // Calcolo la media
				int cfu = Integer.parseInt(corso.getCFU());
				int mark;
				if (corso.getMark().equalsIgnoreCase("30L"))
					mark = 30;
				else
					mark = Integer.parseInt(corso.getMark());
				if (cfu > 0 && mark >= 18) {
					sommaCFU += cfu;
					sommaVotiPesati += mark * cfu;
				}
			} catch (NumberFormatException e) {
				// Esami con ideneità. Compaiono come "SUP" e non influiscono sulla media
			}
		}
		float mediaPesata = sommaVotiPesati / (float) sommaCFU;
		mediaPesata = Math.round(mediaPesata * (float) 1000) / (float) 1000;
		return mediaPesata;
	}

	public static class CorsoLibretto implements Comparable<CorsoLibretto> {
		private String name;
		private String cfu;
		private String date;
		private String mark;

		public CorsoLibretto(String name, String cfu, String date, String mark) {
			this.name = name;
			this.cfu = cfu;
			this.date = date;
			this.mark = mark;
		}

		public String getName() {
			return name;
		}

		public String getCFU() {
			return cfu;
		}

		public String getDate() {
			return date;
		}
		
		public String getMark() {
			return mark;
		}
		
		public void setMark(String mark) {
			this.mark = mark;
		}

		@Override
		public int compareTo(CorsoLibretto another) {
			SimpleDateFormat parserSDF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
			Date thisDate, otherDate;
			try {
				thisDate = parserSDF.parse(getDate());
			} catch (ParseException e) {
				thisDate = null;
			}
			try {
				otherDate = parserSDF.parse(another.getDate());
			} catch (ParseException e) {
				otherDate = null;
			}
			
			if (thisDate == null && otherDate == null) {
				return 0;
			} else if (thisDate == null) {
				return 1;
			} else if (otherDate == null) {
				return -1;
			} else {
				return thisDate.compareTo(otherDate);
			}
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return new CorsoLibretto(name, cfu, date, mark);
		}
	}
}
