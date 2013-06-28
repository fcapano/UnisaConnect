package it.fdev.scrapers;

import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.WeatherFragment;
import it.fdev.unisaconnect.data.WeatherData;
import it.fdev.unisaconnect.data.WeatherData.ActualCondition;
import it.fdev.unisaconnect.data.WeatherData.DailyForecast;
import it.fdev.utils.Utils;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Frammento che si occupa di reperire le info meteo
 * 
 */
public class WeatherScraper extends AsyncTask<MainActivity, WeatherScraper.loadStates, Integer> {

	public final String METEO_URL = "http://unisameteo.appspot.com/meteo";
	public boolean isRunning = false;

	private WeatherFragment callerMeteoFragment;
	private WeatherData meteo;

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, METEO_NOT_AVAILABLE, UNKNOWN_PROBLEM, FINISHED
	};
	
	public WeatherScraper() {
		super();
	}

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		try {
			publishProgress(loadStates.START);
			MainActivity activity = activities[0];
			Document document;
			// Scarico le info aggiornate
			Log.d(Utils.TAG, "Scarico le previsioni aggiornate!");
			Response response = Jsoup.connect(METEO_URL).timeout(30000).execute();
			document = response.parse();
			
			WeatherData cMeteo = new WeatherData(activity);
			
			// Avoid Iterators: http://stackoverflow.com/questions/10291767/is-there-anything-faster-than-jsoup-for-html-scraping
			Elements actualConditionsElements =  document.select("actualCondition > actualWeather");
			Element actualElement;
			for (int i=0; i<actualConditionsElements.size(); i++) {
				actualElement = actualConditionsElements.get(i);
				String lastUpdate = actualElement.getElementsByTag("lastUpdate").text();
				String lastUpdateMilliseconds = actualElement.getElementsByTag("lastUpdate").attr("millisec");
				String description = actualElement.getElementsByTag("description").text();
				String iconUrl = actualElement.getElementsByTag("iconUrl").text();
				String temp = actualElement.getElementsByTag("temp").text();
				String maxTemp = actualElement.getElementsByTag("maxTemp").text();
				String minTemp = actualElement.getElementsByTag("minTemp").text();
				String humidity = actualElement.getElementsByTag("humidity").text();
				String rainToday = actualElement.getElementsByTag("rainToday").text();
				String pressure = actualElement.getElementsByTag("pressure").text();
				String pressureTrend = actualElement.getElementsByTag("pressureTrend").text();
				String windDir = actualElement.getElementsByTag("windDir").text();
				String windSpeed = actualElement.getElementsByTag("windSpeed").text();
				
				Element station = actualElement.getElementsByTag("station").get(0);
				String stationID = station.getElementsByTag("id").get(0).text();
				String stationName = station.getElementsByTag("name").get(0).text();
				String stationWebcamUrl = station.getElementsByTag("webcamUrl").get(0).text();
				
				ActualCondition cCondition = cMeteo.new ActualCondition(lastUpdate, lastUpdateMilliseconds, description, iconUrl, temp, 
						maxTemp, minTemp, humidity, rainToday, pressure, pressureTrend, windDir, windSpeed, stationID, stationName, stationWebcamUrl);
				cMeteo.addActualCondition(cCondition);
			}
			
			// Avoid Iterators: http://stackoverflow.com/questions/10291767/is-there-anything-faster-than-jsoup-for-html-scraping
			Elements dailyForecastsElements =  document.select("forecast > dailyForecast");
			Element dailyElement;
			for (int i=0; i<dailyForecastsElements.size(); i++) {
				dailyElement = dailyForecastsElements.get(i);
				String validThrough = dailyElement.getElementsByTag("validThrough").text();
				String lastUpdateMilliseconds = dailyElement.getElementsByTag("validThrough").attr("millisec");
				String description = dailyElement.getElementsByTag("description").text();
				String iconUrl = dailyElement.getElementsByTag("iconUrl").text();
				String maxTemp = dailyElement.getElementsByTag("maxTemp").text();
				String minTemp = dailyElement.getElementsByTag("minTemp").text();
				String avgHumidity = dailyElement.getElementsByTag("avgHumidity").text();
				String avgWindDir = dailyElement.getElementsByTag("avgWindDir").text();
				String avgWindSpeed = dailyElement.getElementsByTag("avgWindSpeed").text();
				String probOfPrec = dailyElement.getElementsByTag("probOfPrec").text();
				
				DailyForecast cForecast = cMeteo.new DailyForecast(validThrough, lastUpdateMilliseconds, description, iconUrl, maxTemp, minTemp, 
						avgHumidity, avgWindDir, avgWindSpeed, probOfPrec);
				cMeteo.addDailyForecast(cForecast);
			}
			meteo = cMeteo;
			publishProgress(loadStates.FINISHED);
			
//			// Salvo il file xml in modo da non dover riscaricarlo la prossima volta
//			if (!isCachedData) {
//				FileOutputStream outputStream = activity.openFileOutput(METEO_XML_FILENAME, Context.MODE_PRIVATE);
//				outputStream.write(document.toString().getBytes());
//				outputStream.close();
//				sPrefs.setWeatherLastUpdateMillis(System.currentTimeMillis());
//				sPrefs.saveData();
//			}
		} catch (Exception e) {
			Log.w(Utils.TAG, "Error in scraper meteo", e);
			meteo = null;
			publishProgress(loadStates.UNKNOWN_PROBLEM);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(WeatherScraper.loadStates... values) {
		super.onProgressUpdate(values);
		switch (values[0]) {
		case METEO_NOT_AVAILABLE:
		case NO_INTERNET:
		case UNKNOWN_PROBLEM:
			if (callerMeteoFragment != null) {
				callerMeteoFragment.showWeather(null);
			}
			Utils.dismissAlert();
			Utils.dismissDialog();
			break;
		case FINISHED:
			if (callerMeteoFragment != null) {
				callerMeteoFragment.showWeather(meteo);
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

	public void setCallerMeteoFragment(WeatherFragment callerMenuFragment) {
		this.callerMeteoFragment = callerMenuFragment;
	}

}
