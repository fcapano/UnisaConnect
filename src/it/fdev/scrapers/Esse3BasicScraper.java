package it.fdev.scrapers;

import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.WifiPreferencesFragment;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3BasicScraper extends AsyncTask<MainActivity, Esse3BasicScraper.loadStates, Integer> {

	public final String startUrl = "https://esse3web.unisa.it/unisa/auth/Logon.do";
	public final String homeUrl = "https://esse3web.unisa.it/unisa/Home.do";
	public final String baseURL = "https://esse3web.unisa.it/unisa/";

	public boolean isRunning = false;

	private SharedPrefDataManager dataManager;
	protected MainActivity activity;
	private String base64login;

	public static enum loadStates {
		START, LOGGED_IN, ANALYZING, SYNCING, SAVING, WRONG_DATA, NO_DATA, NO_INTERNET, UNKNOWN_PROBLEM, FINISHED
	};

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		activity = activities[0];
		dataManager = SharedPrefDataManager.getDataManager(activity);

		publishProgress(loadStates.START);

		if (!dataManager.dataExists()) {
			publishProgress(loadStates.NO_DATA);
			return -1;
		}

		base64login = Base64.encodeToString((dataManager.getUser() + ":" + dataManager.getPass()).getBytes(), Base64.NO_WRAP);

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		// Log.d(Utils.TAG, "\n\n\n|-------   START TSK   -------|");
		try {
			Document result;
			try { // Come risposta alla prima richiesta si ha un 401, perchè non vengono inviati cookie
					// La risposta contiene anche l'header Set-Cookie. Il cookie è necessario per autenticarsi
				scraperGetUrl(startUrl);
			} catch (Exception e) {
			}
			result = scraperGetUrl(startUrl);
			if (result == null) {
				Log.d(Utils.TAG, "I'm lost 0");
				publishProgress(loadStates.NO_INTERNET);
				return -1;
			}
			String nextUrl = scraperStepLogin(result);
			if (nextUrl == null) {
				Log.d(Utils.TAG, "I'm lost 1");
				publishProgress(loadStates.NO_INTERNET);
				return -1;
			}
			if (!nextUrl.isEmpty()) {
				Log.d(Utils.TAG, "Carriera: " + nextUrl);
				result = scraperGetUrl(nextUrl);
				if (result == null) {
					Log.d(Utils.TAG, "I'm lost 2");
					publishProgress(loadStates.NO_INTERNET);
					return -1;
				}
			}
			nextUrl = scraperStepLogin(result);
			if (nextUrl == null) {
				Log.d(Utils.TAG, "I'm lost 1");
				publishProgress(loadStates.NO_INTERNET);
				return -1;
			}
			if (!nextUrl.isEmpty()) {
				Log.d(Utils.TAG, "I'm lost 3");
				publishProgress(loadStates.NO_INTERNET);
				return -1;
			}
			publishProgress(loadStates.LOGGED_IN);
			return 0;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401)
				publishProgress(loadStates.WRONG_DATA);
			else
				publishProgress(loadStates.UNKNOWN_PROBLEM);
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
		}
		return -1;
	}

	@Override
	protected void onProgressUpdate(Esse3BasicScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case START:
			Utils.createDialog(activity, activity.getString(R.string.connesione_esse3), false);
			break;
		case LOGGED_IN:
//			Utils.createDialog(activity, activity.getString(R.string.login_ok), false);
			break;
		case ANALYZING:
			// Utils.createDialog(activity,
			// activity.getString(R.string.sincronizzazione_esse3), false);
			break;
		case SYNCING:
			Utils.createDialog(activity, activity.getString(R.string.sincronizzazione_esse3), false);
			break;
		case NO_DATA:
			Utils.createAlert(activity, activity.getString(R.string.dati_non_validi), new WifiPreferencesFragment(), false);
			break;
		case WRONG_DATA:
			Utils.createAlert(activity, activity.getString(R.string.dati_errati), new WifiPreferencesFragment(), false);
			break;
		case NO_INTERNET:
			Utils.goToInternetError(activity, null);
			break;
		case UNKNOWN_PROBLEM:
			Utils.createAlert(activity, activity.getString(R.string.problema_di_connessione_generico), null, true);
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

	private String scraperStepLogin(Document document) {
		if (document == null) {
			return null;
		}
		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			Element firstRow = detailsTables.get(0).getElementsByTag("tr").get(1);
			Elements anchors = firstRow.getElementsByTag("a");
			if (anchors.size() == 0) {
				return "";
			}
			String carrieraUrl = anchors.get(anchors.size() - 1).absUrl("href");
			return carrieraUrl;
		} else {
			return "";
		}
	}

	protected Document scraperGetUrl(String url) throws IOException, InterruptedException, HttpStatusException {
		Response res = Jsoup.connect(url).header("Authorization", "Basic " + base64login).method(Method.GET).timeout(10000).execute();
		Document document = res.parse();
		Thread.sleep(500);
		return document;
	}
}
