package it.fdev.scrapers;

import it.fdev.unisaconnect.AppelliFragment;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.data.Appello;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3AppelliPrenotatiScraper extends Esse3BasicScraper {
	public final String appelliPrenotatiURL = "https://esse3web.unisa.it/unisa/auth/studente/Appelli/BachecaPrenotazioni.do";
	private AppelliFragment callerLibrettoFragment;
	private ArrayList<Appello> listaAppelliPrenotati;

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		int loginResCode = super.doInBackground(activities);
		if (loginResCode != 0)
			return loginResCode;

//		publishProgress(loadStates.SYNCING);

		try {
			listaAppelliPrenotati = scraperStepAppelliPrenotati();
			if (listaAppelliPrenotati != null)
				Log.d(Utils.TAG, "Ci sono #" + listaAppelliPrenotati.size() + " appelli prenotati");
			publishProgress(loadStates.FINISHED);
			return 0;
			// }
			// return -1;
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
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Esse3BasicScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case FINISHED:
			callerLibrettoFragment.mostraAppelliPrenotati(listaAppelliPrenotati);
			break;
		default:
			break;
		}
	}

	private ArrayList<Appello> scraperStepAppelliPrenotati() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(appelliPrenotatiURL);
		if (document == null) {
			return null;
		}
		ArrayList<Appello> appelliPrenotatiList = new ArrayList<Appello>();
		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			for (Element table : detailsTables) {
				Elements rows = table.getElementsByTag("tr");
				if (rows.size() != 5) {
					continue;
				}

				String row1Text = rows.get(0).child(0).text().trim();
				String[] row1Data = row1Text.split(" \\- \\[[0-9]+\\] \\- ");
				if (row1Data.length != 2) {
					Log.d(Utils.TAG, "Error interpeting row1: " + row1Text);
					Log.d(Utils.TAG, "Split length: " + row1Data.length);
					continue;
				}
				String name = row1Data[0].trim();
				String description = row1Data[1].trim();

				String row2Text = rows.get(1).child(0).text().trim();
				if (!row2Text.matches("Numero Iscrizione: [0-9]+ su [0-9]+")) {
					Log.d(Utils.TAG, "Error interpeting row2: " + row2Text);
					continue;
				}
				String subscribedNum = row2Text.substring(row2Text.lastIndexOf(" ")).trim();

				String row5Text = rows.get(4).child(0).text().trim();
				if (!row5Text.matches("[0-9]+\\/[0-9]+\\/[0-9]+")) {
					Log.d(Utils.TAG, "Error interpeting row5: " + row5Text);
					continue;
				}
				String date = row5Text.trim();

				appelliPrenotatiList.add(new Appello(name, date, description, subscribedNum));
			}
		}
		return appelliPrenotatiList;
	}

	public void setCallerAppelliFragment(AppelliFragment caller) {
		callerLibrettoFragment = caller;
	}
}
