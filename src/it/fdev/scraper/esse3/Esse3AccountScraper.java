package it.fdev.scraper.esse3;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

import java.io.IOException;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.util.Log;

public class Esse3AccountScraper extends Esse3BasicScraper {
	public final String accountURL = "https://esse3web.unisa.it/unisa/Home.do";

	public Esse3AccountScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}

	@Override
	public LoadStates startScraper() {
		try {
			String nomeCognome = scraperStepAccount();
			if (nomeCognome != null) {
				mDataManager.setNomeCognome(nomeCognome);
				return LoadStates.FINISHED;
			} else
				return LoadStates.NO_DATA;
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

	private String scraperStepAccount() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(accountURL);
		if (document == null) {
			return null;
		}
		Element datiContainer = document.getElementById("gu-hpstu-boxDatiPersonali");
		if (datiContainer == null) {
			return null;
		}
		return datiContainer.getElementsContainingOwnText("Nome Cognome").first().nextElementSibling().text();
	}

}
