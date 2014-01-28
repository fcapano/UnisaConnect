package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentBiblioPrepareSearch;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.Book;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BiblioSearchScraperService extends IntentService {
	
	public final static String BROADCAST_STATE_BIBLIO_SEARCH = "it.fdev.biblio.status_search";
	
	public static boolean isRunning = false;
	
	private Context mContext;

	public BiblioSearchScraperService() {
		super("it.fdev.biblio.search_scraper_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String url = intent.getStringExtra("URL");
			if(url == null) {
				stopForeground(true);
				stopSelf();
				return;
			}
			isRunning = true;
			mContext = getApplicationContext();

			CookieManager cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
			
			setBiblioSettings();
			ArrayList<Book> results = getSearchResults(url);
			broadcastStatus(mContext, BROADCAST_STATE_BIBLIO_SEARCH, results);
			
			
//				broadcastStatus(mContext, MainActivity.BROADCAST_ERROR, loginStatus);
//				isRunning = false;
//				stopForeground(true);
//				stopSelf();
//				return;
			
			// 2. Recupero i dati
			sendLoadingMessage(mContext, R.string.sincronizzazione_esse3);
			
		} catch (Exception e) {
			Log.e(Utils.TAG, "Esse3 service crashed", e);
		}
		isRunning = false;
		stopForeground(true);
		stopSelf();
		return;
	}
	
	private ArrayList<Book> getSearchResults(String url) throws IOException {
		ArrayList<Book> resultList = new ArrayList<Book>();
		Response res = Jsoup.connect(url).method(Method.GET).timeout(30000).execute();
		Document document = res.parse();
		Element table = document.getElementsByTag("table").last();
		Elements rows = table.getAllElements().first().getElementsByTag("tr");
		for (int i=1; i<rows.size(); i++) {
			Element cRow = rows.get(i);
			Elements cols = cRow.getElementsByTag("td");
			Element anchorDetailsUrl = cols.get(0).getAllElements().first();
			String resultNumber = anchorDetailsUrl.text();
			String detailsUrl = anchorDetailsUrl.attr("href");
			String author = cols.get(2).text();
			String format = cols.get(3).text();
			String title = cols.get(4).text();
			String year = cols.get(5).text();
			Element anchorPositionUrl = cols.get(6).getAllElements().first();
			String positionUrl = anchorPositionUrl.attr("href");
			Book cBook = new Book(resultNumber, author, format, title, year, detailsUrl, positionUrl);
			resultList.add(cBook);
		}
		return resultList;
	}
	
	private void setBiblioSettings() throws IOException {
		String url = FragmentBiblioPrepareSearch.BIBLIO_SET_SETTINGS_URL;
		Jsoup.connect(url).method(Method.GET).timeout(30000).execute();
	}

	public static void broadcastStatus(Context ctx, String action, ArrayList<Book> list) {
		Intent localIntent = new Intent(action);
		localIntent.putExtra("status", list);
		ctx.sendBroadcast(localIntent);
	}
	
	public static void sendLoadingMessage(Context ctx, int messageRes) {
		Intent localIntent = new Intent(MainActivity.BROADCAST_LOADING_MESSAGE);
		localIntent.putExtra("message_res", messageRes);
		ctx.sendBroadcast(localIntent);
	}

}
