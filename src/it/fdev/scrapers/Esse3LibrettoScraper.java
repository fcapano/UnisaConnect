package it.fdev.scrapers;

import it.fdev.unisaconnect.LibrettoFragment;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.data.LibrettoCourse;
import it.fdev.unisaconnect.data.LibrettoDB;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

/**
 * Frammento che si occupa del riperimento del libretto dalla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3LibrettoScraper extends Esse3BasicScraper {
	public final String librettoURL = "https://esse3web.unisa.it/unisa/auth/studente/Libretto/LibrettoHome.do";

	private LibrettoFragment callerLibrettoFragment;

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		int loginResCode = super.doInBackground(activities);
		if (loginResCode != 0)
			return loginResCode;

		publishProgress(loadStates.SYNCING);

		ArrayList<LibrettoCourse> corsiList = new ArrayList<LibrettoCourse>();

		try {
			corsiList = scraperStepLibretto();
			if (corsiList != null) {
				Log.d(Utils.TAG, "Ci sono #" + corsiList.size() + " corsi da inserire");
				LibrettoDB librettoDB = new LibrettoDB(activity);
				try {
					librettoDB.open();
					librettoDB.deleteAllCourses();
					librettoDB.insertCourses(corsiList);
				} finally {
					librettoDB.close();
				}
				publishProgress(loadStates.FINISHED);
				return 0;
			}
			return -1;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401)
				publishProgress(loadStates.WRONG_DATA);
			else
				publishProgress(loadStates.UNKNOWN_PROBLEM);
		} catch (IOException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
		} catch (InterruptedException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
		}
		return -1;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Esse3BasicScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case FINISHED:
			callerLibrettoFragment.mostraCorsi();
			Utils.dismissDialog();
			break;
		default:
			break;
		}
	}

	private ArrayList<LibrettoCourse> scraperStepLibretto() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(librettoURL);
		if (document == null) {
			return null;
		}
		ArrayList<LibrettoCourse> corsiList = new ArrayList<LibrettoCourse>();
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
				String courseMark = cells.get(12).text();
				corsiList.add(new LibrettoCourse(courseName.substring(courseName.indexOf("-") + 1).trim(), courseCFU.trim(), courseMark.trim()));
			}
			return corsiList;
		}
		return null;
	}

	public void setCallerLibrettoFragment(LibrettoFragment caller) {
		callerLibrettoFragment = caller;
	}
}
