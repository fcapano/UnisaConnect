package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Appelli implements Serializable {
	private static final long serialVersionUID = -8103726698218890556L;
	
	private Date fetchDate;
	private ArrayList<Appello> listaAppelliDisponibili, listaAppelliPrenotati;
	
	public Appelli(ArrayList<Appello> listaAppelliDisponibili, ArrayList<Appello> listaAppelliPrenotati) {
		this.fetchDate = new Date();
		this.listaAppelliDisponibili = listaAppelliDisponibili;
		if (this.listaAppelliDisponibili == null) {
			this.listaAppelliDisponibili = new ArrayList<Appelli.Appello>();
		}
		this.listaAppelliPrenotati = listaAppelliPrenotati;
		if (this.listaAppelliPrenotati == null) {
			this.listaAppelliPrenotati = new ArrayList<Appelli.Appello>();
		}
	}
	
	public Date getFetchTime() {
		return fetchDate;
	}

	public ArrayList<Appello> getListaAppelliDisponibili() {
		return listaAppelliDisponibili;
	}
	
	public ArrayList<Appello> getListaAppelliPrenotati() {
		return listaAppelliPrenotati;
	}

	public void setListaAppelliDisponibili(ArrayList<Appello> listaAppelliDisponibili) {
		this.listaAppelliDisponibili = listaAppelliDisponibili;
	}

	public void setListaAppelliPrenotati(ArrayList<Appello> listaAppelliPrenotati) {
		this.listaAppelliPrenotati = listaAppelliPrenotati;
	}
	
	public boolean isEmpty(){
		boolean disponibilyEmpty = listaAppelliDisponibili == null && listaAppelliDisponibili.isEmpty() ;
		boolean prenotatiEmpty = listaAppelliPrenotati == null && listaAppelliPrenotati.isEmpty();
		return disponibilyEmpty && prenotatiEmpty;
	}

	public static class Appello implements Serializable  {
		private static final long serialVersionUID = -8224175339824904440L;
		
		private String name;
		private String date;
		private String time;
		private String description;
		private String subscribedNum;
		private String location;

		public Appello(String name, String date, String time, String description, String subscribedNum, String location) {
			this.name = name;
			this.date = date;
			if (time == null) {
				this.time = "";
			} else {
				this.time = time;
			}
			this.description = description;
			this.subscribedNum = subscribedNum;
			if (location == null) {
				this.location = "";
			} else {
				this.location = location;
			}
		}

		public String getName() {
			return name;
		}

		public String getDate() {
			return date;
		}
		
		public String getTime() {
			return time;
		}

		public String getDescription() {
			return description;
		}

		public String getSubscribedNum() {
			return subscribedNum;
		}
		
		public String getLocation() {
			return location;
		}

	}
}