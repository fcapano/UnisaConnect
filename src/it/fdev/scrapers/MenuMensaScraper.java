package it.fdev.scrapers;

import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.MensaFragment;
import it.fdev.unisaconnect.data.MenuMensa;
import it.fdev.unisaconnect.data.MenuMensa.PiattoMensa;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

/**
 * Frammento che si occupa dell'accesso al menu della mensa
 * 
 * @author francesco
 * 
 */
public class MenuMensaScraper extends AsyncTask<MainActivity, MenuMensaScraper.loadStates, Integer> {

	public final String MENSA_URL = "http://ammensa-unisa.appspot.com";
	public boolean isRunning = false;

	protected MainActivity activity;
	private MensaFragment callerMenuFragment;
	private MenuMensa menu = null;

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, MENU_NOT_AVAILABLE, UNKNOWN_PROBLEM, FINISHED
	};

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		try {
			activity = activities[0];
			publishProgress(loadStates.START);
			Response response = Jsoup.connect(MENSA_URL).timeout(30000).execute();
			Document document = response.parse();
			String status = document.getElementsByTag("info").get(0).attr("status");
			if (!status.equals("1")) {
				publishProgress(loadStates.MENU_NOT_AVAILABLE);
				return -1;
			}
			// publishProgress(loadStates.ANALYZING);
			String menuUrl = document.getElementsByTag("menuUrl").get(0).text();
			ArrayList<PiattoMensa> firstCourses = getCourses(document.select("menu > firstCourses").get(0));
			ArrayList<PiattoMensa> secondCourses = getCourses(document.select("menu > secondCourses").get(0));
			ArrayList<PiattoMensa> sideCourses = getCourses(document.select("menu > sideCourses").get(0));
			ArrayList<PiattoMensa> fruitCourse = getCourses(document.select("menu > fruitCourse").get(0));
			ArrayList<PiattoMensa> takeAwayBasket = getCourses(document.select("menu > takeAwayBasket").get(0));
			menu = new MenuMensa(menuUrl, firstCourses, secondCourses, sideCourses, fruitCourse, takeAwayBasket);
			publishProgress(loadStates.FINISHED);
		} catch (Exception e) {
			menu = null;
			publishProgress(loadStates.UNKNOWN_PROBLEM);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(MenuMensaScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case MENU_NOT_AVAILABLE:
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerMenuFragment != null) {
				callerMenuFragment.mostraMenu(null);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
		case FINISHED:
			if (callerMenuFragment != null) {
				callerMenuFragment.mostraMenu(menu);
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

	public void setCallerMenuMensaFragment(MensaFragment callerMenuFragment) {
		this.callerMenuFragment = callerMenuFragment;
	}

	public ArrayList<PiattoMensa> getCourses(Element courses) {
		ArrayList<PiattoMensa> coursesList = new ArrayList<PiattoMensa>();
		
		// Avoid Iterators: http://stackoverflow.com/questions/10291767/is-there-anything-faster-than-jsoup-for-html-scraping
		Elements list = courses.getElementsByTag("course");
		Element cCourse;
		for (int i=0; i<list.size(); i++) {
			cCourse = list.get(i);
			String name = cCourse.getElementsByTag("name").get(0).text();
			String ingredientsIt, ingredientsEn;
			Elements ingredientsTags = cCourse.getElementsByTag("ingredients");
			if (ingredientsTags.size() == 0) {
				coursesList.add(new PiattoMensa(name));
			} else if (ingredientsTags.size() == 1) {
				ingredientsIt = ingredientsTags.get(0).text();
				coursesList.add(new PiattoMensa(name, ingredientsIt));
			} else {
				ingredientsEn = ingredientsTags.get(0).text();
				ingredientsIt = ingredientsTags.get(1).text();
				coursesList.add(new PiattoMensa(name, ingredientsIt, ingredientsEn));
			}
		}
		return coursesList;
	}
}
