package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Appelli implements Serializable {
	private static final long serialVersionUID = -8103729698218190556L;
	
	private Date fetchDate;
	private ArrayList<Appello> listaAppelliDisponibili, listaAppelliPrenotati;
	
	public Appelli(ArrayList<Appello> listaAppelliDisponibili, ArrayList<Appello> listaAppelliPrenotati) {
		this.fetchDate = new Date();
		this.listaAppelliDisponibili = listaAppelliDisponibili;
		this.listaAppelliPrenotati = listaAppelliPrenotati;
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

	public static class Appello implements Serializable  {
		private static final long serialVersionUID = -8224175339820904440L;
		
		private String name;
		private String date;
		private String description;
		private String subscribedNum;

		public Appello(String name, String date, String description, String subscribedNum) {
			this.name = name;
			this.date = date;
			this.description = description;
			this.subscribedNum = subscribedNum;
		}

		public String getName() {
			return name;
		}

		public String getDate() {
			return date;
		}

		public String getDescription() {
			return description;
		}

		public String getSubscribedNum() {
			return subscribedNum;
		}

	}
}