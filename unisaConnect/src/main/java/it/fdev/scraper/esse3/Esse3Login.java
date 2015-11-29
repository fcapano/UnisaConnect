package it.fdev.scraper.esse3;

import android.content.Context;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3Login extends Esse3BasicScraper {

	private final String startUrl = "https://esse3web.unisa.it/unisa/auth/Logon.do";
	
	private boolean chooseCareer;
	private SharedPrefDataManager mDataManager;
	
	public Esse3Login(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID, boolean chooseCareer) {
		super(context, dataManager, base64login, broadcastID);
		this.chooseCareer = chooseCareer;
		mDataManager = SharedPrefDataManager.getInstance(context);
	}

	@Override
	public LoadStates startScraper() {
		try {
			Document result;
			try {
				scraperGetUrl(startUrl);
			} catch (Exception e) {
				// Come risposta alla prima richiesta si ha un 401, perchè non vengono inviati cookie
				// La risposta contiene anche l'header Set-Cookie. Il cookie è necessario per autenticarsi
			}
			result = scraperGetUrl(startUrl);
			if (result == null) {
				Log.d(Utils.TAG, "I'm lost 0");
				return LoadStates.NO_INTERNET;
			}
			if (!chooseCareer) {
				return LoadStates.FINISHED;
			}
			String nextUrl = chooseCareer(result);
			if (nextUrl == null) {
				Log.d(Utils.TAG, "I'm lost 1");
				return LoadStates.NO_INTERNET;
			}
			if (!nextUrl.isEmpty()) {
				result = scraperGetUrl(nextUrl);
				if (result == null) {
					Log.d(Utils.TAG, "I'm lost 2");
					return LoadStates.NO_INTERNET;
				}
			}
			nextUrl = chooseCareer(result);
			if (nextUrl == null) {
				Log.d(Utils.TAG, "I'm lost 3");
				return LoadStates.NO_INTERNET;
			}
			if (!nextUrl.isEmpty()) {
				Log.d(Utils.TAG, "I'm lost 4");
				return LoadStates.NO_INTERNET;
			}
			return LoadStates.FINISHED;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401) {
				return LoadStates.WRONG_DATA;
			} else {
				return LoadStates.UNKNOWN_PROBLEM;
			}
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
		}
		return LoadStates.UNKNOWN_PROBLEM;
	}
	
	private String chooseCareer(Document document) {
		if (document == null) {
			return null;
		}
		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			int tipoCorso = mDataManager.getTipoCorso();
			if (detailsTables.size() < tipoCorso || tipoCorso < 1) {
				mDataManager.setTipoCorso(1);
				tipoCorso = 1;
			}
			Element firstRow = detailsTables.get(0).getElementsByTag("tr").get(tipoCorso);
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
	
}
