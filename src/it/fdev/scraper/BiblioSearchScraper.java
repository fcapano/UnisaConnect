package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentBiblioDoSearch;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.Book;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class BiblioSearchScraper extends IntentService {

	public final static String BROADCAST_STATE_BIBLIO_SEARCH = "it.fdev.biblio.status_search";

	public static boolean isRunning = false;

	private Context mContext;
	private static CookieManager mCookieManager = new CookieManager();
	private static String reqParam;

	public BiblioSearchScraper() {
		super("it.fdev.biblio.search_scraper_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String url = intent.getStringExtra(FragmentBiblioDoSearch.ARG_URI);
			if (url == null) {
				isRunning = false;
				stopForeground(true);
				stopSelf();
				return;
			}

			isRunning = true;
			mContext = getApplicationContext();

			Utils.sendLoadingMessage(mContext, R.string.cerco_libri);

			ArrayList<Book> results = getSearchResults(url);
			Utils.broadcastStatus(mContext, BROADCAST_STATE_BIBLIO_SEARCH, FragmentBiblioDoSearch.BROADCAST_STATUS, results);
		} catch (Exception e) {
			Log.e(Utils.TAG, "Biblio search service crashed", e);
			Utils.broadcastStatus(mContext, BROADCAST_STATE_BIBLIO_SEARCH, FragmentBiblioDoSearch.BROADCAST_STATUS, null);
		}
		isRunning = false;
		stopForeground(true);
		stopSelf();
		return;
	}

	public static String prepareCookies(String url) throws IOException {
		CookieHandler.setDefault(mCookieManager);
		Response res = Jsoup.connect(url).method(Method.GET).timeout(30000).execute();
		Document document = res.parse();

		String toSearch = Pattern.quote("lio-aleph.unisa.it/F/") + "([A-Z0-9]+" + Pattern.quote("-") + "[0-9]+)" + Pattern.quote("?func=fin");
		Pattern pattern = Pattern.compile(toSearch);
		Matcher matcher = pattern.matcher(document.toString());
		if (!matcher.find() || matcher.groupCount() < 1) {
			return null;
		}
		return matcher.group(1);
	}

	private ArrayList<Book> getSearchResults(String url) throws IOException {
		String urlToGet = url;
		if (reqParam == null || reqParam.isEmpty()) {
			reqParam = prepareCookies(url);
		}
		if (reqParam == null) {
			return null;
		}
		urlToGet = url.replace(".unisa.it/F/", ".unisa.it/F/" + reqParam);

		Log.d(Utils.TAG, "urlToGet: " + urlToGet);

		Response res = Jsoup.connect(urlToGet).method(Method.GET).timeout(30000).execute();
		Document document = res.parse();
		
		boolean isValidResults = document.getElementsMatchingOwnText("Numero di record nel set superato").isEmpty();
		if (!isValidResults) {
			return null;
		}

		ArrayList<Book> resultList = new ArrayList<Book>();
		Element table = document.getElementsByTag("table").last();
		Elements rows = table.getAllElements().first().getElementsByTag("tr");
		for (int i = 1; i < rows.size(); i++) {
			Element cRow = rows.get(i);
			Elements cols = cRow.getElementsByTag("td");
			if (cols.size() < 7) {
				return null;
			}
			String resultNumber = cols.get(0).text().trim();
			String detailsUrl = cols.get(0).getElementsByTag("a").first().attr("href").trim();
			String author = cols.get(2).text().trim();
			String format = cols.get(3).text().trim();
			String title = cols.get(4).text().trim();
			String year = cols.get(5).text().trim();
			String position = cols.get(6).getAllElements().first().text().trim();
			Book cBook = new Book(resultNumber, author, format, title, year, detailsUrl, position);
			resultList.add(cBook);
		}
		return resultList;
	}

}
