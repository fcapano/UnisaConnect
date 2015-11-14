package it.fdev.unisaconnect.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		private static final long serialVersionUID = 3611873865319239522L;
		
		private List<String> causali;
		private String importo;
		private String scadenza;
		
		public Pagamento(String causale, String importo, String scadenza) {
			super();
			this.causali = new ArrayList<String>(1);
			causali.add(causale);
			this.importo = importo;
			this.scadenza = scadenza;
		}
		
		public Pagamento(List<String> causali, String importo, String scadenza) {
			super();
			this.causali = causali;
			this.importo = importo;
			this.scadenza = scadenza;
		}
		
		public List<String> getCausali() {
			return causali;
		}
		
		public void addCausale(String causale) {
			this.causali.add(causale);
		}

		public String getImporto() {
			return importo;
		}

		public String getScadenza() {
			return scadenza;
		}
		
	}
	
}
