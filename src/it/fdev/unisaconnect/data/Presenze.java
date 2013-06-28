package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Presenze implements Serializable {
	private static final long serialVersionUID = -681360705502780753L;

	private Date fetchTime;
	private ArrayList<Esame> listaEsami;
	
	public Presenze(ArrayList<Esame> listaEsami) {
		this.fetchTime = new Date();
		this.listaEsami = listaEsami;
	}
	
	public Date getFetchTime() {
		return fetchTime;
	}
	
	public ArrayList<Esame> getListaEsami() {
		return listaEsami;
	}
	
	public static class Esame implements Serializable {
		private static final long serialVersionUID = 676281892900728352L;
		
		private String nomeEsame;
		private String anno;
		private ArrayList<RiepilogoPresenzeEsame> listaRiepilogoPresenze;
		private ArrayList<DettagliPresenzeEsame> listaDettagliPresenze;

		public Esame(String nomeEsame, String anno, ArrayList<RiepilogoPresenzeEsame> listaRiepilogoPresenze, ArrayList<DettagliPresenzeEsame> listaDettagliPresenze) {
			this.nomeEsame = nomeEsame;
			this.anno = anno;
			this.listaRiepilogoPresenze = listaRiepilogoPresenze;
			this.listaDettagliPresenze = listaDettagliPresenze;
		}
		
		public Esame(String nomeEsame, String anno, ArrayList<RiepilogoPresenzeEsame> listaRiepilogoPresenze) {
			this(nomeEsame, anno, listaRiepilogoPresenze, new ArrayList<DettagliPresenzeEsame>());
		}
		
		public String getNomeEsame() {
			return nomeEsame;
		}
		
		public String getAnno() {
			return anno;
		}
		
		public ArrayList<RiepilogoPresenzeEsame> getListaRiepilogoPresenze() {
			return listaRiepilogoPresenze;
		}

		public ArrayList<DettagliPresenzeEsame> getListaDettagliPresenze() {
			return listaDettagliPresenze;
		}
	}
	
	public static class RiepilogoPresenzeEsame implements Serializable {
		private static final long serialVersionUID = 178711433382574693L;
		
		private String attivita;
		private String totaleOreStudente;
		private String totaleOreDocente;
		private String percentualePresenza;
		
		public RiepilogoPresenzeEsame(String attivita, String totaleOreStudente, String totaleOreDocente, String percentualePresenza) {
			this.attivita = attivita;
			this.totaleOreStudente = totaleOreStudente;
			this.totaleOreDocente = totaleOreDocente;
			this.percentualePresenza = percentualePresenza;
		}
		
		public String getAttivita() {
			return attivita;
		}
		
		public String getTotOreStudente() {
			return totaleOreStudente;
		}

		public String getTotOreDocente() {
			return totaleOreDocente;
		}

		public String getPercentualePresenza() {
			return percentualePresenza;
		}
	}

	public static class DettagliPresenzeEsame implements Serializable {
		private static final long serialVersionUID = -1399796907711177527L;

		private String giorno;
		private String timbrature;
		private String attivita;
		private String ore;
		private String aula;

		public DettagliPresenzeEsame(String giorno, String timbrature, String attivita, String ore, String aula) {
			super();
			this.giorno = giorno;
			this.timbrature = timbrature;
			this.attivita = attivita;
			this.ore = ore;
			this.aula = aula;
		}

		public String getGiorno() {
			return giorno;
		}

		public String getTimbrature() {
			return timbrature;
		}

		public String getAttivita() {
			return attivita;
		}

		public String getOre() {
			return ore;
		}

		public String getAula() {
			return aula;
		}
	}

}
