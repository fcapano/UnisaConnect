package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentPresenze;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.Presenze;
import it.fdev.unisaconnect.data.Presenze.RiepilogoPresenzeEsame;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Frammento che si occupa dell'accesso alle presenze
 * 
 * @author francesco
 * 
 */
public class PresenzeScraper extends AsyncTask<MainActivity, PresenzeScraper.loadStates, Integer> {

	public boolean isRunning = false;
	
	private final String START_URL 			= "http://www.presenzestudenti.unisa.it/studente";
	private final String AUTH_URL 			= "https://sso.unisa.it/idp/Authn/UserPassword";
	private final String BASE_URL 			= "http://www.presenzestudenti.unisa.it";
	private final String INDEX_URL 			= BASE_URL + "/index_st.php";
	private final String LOGIN_URL 			= BASE_URL + "/informazioni_st.php";
	private final String RIEPILOGO_URL 		= BASE_URL + "/cartellino_studente.php";		// Pagina con il form per scegliere l'esame
	private final String DOM_STUDENTE_URL 	= BASE_URL + "/domXML_studente.php";			// Lista esami
	private final String CARTELLINO_URL 	= BASE_URL + "/ris_cartellino_studente.php";	// Dettagli esame

	private final int REQUEST_TIMEOUT = 30000;
	private final int SLEEP_TIME_REQUESTS = 500;
	
	protected MainActivity activity;
	private SharedPrefDataManager mDataManager;
	private FragmentPresenze callerPresenzeFragment;
	private Presenze presenze = null;

