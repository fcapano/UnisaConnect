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
//TODO
public class Esse3AutocertificazioniScraper extends Esse3BasicScraper {
	public final String autocertificazioniURL = "https://esse3web.unisa.it/unisa/auth/studente/Certificati/ListaCertificati.do";
	private AppelliFragment callerLibrettoFragment;
	private ArrayList<Appello> listaAppelliDisponibili;

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		int loginResCode = super.doInBackground(activities);
		if (loginResCode != 0)
			return loginResCode;

		publishProgress(loadStates.SYNCING);

		try {
			listaAppelliDisponibili = scraperStepAppelliDisponibili();
			if (listaAppelliDisponibili != null) {
				Log.d(Utils.TAG, "Ci sono #" + listaAppelliDisponibili.size() + " appelli disponibili");
				publishProgress(loadStates.FINISHED);
				return 0;
			}
			return -1;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if(code == 401)
				publishProgress(loadStates.WRONG_DATA);
			else
				publishProgress(loadStates.UNKNOWN_PROBLEM);
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
		}
		return -7;
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
			callerLibrettoFragment.mostraAppelliDisponibili(listaAppelliDisponibili);
			Utils.dismissDialog();
			break;
		default:
			break;
		}
	}

	private ArrayList<Appello> scraperStepAppelliDisponibili() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(autocertificazioniURL);
		if (document == null) {
			return null;
		}
		ArrayList<Appello> appelliList = new ArrayList<Appello>();
		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			Elements rows = detailsTables.get(0).getElementsByTag("tr");
			for (int i = 1; i < rows.size(); i++) {
				Element row = rows.get(i);
				Elements cells = row.getElementsByTag("td");
				if (cells.size() != 9) {
					return null;
				}
				String name = cells.get(1).text().trim();
				String date = cells.get(2).text().trim();
				String description = cells.get(4).text().trim();
				String subscribedNum = cells.get(7).text().trim();
				appelliList.add(new Appello(name, date, description, subscribedNum));
			}
			return appelliList;
		}
		return null;
	}

	public void setCallerAppelliFragment(AppelliFragment caller) {
		callerLibrettoFragment = caller;
	}
}
