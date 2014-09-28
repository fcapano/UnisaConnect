package it.fdev.scraper;

import it.fdev.unisaconnect.FragmentMensa;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.MenuMensa;
import it.fdev.unisaconnect.data.MenuMensa.PiattoMensa;
import it.fdev.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;

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
	private FragmentMensa callerMenuFragment;
	private MenuMensa menu = null;
	private String errorMessage;

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
			Element infoTag = document.getElementsByTag("info").first();
			String status = infoTag.attr("status");
			if (!status.equals("1")) {
				if (infoTag.children().size() == 0 && infoTag.text().length() > 0) {
					errorMessage = infoTag.text();
				} else {
					errorMessage = null;
				}
				publishProgress(loadStates.MENU_NOT_AVAILABLE);
				return -1;
			}
			// publishProgress(loadStates.ANALYZING);
			String menuDate = document.getElementsByTag("date").first().text();
			String menuDateMillis = document.getElementsByTag("date").first().attr("millis");
			String menuUrl = document.getElementsByTag("menuUrl").first().text();
			ArrayList<PiattoMensa> firstCourses = getCourses(document.select("menu > firstCourses").first());
			ArrayList<PiattoMensa> secondCourses = getCourses(document.select("menu > secondCourses").first());
			ArrayList<PiattoMensa> sideCourses = getCourses(document.select("menu > sideCourses").first());
			ArrayList<PiattoMensa> fruitCourse = getCourses(document.select("menu > fruitCourse").first());
			ArrayList<PiattoMensa> takeAwayBasket = getCourses(document.select("menu > takeAwayBasket").first());
			menu = new MenuMensa(menuDate, menuDateMillis, menuUrl, firstCourses, secondCourses, sideCourses, fruitCourse, takeAwayBasket);
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
		MenuMensaScraperAlternativo mensaAlternativoScraper = new MenuMensaScraperAlternativo();
		mensaAlternativoScraper.setCallerMenuMensaFragment(callerMenuFragment);
		super.onProgressUpdate(values);
		switch (values[0]) {
		case START:
			activity.setLoadingText(R.string.sincronizzazione_menu);
			break;
		case MENU_NOT_AVAILABLE:
			if (callerMenuFragment == null) {
				return;
			}
			if (errorMessage != null && errorMessage.toLowerCase(Locale.ITALIAN).contains("sorpresa")) {
				// Nel caso in cui ammensa-unisa non abbia il menu del giorno, verifico se unisamenu invece ne ha uno
				Log.d(Utils.TAG, "ammensa-unisa han no menu...trying with unisamenu");
				mensaAlternativoScraper.setErrorMessage(errorMessage);
				mensaAlternativoScraper.execute(activity);
			} else {
				if (errorMessage != null) {
					callerMenuFragment.mostraErrore(errorMessage);
				} else {
					callerMenuFragment.mostraMenu(null);
				}
			}
			break;
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerMenuFragment == null) {
				return;
			}
			// Nel caso in cui ammensa-unisa non abbia il menu del giorno, verifico se unisamenu invece ne ha uno
			Log.d(Utils.TAG, "ammensa-unisa han no menu...trying with unisamenu");
			mensaAlternativoScraper.execute(activity);
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

	public ArrayList<PiattoMensa> getCourses(Element courses) {
		ArrayList<PiattoMensa> coursesList = new ArrayList<PiattoMensa>();

		// Avoid Iterators: http://stackoverflow.com/questions/10291767/is-there-anything-faster-than-jsoup-for-html-scraping
		Elements list = courses.getElementsByTag("course");
		Element cCourse;
		for (int i = 0; i < list.size(); i++) {
			cCourse = list.get(i);
			String name = cCourse.getElementsByTag("name").first().text().trim();
			String ingredientsIt, ingredientsEn;
			Elements ingredientsTags = cCourse.getElementsByTag("ingredients");
			if (ingredientsTags.size() == 0) {
				coursesList.add(new PiattoMensa(name));
			} else if (ingredientsTags.size() == 1) {
				ingredientsIt = ingredientsTags.first().text().trim();
				coursesList.add(new PiattoMensa(name, ingredientsIt));
			} else {
				ingredientsEn = ingredientsTags.get(0).text().trim();
				ingredientsIt = ingredientsTags.get(1).text().trim();
				coursesList.add(new PiattoMensa(name, ingredientsIt, ingredientsEn));
			}
		}
		return coursesList;
	}
}
