package it.fdev.unisaconnect.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Libretto {
	private Date fetchTime;
	private float mediaAritmetica;
	private float mediaPonderata;
	private int nCFUTotali;
	private int nCFUConseguiti;
	
	private ArrayList<CorsoLibretto> corsiByDate;
	private ArrayList<CorsoLibretto> corsiByName;
	
	public Libretto() {
		this(new Date(), new ArrayList<CorsoLibretto>());
	}

	public Libretto(Date fetchTime, ArrayList<CorsoLibretto> corsi) {
		this.fetchTime = fetchTime;
		setCorsi(corsi);
	}

	@SuppressWarnings("unchecked")
	public void setCorsi(ArrayList<CorsoLibretto> corsi) {
		this.corsiByDate = (ArrayList<CorsoLibretto>) corsi.clone();
		Collections.sort(this.corsiByDate, sorterCorsiByDate);
		this.corsiByName = corsi;
		Collections.sort(this.corsiByName, sorterCorsiByName);
		calcolaStatistiche();
	}
	
	public Date getFetchTime() {
		return fetchTime;
	}

	public ArrayList<CorsoLibretto> getCorsiByName() {
		return corsiByName;
	}
	
	public ArrayList<CorsoLibretto> getCorsiByDate() {
		return corsiByDate;
	}
	
	public int getCFUTotali() {
		return this.nCFUTotali;
	}
	
	public int getCFUConseguiti() {
		return this.nCFUConseguiti;
	}
	
	public float getMediaAritmetica() {
		return this.mediaAritmetica;
	}
	
	public float getMediaPonderata() {
		return this.mediaPonderata;
	}
	
	public int getSize() {
		return corsiByName.size();
	}
	
	
	public void calcolaStatistiche(){
		int numeroEsami = 0;
		int sommaCFUTotali= 0;
		int sommaCFUConseguiti = 0;
		int sommaCFUConseguitiFannoMedia = 0;
		int sommaVotiPesati = 0;
		int sommaVotiNonPesati = 0;
		
		for (CorsoLibretto corso : corsiByName) {
			int cfu = corso.getCFU();
			boolean hasDate = corso.getDate() != null;
			int mark = corso.getMark();
			
			if (cfu <= 0) {
				continue;
			}
			
			sommaCFUTotali += cfu;
			
			if (mark == -1 || mark >= 18) {					//Esame conseguito
				if (hasDate) {								//Ha una data -> Il voto non è inserito graficamente con lo slider
					sommaCFUConseguiti += cfu;
				}
				if (mark >= 18) {							//Esame conseguito e non idoneità
					numeroEsami++;
					sommaCFUConseguitiFannoMedia += cfu;
					if (mark == 31) {						//Lode = 30?
						mark = 30;
					}
					sommaVotiPesati += mark * cfu;
					sommaVotiNonPesati += mark;
				}
			}
		}
		
		float mediaPonderata = sommaVotiPesati / (float) sommaCFUConseguitiFannoMedia;
		mediaPonderata = Math.round(mediaPonderata * (float) 1000) / (float) 1000;
		this.mediaPonderata = mediaPonderata;
		
		float mediaAritmetica = sommaVotiNonPesati/ (float) numeroEsami;
		mediaAritmetica = Math.round(mediaAritmetica * (float) 1000) / (float) 1000;
		this.mediaAritmetica = mediaAritmetica;
		
		this.nCFUTotali = sommaCFUTotali;
		this.nCFUConseguiti = sommaCFUConseguiti;
	}
	
	private Comparator<CorsoLibretto> sorterCorsiByDate = new Comparator<CorsoLibretto>() {
		public int compare(CorsoLibretto c1, CorsoLibretto c2) {
			Date c1Date = c1.getDate();
			Date c2Date = c2.getDate();
			if (c1Date == null && c2Date == null) {
				return 0;
			} else if (c1Date == null) {
				return 1;
			} else if (c2Date == null) {
				return -1;
			} else {
				return c1Date.compareTo(c2Date);
			}
		}
	};
	
	/**
	 * Compara per [voto esiste] [nome]
	 */
	private Comparator<CorsoLibretto> sorterCorsiByName = new Comparator<CorsoLibretto>() {
		public int compare(CorsoLibretto c1, CorsoLibretto c2) {
			String nome1 = c1.getName();
			String nome2 = c2.getName();

			int voto1 = 1;
			if (c1.getMark() == 0 || c1.getDate() == null) {
				voto1 = -1;
			}
			int voto2 = 1;
			if (c2.getMark() == 0 || c2.getDate() == null) {
				voto2 = -1;
			}

			if (voto1 == voto2) {
				return nome1.compareTo(nome2);
			}
			if (voto1 < 0) {
				return 1;
			} else if (voto2 < 0) {
				return -1;
			}
			return nome1.compareTo(nome2);
		}
	};

	public static class CorsoLibretto {
		private String name;
		private int cfu;
		private Date date;
		private int mark;
		
		private static SimpleDateFormat parserSDF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		
		/**
		 * @param name Nome
		 * @param cfuString CFU
		 * @param dateString Data conseguito
		 * @param markString Voto
		 */
		public CorsoLibretto(String name, String cfuString, String dateString, String markString) {
			this.name = name;
			
			if(cfuString == null) {
				this.cfu = 0;
			} else {
				try {
					this.cfu = Integer.parseInt(cfuString);
				} catch(NumberFormatException e) {
					this.cfu = 0;
				}
			}
			
			if(dateString == null) {
				this.date = null;
			} else {
				try {
					this.date = parserSDF.parse(dateString);
				} catch (ParseException e) {
					this.date = null;
				}
			}
			
			if (markString == null) {
				this.mark = 0;
			} else if (markString.equalsIgnoreCase("30L")) {
				this.mark = 31;
			} else if (markString.toLowerCase(Locale.ITALIAN).startsWith("sup") || markString.toLowerCase(Locale.ITALIAN).startsWith("id")) {
				mark = -1;
			} else {
				try {
					this.mark = Integer.parseInt(markString);
				} catch (NumberFormatException e) {
					this.mark = 0;
				}
			}
		}

		public String getName() {
			return name;
		}

		public int getCFU() {
			return cfu;
		}

		public Date getDate() {
			return date;
		}
		
		public String getDateString() {
			if (this.date == null) {
				return null;
			}
			return parserSDF.format(this.date);
		}
		
		/**
		 * @return 18-30, 31=30Lode, -1=SUPERATO, 0=Nessun voto
		 */
		public int getMark() {
			return mark;
		}
		
		public void setMark(int mark) {
			this.mark = mark;
		}
		
		@Override
		protected Object clone() {
			// return new CorsoLibretto(name, cfu, date, mark);
			return this;
		}
	}
}
