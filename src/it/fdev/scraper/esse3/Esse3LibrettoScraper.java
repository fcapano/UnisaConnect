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
				String courseName = cells.get(1).text();
				courseName = courseName.substring(courseName.indexOf("-") + 1);
				String courseCFU = cells.get(6).text();
				String courseMarkDate = cells.get(9).text();
				String courseMark;
				String courseDate;
				try {
					courseMark = courseMarkDate.substring(0, courseMarkDate.indexOf("-"));
					courseDate = courseMarkDate.substring(courseMarkDate.indexOf("-")+1);
				} catch(Exception e) {
					courseMark = "";
					courseDate = "";
				}
				corsiList.add(new CorsoLibretto(courseName.replace("\u00a0","").trim(), courseCFU.replace("\u00a0","").trim(), courseDate.replace("\u00a0","").trim(), courseMark.replace("\u00a0","").trim()));
			}
			return corsiList;
		}
		return null;
	}
}
