package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentNews;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.utils.CardsAdapter;
import it.fdev.utils.CardsAdapter.CardItem;
import it.fdev.utils.Utils;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Frammento che si occupa dell'accesso al menu della mensa
 * 
 * @author francesco
 * 
 */
public class NewsScraper extends AsyncTask<MainActivity, NewsScraper.loadStates, Integer> {

	public boolean isRunning = false;

	protected MainActivity activity;
	private FragmentNews callerFragment;
	private String urlToFetch = null;
	private ArrayList<CardsAdapter.CardItem> itemsList = null;

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, NO_URL_DEFINED, UNKNOWN_PROBLEM, FINISHED
	};
	
	public NewsScraper(String url) {
		this.urlToFetch = url;
	}

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		if(urlToFetch == null) {
			publishProgress(loadStates.NO_URL_DEFINED);
			return -1;
		}
		
		try {
			activity = activities[0];
			publishProgress(loadStates.START);
            URL url = new URL(urlToFetch);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());

            NodeList nodes = doc.getElementsByTagName("item");
            
            itemsList = new ArrayList<CardsAdapter.CardItem>();
			Log.d(Utils.TAG, "there are cards #" + nodes.getLength());
            
            for (int i = 0; i < nodes.getLength(); i++) {
            	try {
	                Element element = (Element) nodes.item(i);
	                String title = element.getElementsByTagName("title").item(0).getTextContent().trim();
	                String link = element.getElementsByTagName("link").item(0).getTextContent().trim();
	                String description = element.getElementsByTagName("description").item(0).getTextContent().trim();
	                
	                String formattedDate;
	                try {
	                	String dateString = element.getElementsByTagName("published").item(0).getTextContent().trim();
	                	// http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
		                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ITALY);
		                SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM", Locale.ITALY);
		                Date date = inputDateFormat.parse(dateString);
		                formattedDate = outputDateFormat.format(date);
	                } catch(Exception e) {
	                	formattedDate = "";
	                }
	                
            		CardItem cCard = new CardItem(title, link, description, formattedDate, false);
	                itemsList.add(cCard);
            	} catch (Exception e) {
            		Log.w(Utils.TAG, e);
                    continue;
                }
            }
            publishProgress(loadStates.FINISHED);
        }
        catch (Exception e) {
        	itemsList = null;
			Log.e(Utils.TAG, "Problem parsing the xml feed", e);
			publishProgress(loadStates.UNKNOWN_PROBLEM);
			e.printStackTrace();
			return -1;
        }
		return 0;
	}

	@Override
	protected void onProgressUpdate(NewsScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case START:
			activity.setLoadingText(R.string.sync_news);
			break;
		case NO_URL_DEFINED:
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerFragment != null) {
				callerFragment.showCards(new ArrayList<CardsAdapter.CardItem>());
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
		case FINISHED:
			if (callerFragment != null) {
				callerFragment.showCards(itemsList);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
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

	public void setCallerFragment(FragmentNews callerFragment) {
		this.callerFragment = callerFragment;
	}

}
