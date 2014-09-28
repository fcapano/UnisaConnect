package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentBus;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.model.bustrattaendpoint.Bustrattaendpoint;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusCorsa;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusFermata;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusTratta;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

public class BusScraper {

	// public static final String BASE_URL = "http://192.168.1.199:8888/unisaconnectbus/";
	public static final String BASE_URL = "http://unisaconnectbus.appspot.com/unisaconnectbus/";
	public static final String CERCA_STAZIONE_URL = BASE_URL + "cercastazione";
	public static final String TRATTE_STAZIONE_URL = BASE_URL + "gettrattestazione";

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, UNKNOWN_PROBLEM, FINISHED
	};

	public static class AddQuoteAsyncTask extends AsyncTask<String, Void, BusTratta> {
		Context context;
		private ProgressDialog pd;

		public AddQuoteAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Adding the Quote...");
			pd.show();
		}

		protected BusTratta doInBackground(String... params) {
			BusTratta response = null;
			try {
				Bustrattaendpoint.Builder builder = new Bustrattaendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Bustrattaendpoint service = builder.build();
				BusTratta tratta = new BusTratta();
				response = service.insertBusTratta(tratta).execute();
			} catch (Exception e) {
				Log.d("Could not Add Quote", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(BusTratta quote) {
			// Clear the progress dialog and the fields
			pd.dismiss();
//			editMessage.setText("");
//			editAuthorName.setText("");

			// Display success message to user
//			Toast.makeText(getBaseContext(), "Quote added succesfully", Toast.LENGTH_SHORT).show();
		}
	}

	public static class CercaStazione extends AsyncTask<Object, BusScraper.loadStates, Integer> {
		public boolean isRunning = false;
		protected MainActivity mActivity;
		private FragmentBus callerBusFragment;

		ArrayList<String> risultatiRicerca = new ArrayList<String>();

		@Override
		protected Integer doInBackground(Object... params) {

			try {
				mActivity = (MainActivity) params[0];
				String query = (String) params[1];
				Log.d(Utils.TAG, "Cerco le stazioni - " + query);
				publishProgress(loadStates.START);
				Connection connection = Jsoup.connect(CERCA_STAZIONE_URL);
				connection.timeout(30000);
				connection.data("q", query);
				Document document = connection.post();
				Log.d(Utils.TAG, "Analizzo le stazioni - " + query);
				Elements stazioniEl = document.getElementsByTag("stazione");
				risultatiRicerca.clear();
				for (Element cStazioneEl : stazioniEl) {
					Log.d(Utils.TAG, "---->  " + cStazioneEl.text());
					risultatiRicerca.add(cStazioneEl.text());
				}
				publishProgress(loadStates.FINISHED);
			} catch (Exception e) {
				Log.w(Utils.TAG, "---->  Errore!", e);
				e.printStackTrace();
				publishProgress(loadStates.UNKNOWN_PROBLEM);
				return -1;
			}
			return 0;
		}

		@Override
		protected void onProgressUpdate(BusScraper.loadStates... values) {
			super.onProgressUpdate(values);
			switch (values[0]) {
			case START:

				break;
			case NO_INTERNET:
			case UNKNOWN_PROBLEM:
				if (callerBusFragment == null) {
					return;
				}
				callerBusFragment.showRisultatiRicerca(null);
				break;
			case FINISHED:
				if (callerBusFragment == null) {
					return;
				}
				callerBusFragment.showRisultatiRicerca(risultatiRicerca);
				break;
			default:
				break;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isRunning = true;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			isRunning = false;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			isRunning = false;
		}

		public void setCallerBusFragment(FragmentBus caller) {
			callerBusFragment = caller;
		}
	}

	public static class GetTratteStazione extends AsyncTask<Object, BusScraper.loadStates, Integer> {
		public boolean isRunning = false;
		protected MainActivity mActivity;
		private FragmentBus callerBusFragment;

		ArrayList<BusTratta> tratteRicerca = new ArrayList<BusTratta>();

		@Override
		protected Integer doInBackground(Object... params) {

			try {
				mActivity = (MainActivity) params[0];
				String stazione = (String) params[1];
				Log.d(Utils.TAG, "Cerco le tratte - " + stazione);
				publishProgress(loadStates.START);
				Connection connection = Jsoup.connect(TRATTE_STAZIONE_URL);
				connection.timeout(30000);
				connection.data("q", stazione);
				Document document = connection.post();
				Log.d(Utils.TAG, "Analizzo le tratte - " + stazione);
				Elements stazioniEl = document.getElementsByTag("tratta");
				tratteRicerca.clear();
				for (Element cTrattaEl : stazioniEl) {
					BusTratta cTratta = xmlToTratta(cTrattaEl);
					tratteRicerca.add(cTratta);
				}
				publishProgress(loadStates.FINISHED);
			} catch (Exception e) {
				Log.w(Utils.TAG, "---->  Errore!", e);
				e.printStackTrace();
				publishProgress(loadStates.UNKNOWN_PROBLEM);
				return -1;
			}
			return 0;
		}

		public BusTratta xmlToTratta(Element trattaXML) {
			String compagnia = trattaXML.getElementsByTag("compagnia").get(0).text();
			String capolinea = trattaXML.getElementsByTag("capolinea").get(0).text();
			List<BusCorsa> corse = new ArrayList<BusCorsa>();
			Elements corseList = trattaXML.getElementsByTag("corsa");
			for (Element cCorsaEl : corseList) {
				BusCorsa cCorsa = xmlToCorsa(cCorsaEl);
				corse.add(cCorsa);
			}
			List<String> stazioniVersoUni = new ArrayList<String>();
			Elements stazioneVersoUniList = trattaXML.getElementsByTag("stazioneVU");
			for (Element cStazioneEl : stazioneVersoUniList) {
				String cStazione = cStazioneEl.text();
				stazioniVersoUni.add(cStazione);
			}
			List<String> stazioniDaUni = new ArrayList<String>();
			Elements stazioneDaUniList = trattaXML.getElementsByTag("stazioneDU");
			for (Element cStazioneEl : stazioneDaUniList) {
				String cStazione = cStazioneEl.text();
				stazioniDaUni.add(cStazione);
			}
			BusTratta tratta = new BusTratta();
			tratta.setCompagnia(compagnia);
			tratta.setCapolinea(capolinea);
			tratta.setCorse(corse);
			tratta.setStazioniVersoUni(stazioniVersoUni);
			tratta.setStazioniDaUni(stazioniDaUni);
			return tratta;
			// return new BusTratta(compagnia, capolinea, corse, stazioniVersoUni, stazioniDaUni);
		}

		public BusCorsa xmlToCorsa(Element corsaXML) {
			boolean versoUni = corsaXML.getElementsByTag("verso_uni").get(0).text().equalsIgnoreCase("true");
			String giorni = corsaXML.getElementsByTag("giorni").get(0).text();
			int oraPartenza = Integer.parseInt(corsaXML.getElementsByTag("ora").get(0).text());
			List<BusFermata> fermate = new ArrayList<BusFermata>();
			Elements fermateList = corsaXML.getElementsByTag("fermata");
			for (Element cFermataEl : fermateList) {
				BusFermata cFermata = xmlToFermata(cFermataEl);
				fermate.add(cFermata);
			}
			BusCorsa corsa = new BusCorsa();
			corsa.setVersoUni(versoUni);
			corsa.setGiorni(giorni);
			corsa.setOraPartenza(oraPartenza);
			corsa.setFermate(fermate);
			return corsa;
			// return new BusCorsa(versoUni, giorni, oraPartenza, fermate);
		}

		public BusFermata xmlToFermata(Element fermataXML) {
			String stazione = fermataXML.getElementsByTag("stazione").get(0).text();
			int ora = Integer.parseInt(fermataXML.getElementsByTag("ora").get(0).text());
			int posizione = Integer.parseInt(fermataXML.getElementsByTag("posizione").get(0).text());
			BusFermata fermata = new BusFermata();
			fermata.setStazione(stazione);
			fermata.setOra(ora);
			fermata.setPosizione(posizione);
			return fermata;
			// return new BusFermata(stazione, ora, posizione);
		}

		@Override
		protected void onProgressUpdate(BusScraper.loadStates... values) {
			super.onProgressUpdate(values);
			switch (values[0]) {
			case START:

				break;
			case NO_INTERNET:
			case UNKNOWN_PROBLEM:
				if (callerBusFragment == null) {
					return;
				}
				callerBusFragment.showTratteRicerca(null);
				break;
			case FINISHED:
				if (callerBusFragment == null) {
					return;
				}
				callerBusFragment.showTratteRicerca(tratteRicerca);
				break;
			default:
				break;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isRunning = true;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			isRunning = false;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			isRunning = false;
		}

		public void setCallerBusFragment(FragmentBus caller) {
			callerBusFragment = caller;
		}
	}
}
