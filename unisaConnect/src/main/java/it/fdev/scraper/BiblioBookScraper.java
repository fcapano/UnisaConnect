package it.fdev.scraper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.BookDetails;
import it.fdev.utils.Utils;

public class BiblioBookScraper extends IntentService {

	public final static String BROADCAST_STATE_BIBLIO_BOOK = "it.fdev.biblio.status_book";

	public static boolean isRunning = false;

	private Context mContext;

	public BiblioBookScraper() {
		super("it.fdev.biblio.book_scraper_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String url = intent.getStringExtra("URL");
			if (url == null) {
				isRunning = false;
				stopForeground(true);
				stopSelf();
				return;
			}
			
			Log.d(Utils.TAG, "Cerco libro. URL: " + url);

			isRunning = true;
			mContext = getApplicationContext();

			Utils.sendLoadingMessage(mContext, R.string.cerco_libro);

			BookDetails book = getSearchResults(url);
			Utils.broadcastStatus(mContext, BROADCAST_STATE_BIBLIO_BOOK, "status", book);
		} catch (Exception e) {
			Log.e(Utils.TAG, "Biblio search service crashed", e);
			Utils.broadcastStatus(mContext, BROADCAST_STATE_BIBLIO_BOOK, "status", null);
		}
		isRunning = false;
		stopForeground(true);
		stopSelf();
		return;
	}

	private BookDetails getSearchResults(String url) throws IOException {
		Response res = Jsoup.connect(url).method(Method.GET).timeout(30000).execute();
		Document document = res.parse();

		String[] ids = 		{"Autore", "Titolo", "Collocazione", "Edizione", "Pubblicazione", "Descr.", "Serie", "Lingua pubbl.", "Soggetto", "CDD", "ISBN"};
		String[] values = 	new String[11];
		
		for (int i=0; i<ids.length; i++) {
			String cID = ids[i];
			if (document.getElementById(cID) == null) {
				continue;
			}
			String val = document.getElementById(cID).text().trim();
			if (val.isEmpty()) {
				continue;
			}
			values[i] = val;
		}
		
		BookDetails bookDetails = new BookDetails(values[0], values[1], url, values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10]);
		return bookDetails;
	}

}
