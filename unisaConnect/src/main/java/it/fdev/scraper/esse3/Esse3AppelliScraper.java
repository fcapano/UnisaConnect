package it.fdev.scraper.esse3;

import android.content.Context;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import it.fdev.unisaconnect.data.Appelli;
import it.fdev.unisaconnect.data.Appelli.Appello;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3AppelliScraper extends Esse3BasicScraper {
	public final String appelliDisponibiliURL = "https://esse3web.unisa.it/unisa/auth/studente/Appelli/AppelliF.do";
	public final String appelliPrenotatiURL = "https://esse3web.unisa.it/unisa/auth/studente/Appelli/BachecaPrenotazioni.do";
	private ArrayList<Appello> listaAppelliDisponibili;
	private ArrayList<Appello> listaAppelliPrenotati;

	public Esse3AppelliScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}

	@Override
	public LoadStates startScraper() {
		try {
			listaAppelliDisponibili = scraperStepAppelliDisponibili();
			listaAppelliPrenotati = scraperStepAppelliPrenotati();
			// if (listaAppelliDisponibili != null)
			// Log.d(Utils.TAG, "Ci sono #" + listaAppelliDisponibili.size() + " appelli disponibili");
			// if (listaAppelliPrenotati != null)
			// Log.d(Utils.TAG, "Ci sono #" + listaAppelliPrenotati.size() + " appelli prenotati");
			mDataManager.setAppelli(new Appelli(listaAppelliDisponibili, listaAppelliPrenotati));
			return LoadStates.FINISHED;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401)
				return LoadStates.WRONG_DATA;
			else
				return LoadStates.UNKNOWN_PROBLEM;
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			return LoadStates.UNKNOWN_PROBLEM;
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
				if (rows.size() < 5) {
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

				String subscribedText = rows.get(1).child(0).text().trim();
				if (!subscribedText.matches("Numero Iscrizione: [0-9]+ su [0-9]+")) {
					Log.d(Utils.TAG, "Error interpeting subscribedText: " + subscribedText);
					continue;
				}
				String subscribedNum = subscribedText.substring(subscribedText.lastIndexOf(" ")).trim();

				String date = rows.get(4).child(0).text().trim();
				if (!date.matches("[0-9]+\\/[0-9]+\\/[0-9]+")) {
					Log.d(Utils.TAG, "Error interpeting dateText: " + date);
					date = "";
				}

				String time = rows.get(4).child(1).text().trim();
				if (!time.isEmpty() && !time.matches("[0-9]+\\:[0-9]+")) {
					Log.d(Utils.TAG, "Error interpeting timeText: " + time);
					time = "";
				}

				String location = rows.get(4).child(3).text().trim();

				appelliPrenotatiList.add(new Appello(name, date, time, description, subscribedNum, location));
			}
		}
		return appelliPrenotatiList;
	}

	private ArrayList<Appello> scraperStepAppelliDisponibili() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(appelliDisponibiliURL);
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
				appelliList.add(new Appello(name, date, null, description, subscribedNum, null));
			}
		}
		return appelliList;
	}

}
