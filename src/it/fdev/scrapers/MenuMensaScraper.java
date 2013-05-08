package it.fdev.scrapers;

import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.MensaFragment;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

/**
 * Frammento che si occupa dell'accesso alla esse3
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
		activity = activities[0];
		publishProgress(loadStates.START);
		try {
			Response response = Jsoup.connect(MENSA_URL).timeout(10000).execute();
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
		} catch (IOException e) {
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
			if (callerMenuFragment != null) {
				callerMenuFragment.mostraMenu(null);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
		case NO_INTERNET:
			if (callerMenuFragment != null) {
				callerMenuFragment.mostraMenu(null);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
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
		for (Element cCourse : courses.getElementsByTag("course")) {
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

	public class MenuMensa {
		private String pdfUrl;
		private ArrayList<PiattoMensa> firstCourses;
		private ArrayList<PiattoMensa> secondCourses;
		private ArrayList<PiattoMensa> sideCourses;
		private ArrayList<PiattoMensa> fruitCourse;
		private ArrayList<PiattoMensa> takeAwayBasketCourses;
		private ArrayList<PiattoMensa> otherCourses;

		public MenuMensa(String pdfUrl, ArrayList<PiattoMensa> firstCourses, ArrayList<PiattoMensa> secondCourses, ArrayList<PiattoMensa> sideCourses, ArrayList<PiattoMensa> fruitCourse, ArrayList<PiattoMensa> takeAwayBasketCourses) {
			this.pdfUrl = pdfUrl;
			this.firstCourses = firstCourses;
			this.secondCourses = secondCourses;
			this.sideCourses = sideCourses;
			this.fruitCourse = fruitCourse;
			this.takeAwayBasketCourses = takeAwayBasketCourses;

			if (fruitCourse != null && fruitCourse.size() > 0 && takeAwayBasketCourses != null && takeAwayBasketCourses.size() > 0) {
				otherCourses = new ArrayList<MenuMensaScraper.PiattoMensa>();
				otherCourses.addAll(fruitCourse);
				otherCourses.addAll(takeAwayBasketCourses);
			}

		}

		public String getPdfUrl() {
			return pdfUrl;
		}

		public ArrayList<PiattoMensa> getFirstCourses() {
			return firstCourses;
		}

		public ArrayList<PiattoMensa> getSecondCourses() {
			return secondCourses;
		}

		public ArrayList<PiattoMensa> getSideCourses() {
			return sideCourses;
		}

		public ArrayList<PiattoMensa> getFruitCourses() {
			return fruitCourse;
		}

		public ArrayList<PiattoMensa> getTakeAwayBasketCourses() {
			return takeAwayBasketCourses;
		}
		
		public ArrayList<PiattoMensa> getOtherCourses() {
			return otherCourses;
		}
	}

	public class PiattoMensa {
		private String nomePiatto;
		private String ingredientiIt = null;
		private String ingradientiEn = null;

		public PiattoMensa(String nomePiatto, String ingredientiIt, String ingradientiEn) {
			this.nomePiatto = nomePiatto;
			this.ingredientiIt = ingredientiIt;
			this.ingradientiEn = ingradientiEn;
		}

		public PiattoMensa(String nomePiatto, String ingredientiIt) {
			this(nomePiatto, ingredientiIt, null);
		}

		public PiattoMensa(String nomePiatto) {
			this(nomePiatto, null, null);
		}

		public String getNomePiatto() {
			return nomePiatto;
		}

		public String getIngredientiIt() {
			return ingredientiIt;
		}

		public String getIngradientiEn() {
			return ingradientiEn;
		}
	}
}
