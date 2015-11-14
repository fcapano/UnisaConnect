package it.fdev.scraper;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import it.fdev.unisaconnect.FragmentNews;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.utils.CardsAdapter;
import it.fdev.utils.CardsAdapter.CardItem;
import it.fdev.utils.Utils;

/**
 * Frammento che si occupa dell'accesso al menu della mensa
 * 
 * @author francesco
 * 
 */
public class RssScraper extends AsyncTask<MainActivity, RssScraper.loadStates, Integer> {

	public boolean isRunning = false;

	private Fragment callerFragment;
	private String url = null;
	private ArrayList<CardsAdapter.CardItem> itemsList = null;
	private int maxItemsToFetch = Integer.MAX_VALUE;
	private int maxTextLength = Integer.MAX_VALUE;

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, NO_URL_DEFINED, UNKNOWN_PROBLEM, FINISHED
	};
	
	public RssScraper(String url) {
		this.url = url;
	}

	// Rss 2.0 format: http://cyber.law.harvard.edu/rss/rss.html
	@Override
	protected Integer doInBackground(MainActivity... activities) {
		if(url == null) {
			publishProgress(loadStates.NO_URL_DEFINED);
			return -1;
		}
		
		try {
			publishProgress(loadStates.START);
			Response response = Jsoup.connect(url).ignoreContentType(true).timeout(30000).execute();
			Document document = response.parse();
			Elements items = document.getElementsByTag("item");
			
			itemsList = new ArrayList<CardsAdapter.CardItem>();
			Log.d(Utils.TAG, "there are cards #" + items.size());
			for(Element cItem : items) {
				if (itemsList.size() >= maxItemsToFetch) {
					break;
				}
				try {
					String title = cItem.getElementsByTag("title").first().text().trim();
					String link = cItem.getElementsByTag("link").first().toString().trim(); //Link not working. Why?
					String description = cItem.getElementsByTag("description").first().text().trim();
					String contentHtml = cItem.getElementsByTag("content:encoded").first().text().trim();
//					String content = Jsoup.parse(contentHtml).text();
					String content = Html.fromHtml(contentHtml).toString().trim();
					String pubDate = cItem.getElementsByTag("pubDate").first().text();
					
					String cdataStart = "^\\Q<![CDATA[\\E"; //"^" + Pattern.quote("<![CDATA[");
					String cdataEnd = "\\Q]]>\\E$"; 		//Pattern.quote("]]>") + "$";
					title = title.replaceAll(cdataStart, "").replaceAll(cdataEnd, "").trim();
					description = description.replaceAll(cdataStart, "").replaceAll(cdataEnd, "").trim();
					content = content.replaceAll(cdataStart, "").replaceAll(cdataEnd, "").trim();
					
					if (description.length() > content.length()) {
						content = description;
					}
					
					if (content.length() > maxTextLength) {
						content = content.substring(0, maxTextLength).trim();
					}
					
					DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
					Date date = formatter.parse(pubDate);
					String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date);
					
					CardItem cCard = new CardItem(title, link, content, dateString);
					itemsList.add(cCard);
				} catch (Exception e) {
					continue;
				}
			}
			publishProgress(loadStates.FINISHED);
		} catch (Exception e) {
			itemsList = null;
			Log.e(Utils.TAG, "Problem parsing RSS", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(RssScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			Log.d(Utils.TAG, "Problem");
			if (callerFragment != null) {
//				callerFragment.mostraMenu(null);
			}
			Utils.dismissAlert();
			break;
		case FINISHED:
			Log.d(Utils.TAG, "Finished");
			if (callerFragment != null) {
				((FragmentNews) callerFragment).showCards(itemsList);
			}
			Utils.dismissAlert();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		isRunning = true;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		isRunning = false;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		isRunning = false;
	}

	public void setCallerFragment(Fragment callerFragment) {
		this.callerFragment = callerFragment;
	}

	public void setMaxItems(int maxItemsToFetch) {
		this.maxItemsToFetch = maxItemsToFetch;
	}
	
	public void setMaxTextLength(int maxTextLength) {
		this.maxTextLength = maxTextLength;
	}
}
