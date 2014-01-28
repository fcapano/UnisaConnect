package it.fdev.scraper.esse3;

import it.fdev.unisaconnect.data.Libretto;
import it.fdev.unisaconnect.data.Libretto.CorsoLibretto;
import it.fdev.unisaconnect.data.LibrettoDB;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.util.Log;

/**
 * Frammento che si occupa del riperimento del libretto dalla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3LibrettoScraper extends Esse3BasicScraper {
	public final String librettoURL = "https://esse3web.unisa.it/unisa/auth/studente/Libretto/LibrettoHome.do";

	public Esse3LibrettoScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}
	
	@Override
	public LoadStates startScraper() {
		Libretto libretto = new Libretto();
		ArrayList<CorsoLibretto> corsiList = new ArrayList<CorsoLibretto>();

		try {
			corsiList = scraperStepLibretto();
			if (corsiList != null) {
				Log.d(Utils.TAG, "Ci sono #" + corsiList.size() + " corsi da inserire");
				libretto.setCorsi(corsiList);
				LibrettoDB librettoDB = new LibrettoDB(mContext);
				try {
					librettoDB.open();
					librettoDB.resetLibretto(libretto);
				} finally {
					librettoDB.close();
				}
				return LoadStates.FINISHED;
			}
			Log.d(Utils.TAG, "Corsi non trovati!");
			return LoadStates.NO_DATA;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401)
				return LoadStates.WRONG_DATA;
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
		}
		return LoadStates.UNKNOWN_PROBLEM;
	}

	private ArrayList<CorsoLibretto> scraperStepLibretto() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(librettoURL);
		if (document == null) {
			return null;
		}
		ArrayList<CorsoLibretto> corsiList = new ArrayList<CorsoLibretto>();
		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			Elements rows = detailsTables.get(0).getElementsByTag("tr");
			for (int i = 1; i < rows.size(); i++) {
				Element row = rows.get(i);
				Elements cells = row.getElementsByTag("td");
				if (cells.size() < 12) {
					return null;
				}
				String courseName = cells.get(2).text();
				String courseCFU = cells.get(10).text();
				String courseDate = cells.get(11).text();
				String courseMark = cells.get(12).text();
				corsiList.add(new CorsoLibretto(courseName.substring(courseName.indexOf("-") + 1).trim(), courseCFU.trim(), courseDate.trim(), courseMark.trim()));
			}
			return corsiList;
		}
		return null;
	}
}
