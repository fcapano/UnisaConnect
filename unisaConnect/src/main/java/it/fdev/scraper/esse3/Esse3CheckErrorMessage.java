package it.fdev.scraper.esse3;

import android.content.Context;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

/**
 * Frammento che si occupa dell'accesso alla esse3
 * 
 * @author francesco
 * 
 */
public class Esse3CheckErrorMessage extends Esse3BasicScraper {

	private final String homeUrl = "http://esse3web.unisa.it/unisa/Home.do";
	
	private String errorMessage = null;
	
	public Esse3CheckErrorMessage(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}

	@Override
	public LoadStates startScraper() {
		try {
			Document document = scraperGetUrl(homeUrl);
			Elements messaggioEl = document.getElementsContainingOwnText("Messaggio");
			if (messaggioEl.isEmpty() || messaggioEl.first().nextElementSibling() == null) {
				return LoadStates.WRONG_DATA;
			} else {
				errorMessage = messaggioEl.first().nextElementSibling().text();
				return LoadStates.ESSE3_PROBLEM;
			}
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
//			int code = e.getStatusCode();
//			if (code == 401) {
//				return LoadStates.UNKNOWN_PROBLEM;
//			} else {
			errorMessage = "Il servizio ESSE3 è temporaneamente non disponibile, puoi verificare il problema andando su: esse3web.unisa.it";
			return LoadStates.ESSE3_PROBLEM;
//			}
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			return LoadStates.UNKNOWN_PROBLEM;
		}
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
