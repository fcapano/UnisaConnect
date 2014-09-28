package it.fdev.unisaconnect.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class BusTratta1 {

	private String compagnia;
	private String capolinea;
	private List<BusCorsa1> corseVersoUni;
	private List<BusCorsa1> corseDaUni;
	private List<String> stazioniVersoUni;
	private List<String> stazioniDaUni;
	
	public BusTratta1() {
		
	}

	public BusTratta1(String compagnia, String capolinea, List<BusCorsa1> corse, List<String> stazioniVersoUni, List<String> stazioniDaUni) {
		this.compagnia = compagnia;
		this.capolinea = capolinea;

		corseVersoUni = new ArrayList<BusCorsa1>();
		corseDaUni = new ArrayList<BusCorsa1>();
		for (BusCorsa1 cCorsa : corse) {
			if (cCorsa.isVersoUni()) {
				corseVersoUni.add(cCorsa);
			} else {
				corseDaUni.add(cCorsa);
			}
		}
		Collections.sort(corseVersoUni);
		Collections.sort(corseDaUni);

		this.stazioniVersoUni = stazioniVersoUni;
		this.stazioniDaUni = stazioniDaUni;
	}

	public String getCompagnia() {
		return compagnia;
	}

	public String getCapolinea() {
		return capolinea;
	}

	public List<BusCorsa1> getCorseVersoUni() {
		return corseVersoUni;
	}

	public List<BusCorsa1> getCorseDaUni() {
		return corseDaUni;
	}

	public void addCorsa(BusCorsa1 corsa) {
		if (corsa.isVersoUni()) {
			corseVersoUni.add(corsa);
			Collections.sort(corseVersoUni);
		} else {
			corseDaUni.add(corsa);
			Collections.sort(corseDaUni);
		}
	}

	public List<String> getStazioniVersoUni() {
		return stazioniVersoUni;
	}

	public List<String> getStazioniDaUni() {
		return stazioniDaUni;
	}

	public static class BusCorsa1 implements Comparable<BusCorsa1> {
		private BusTratta1 tratta;
		private boolean versoUni;
		private String giorni;
		private int oraPartenza;
		private List<BusFermata1> fermate;
		private WeakHashMap<String, BusFermata1> fermateIndexed;

		public BusCorsa1(BusTratta1 tratta, boolean versoUni, String giorni, int oraPartenza, List<BusFermata1> fermate) {
			this.tratta = tratta;
			this.versoUni = versoUni;
			this.giorni = giorni;
			this.oraPartenza = oraPartenza;
			this.fermate = fermate;

			fermateIndexed = new WeakHashMap<String, BusFermata1>(fermate.size());
			for (BusFermata1 cFermata : fermate) {
				fermateIndexed.put(cFermata.getStazione(), cFermata);
			}
			//
			// oraPartenza = Integer.MAX_VALUE;
			// for (BusFermata cFermata : fermate) {
			// oraPartenza = Math.min(oraPartenza, cFermata.getOra());
			// }
		}
		
		public BusCorsa1(boolean versoUni, String giorni, int oraPartenza, List<BusFermata1> fermate) {
			this(null, versoUni, giorni, oraPartenza, fermate);
		}

		public BusTratta1 getTratta() {
			return tratta;
		}

		public boolean isVersoUni() {
			return versoUni;
		}

		public String getGiorni() {
			return giorni;
		}

		public int getOraPartenza() {
			return oraPartenza;
		}

		public List<BusFermata1> getFermate() {
			return fermate;
		}

		public int getOraFermata(String fermata) {
			if (fermateIndexed.containsKey(fermata)) {
				return fermateIndexed.get(fermata).getOra();
			}
			return -1;
		}

		@Override
		public int compareTo(BusCorsa1 another) {
			return this.oraPartenza - another.getOraPartenza();
		}

		public static class BusFermata1 implements Comparable<BusFermata1> {
			private BusCorsa1 corsa;
			private String stazione;
			private int ora;
			private int posizione;

			public BusFermata1(BusCorsa1 corsa, String stazione, int ora, int posizione) {
				this.corsa = corsa;
				this.stazione = stazione;
				this.ora = ora;
				this.posizione = posizione;
			}
			
			public BusFermata1(String stazione, int ora, int posizione) {
				this(null, stazione, ora, posizione);
			}

			public BusCorsa1 getCorsa() {
				return corsa;
			}

			public String getStazione() {
				return stazione;
			}

			public int getOra() {
				return ora;
			}

			public int getPosizione() {
				return posizione;
			}

			public void setCorsa(BusCorsa1 corsa) {
				this.corsa = corsa;
			}

			@Override
			public int compareTo(BusFermata1 another) {
				// return ora - another.getOra();
				return posizione - another.getPosizione();
			}

		}

	}
}