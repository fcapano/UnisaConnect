package it.fdev.scraper.esse3;

import it.fdev.unisaconnect.data.Pagamenti;
import it.fdev.unisaconnect.data.Pagamenti.Pagamento;
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

public class Esse3PagamentiScraper extends Esse3BasicScraper {
	public final String pagamentiURL = "https://esse3web.unisa.it/unisa/auth/studente/Tasse/TasseDaPagare.do";

	public Esse3PagamentiScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}

	@Override
	public LoadStates startScraper() {
		try {
			Pagamenti pagamenti = scraperStepPagamenti();
			if (pagamenti != null) {
				mDataManager.setPagamenti(pagamenti);
//				dataManager.saveData();
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

	private Pagamenti scraperStepPagamenti() throws HttpStatusException, IOException, InterruptedException {
		Document document = scraperGetUrl(pagamentiURL);
		if (document == null) {
			return null;
		}
		ArrayList<Pagamento> pagamentiList = new ArrayList<Pagamento>();
		Elements detailsTables = document.getElementsByClass("detail_table");
		Log.d(Utils.TAG, "#DET_TAB: " + detailsTables.size());
		if (detailsTables.size() > 0) {
			Elements rows = detailsTables.get(0).getElementsByTag("tr");
			Log.d(Utils.TAG, "#DET_TAB_ROWS: " + rows.size());
			for (int i = 1; i < rows.size() - 1; i++) {
				Element row = rows.get(i);
				Elements cells = row.getElementsByTag("td");
				Log.d(Utils.TAG, "#DET_TAB_ROW_CELLS: " + cells.size());
				if (cells.size() != 4) {
					return null;
				}

				String pagamentoScadenza = cells.get(0).text();
				String pagamentoTitolo = cells.get(1).text();
				String pagamentoDescrizione = cells.get(2).text();
				String pagamentoImporto = cells.get(3).text();

				pagamentiList.add(new Pagamento(pagamentoTitolo.trim(), pagamentoDescrizione.trim(), pagamentoImporto.trim(), pagamentoScadenza.trim()));
			}
			return new Pagamenti(pagamentiList);
		}
		return null;
	}

}
