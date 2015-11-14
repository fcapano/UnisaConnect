package it.fdev.scraper;

import android.os.AsyncTask;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import it.fdev.unisaconnect.FragmentMensa;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.MenuMensa;
import it.fdev.unisaconnect.data.MenuMensa.PiattoMensa;

/**
 * Frammento che si occupa dell'accesso al menu della mensa
 * 
 * @author francesco
 * 
 */
public class MenuMensaScraperAlternativo extends AsyncTask<MainActivity, MenuMensaScraperAlternativo.loadStates, Integer> {

	public final String MENSA_URL = "http://www.unisamenu.it/";
	public boolean isRunning = false;

	protected MainActivity activity;
	private FragmentMensa callerMenuFragment;
	private MenuMensa menu = null;
	
	private String errorMessage = null;

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
			
			// publishProgress(loadStates.ANALYZING);
			String menuDate = document.getElementById("date").text();
			String menuUrl = "";
			ArrayList<PiattoMensa> firstCourses = getCourses(document.getElementsByClass("primo").first());
			ArrayList<PiattoMensa> secondCourses = getCourses(document.getElementsByClass("secondo").first());
			ArrayList<PiattoMensa> sideCourses = getCourses(document.getElementsByClass("contorno").first());
			ArrayList<PiattoMensa> fruitCourse = getCourses(document.getElementsByClass("altro").first());
			ArrayList<PiattoMensa> takeAwayBasket = new ArrayList<MenuMensa.PiattoMensa>();
			menu = new MenuMensa(menuDate, null, menuUrl, firstCourses, secondCourses, sideCourses, fruitCourse, takeAwayBasket);
			
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
	protected void onProgressUpdate(MenuMensaScraperAlternativo.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case START:
			activity.setLoadingText(R.string.sincronizzazione_menu);			
			break;
		case MENU_NOT_AVAILABLE:
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerMenuFragment == null) {
				return;
			}
			if (errorMessage != null) {
				callerMenuFragment.mostraErrore(errorMessage);
			} else {
				callerMenuFragment.mostraMenu(null);
			}
			break;
		case FINISHED:
			if (callerMenuFragment == null) {
				return;
			}
			callerMenuFragment.mostraMenu(menu);
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

	public void setCallerMenuMensaFragment(FragmentMensa callerMenuFragment) {
		this.callerMenuFragment = callerMenuFragment;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ArrayList<PiattoMensa> getCourses(Element courses) {
		ArrayList<PiattoMensa> coursesList = new ArrayList<PiattoMensa>();
		
		// Avoid Iterators: http://stackoverflow.com/questions/10291767/is-there-anything-faster-than-jsoup-for-html-scraping
		Elements list = courses.getElementsByClass("course");
		Element cCourse;
		for (int i=0; i<list.size(); i++) {
			cCourse = list.get(i);
			String name = cCourse.getElementsByClass("name").first().text();
			String ingredientsIt, ingredientsEn;
			Elements ingredientsTags = cCourse.getElementsByClass("description");
			if (ingredientsTags.size() == 0) {
				coursesList.add(new PiattoMensa(name));
			} else if (ingredientsTags.size() == 1) {
				ingredientsIt = ingredientsTags.first().text();
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
