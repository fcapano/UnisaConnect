package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Pagamenti implements Serializable {
	private static final long serialVersionUID = 6160894386810849426L;
	
	private Date fetchTime;
	private ArrayList<Pagamento> listaPagamenti;
	
	public Pagamenti(ArrayList<Pagamento> list) {
		this.fetchTime = new Date();
		this.listaPagamenti = list;
	}
	
	public Date getFetchTime() {
		return fetchTime;
	}

	public ArrayList<Pagamento> getListaPagamenti() {
		return listaPagamenti;
	}

	public static class Pagamento implements Serializable {
		private static final long serialVersionUID = 3611873865319239521L;
		
		private String titolo;
		private String descrizione;
		private String importo;
		private String scadenza;
		
		public Pagamento(String titolo, String descrizione, String importo, String scadenza) {
			super();
			this.titolo = titolo;
			this.descrizione = descrizione;
			this.importo = importo;
			this.scadenza = scadenza;
		}
		
		public String getTitolo() {
			return titolo;
		}

		public String getDescrizione() {
			return descrizione;
		}

		public String getImporto() {
			return importo;
		}

		public String getScadenza() {
			return scadenza;
		}
		
	}
	
}
