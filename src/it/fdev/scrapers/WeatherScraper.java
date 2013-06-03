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
//	private final String METEO_XML_FILENAME = "meteo.xml";
//	private final int MIN_UPDATE_MINUTES_INTERVAL = 10;

	public boolean isRunning = false;

	private WeatherFragment callerMeteoFragment;
	private WeatherData meteo;
//	private boolean isCachedData = true;
//	private boolean forceNewDownload = false;

	public static enum loadStates {
		START, ANALYZING, NO_INTERNET, METEO_NOT_AVAILABLE, UNKNOWN_PROBLEM, FINISHED
	};
	
	public WeatherScraper() {
		super();
//		forceNewDownload = force;
	}

	@Override
	protected Integer doInBackground(MainActivity... activities) {
		try {
			publishProgress(loadStates.START);
			MainActivity activity = activities[0];
//			SharedPrefDataManager sPrefs = SharedPrefDataManager.getDataManager(activity);
//			long lastUpdateTime = sPrefs.getWeatherLastUpdateMillis();
//			long minutesPassedLastUpdate = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastUpdateTime);
			Document document;
//			try {
//				//Se è passato poco tempo dall'ultimo update, carico il file salvato l'ultima volta 
//				if (!forceNewDownload && minutesPassedLastUpdate < MIN_UPDATE_MINUTES_INTERVAL) {
//					// Se esiste il file xml dell'ulima volta lo uso
//					File xmlFile = new File(activity.getFilesDir() + File.separator + METEO_XML_FILENAME);
//					Log.d(Utils.TAG, "Riutilizzo il meteo già scaricato");
//					document = Jsoup.parse(xmlFile, null);
//				} else {
//					// E' passato troppo tempo...vado nel catch dove riscarico le previsioni
//					throw new IOException("Force url reload");
//				}
//			} catch (IOException e) {
				// Scarico le info aggiornate
				Log.d(Utils.TAG, "Scarico le previsioni aggiornate!");
				Log.d(Utils.TAG, "1");
				Response response = Jsoup.connect(METEO_URL).timeout(30000).execute();
				Log.d(Utils.TAG, "2");
				document = response.parse();
				Log.d(Utils.TAG, "3");
//				isCachedData = false;
//			}
			
			WeatherData cMeteo = new WeatherData(activity);
			Elements actualConditionsElements =  document.select("actualCondition > actualWeather");
			for(Element actualElement : actualConditionsElements) {
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
			
			Elements dailyForecastsElements =  document.select("forecast > dailyForecast");
			for(Element dailyElement : dailyForecastsElements) {
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