	public static enum loadStates {
		START, SYNCING, NO_INTERNET, NO_DATA, PRESENZE_NOT_AVAILABLE, UNKNOWN_PROBLEM, FINISHED
	};

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		try {
			activity = activities[0];
			publishProgress(loadStates.START);
			
			mDataManager = new SharedPrefDataManager(activity);

			if (!mDataManager.loginDataExists()) {
				publishProgress(loadStates.NO_DATA);
				return -1;
			}
			String user = mDataManager.getUser() + "@studenti.unisa.it";
			String pass = mDataManager.getPass();
			
			Map<String, String> cookies;
			
			Response res;
			Document document;
			
			// Set initial cookies
			res = Jsoup.connect(START_URL)
					.method(Method.GET)
					.timeout(REQUEST_TIMEOUT)
					.execute();
			cookies = res.cookies();
			Thread.sleep(SLEEP_TIME_REQUESTS);
			
			// Login
			// Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(AUTH_URL);
		    String cookiesString = "";
		    for (Entry<String,String> cCookie : cookies.entrySet()) {
		    	cookiesString += cCookie.getKey() + "=" + cCookie.getValue() + "; ";
		    }
		    httppost.setHeader("Cookie", cookiesString);
		    // Add your data
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		    nameValuePairs.add(new BasicNameValuePair("j_username", user));
		    nameValuePairs.add(new BasicNameValuePair("j_password", pass));
		    nameValuePairs.add(new BasicNameValuePair("profilo", "s"));
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    try {
		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        String html = EntityUtils.toString(response.getEntity());
		        document = Jsoup.parse(html, BASE_URL);
		        System.out.println(document);
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		    	e.printStackTrace();
		    	publishProgress(loadStates.UNKNOWN_PROBLEM);
				return -1;
		    }
			
			Thread.sleep(SLEEP_TIME_REQUESTS);
			
			// Check wrong user/pwd
//			Elements datiErrati = document.getElementById("container2").getElementsContainingText("I dati di accesso sono errati");
			Element datiErrati = document.getElementById("error");
			if (datiErrati != null) {
				publishProgress(loadStates.NO_DATA);
				return -1;
			}
			
			publishProgress(loadStates.SYNCING);
			
			res = Jsoup.connect(RIEPILOGO_URL)
					.cookies(cookies)
					.method(Method.GET)
					.timeout(REQUEST_TIMEOUT)
					.execute();
			Thread.sleep(SLEEP_TIME_REQUESTS);
			
			// Get esami
			res = Jsoup.connect(DOM_STUDENTE_URL)
					.cookies(cookies)
					.method(Method.GET)
					.timeout(REQUEST_TIMEOUT)
					.execute();
			Thread.sleep(SLEEP_TIME_REQUESTS);
			
			document = Jsoup.parse(res.body(), "", Parser.xmlParser());
			
			ArrayList<Presenze.Esame> listaEsami = new ArrayList<Presenze.Esame>();
			
			Elements timbraturaList = document.getElementsByTag("TIMBRATURA");
			
			if (timbraturaList.size() <= 0) {
				publishProgress(loadStates.PRESENZE_NOT_AVAILABLE);
				return -1;
			}
			
			Element timbratura, codCorso;
			for (int i=0; i<timbraturaList.size(); i++) {
				ArrayList<RiepilogoPresenzeEsame> listaAttivita = new ArrayList<Presenze.RiepilogoPresenzeEsame>();
				
				timbratura = timbraturaList.get(i);
				String id = timbratura.attr("id");
				String aa_off = timbratura.attr("aa_off");
				codCorso = timbratura.getElementsByTag("CODCORSO").get(0);
				String desccorso = codCorso.attr("desccorso");
				String cod = codCorso.text();
				Log.d(Utils.TAG, "id: " + id + "   aa_off:" + aa_off + "   desccorso:" + desccorso + "   cod:" + cod);

				Document dettagliEsameDocument = Jsoup.connect(CARTELLINO_URL)
						.data("anno", aa_off)
						.data("select_aa_off", "7")
						.data("corso", cod)
						.data("descr_corso", desccorso)
						.data("select_codcorso" , "2")
						.cookies(cookies)
						.method(Method.GET)
						.timeout(REQUEST_TIMEOUT)
						.post();

				Thread.sleep(500);
				
				Elements tables = dettagliEsameDocument.getElementsByTag("table");
//				Element detailsTable = tables.get(0).getElementsByTag("tbody").get(0);
				Element summaryTable = tables.get(1).getElementsByTag("tbody").get(0);
				Elements cRowColumns;
				Elements rowList;
				
//				rowList = detailsTable.getElementsByTag("tr");
//				for (int j=0; j< rowList.size(); j++) {
//					cRowColumns = rowList.get(j).getElementsByTag("td");
//					String giorno = cRowColumns.get(0).text();
//					String timbrature = cRowColumns.get(1).text();
//					String attivita = cRowColumns.get(2).text();
//					String ore = cRowColumns.get(3).text();
//					String aula = cRowColumns.get(4).text();
//					listaPresenze.add(new DettagliPresenza(giorno, timbrature, attivita, ore, aula));
//					Log.d(Utils.TAG, "giorno:" + giorno + "   timbrature:" + timbrature + "   attivita:" + attivita + "   ore:" + ore + "   aula:" + aula);
//				}
				
				rowList = summaryTable.getElementsByTag("tr");
				for (int j=0; j< rowList.size(); j++) {
					cRowColumns = rowList.get(j).getElementsByTag("td");
					String attivita = cRowColumns.get(0).text();
					String totaleOreStudente = cRowColumns.get(1).text();
					String totaleOreDocente = cRowColumns.get(2).text();
					String frequenza = cRowColumns.get(3).text();
					listaAttivita.add(new RiepilogoPresenzeEsame(attivita, totaleOreStudente, totaleOreDocente, frequenza));
					Log.d(Utils.TAG, "attivita:" + attivita + "   totaleOreStudente:" + totaleOreStudente + "   totaleOreDocente:" + totaleOreDocente + "   frequenza:" + frequenza);
				}
				Presenze.Esame esame = new Presenze.Esame(desccorso, aa_off, listaAttivita);
				listaEsami.add(esame);
			}
			presenze = new Presenze(listaEsami);
			publishProgress(loadStates.FINISHED);
		} catch (Exception e) {
			presenze = null;
			publishProgress(loadStates.UNKNOWN_PROBLEM);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(PresenzeScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case START:
			activity.setLoadingText(R.string.autenticazione_esse3);
			break;
		case SYNCING:
			activity.setLoadingText(R.string.sincronizzazione_esse3);
			break;
		case PRESENZE_NOT_AVAILABLE:
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerPresenzeFragment != null) {
				callerPresenzeFragment.mostraPresenze(null);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
		case NO_DATA:
			Utils.createAlert(activity, activity.getString(R.string.dati_errati), BootableFragmentsEnum.ACCOUNT, false);
			break;
		case FINISHED:
			activity.setLoadingText(R.string.completamento_esse3);
			if (callerPresenzeFragment != null) {
				callerPresenzeFragment.mostraPresenze(presenze);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
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

	public void setCallerPresenzeFragment(FragmentPresenze callerMenuFragment) {
		this.callerPresenzeFragment = callerMenuFragment;
	}

}
