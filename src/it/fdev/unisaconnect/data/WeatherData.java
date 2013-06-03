package it.fdev.unisaconnect.data;

import it.fdev.unisaconnect.R;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class WeatherData implements Serializable {
	private static final long serialVersionUID = -5469551486116200662L;

	private final String OPTIMIZED_WEBCAM_IMG_URL = "http://unisameteo.appspot.com/serve?id=";
	private final String YOW_PATH = "Yow/";
	private static Map<String, String> yowIconsAssociations = new HashMap<String, String>();
	static {
		yowIconsAssociations.put("chanceflurries", "11.png");
		yowIconsAssociations.put("chancerain", "10.png");
		yowIconsAssociations.put("chancesleet", "9.png");
		yowIconsAssociations.put("chancesnow", "11.png");
		yowIconsAssociations.put("chancetstorms", "20.png");
		yowIconsAssociations.put("clear", "4.png");
		yowIconsAssociations.put("cloudy", "1.png");
		yowIconsAssociations.put("flurries", "11.png");
		yowIconsAssociations.put("fog", "2.png");
		yowIconsAssociations.put("hazy", "2.png");
		yowIconsAssociations.put("mostlycloudy", "22.png");
		yowIconsAssociations.put("mostlysunny", "22.png");
		yowIconsAssociations.put("partlycloudy", "22.png");
		yowIconsAssociations.put("partlysunny", "4.png");
		yowIconsAssociations.put("sleet", "9.png");
		yowIconsAssociations.put("rain", "10.png");
		yowIconsAssociations.put("snow", "11.png");
		yowIconsAssociations.put("sunny", "4.png");
		yowIconsAssociations.put("tstorms", "20.png");
		yowIconsAssociations.put("cloudy", "1.png");
		yowIconsAssociations.put("partlycloudy", "22.png");

		yowIconsAssociations.put("nt_chanceflurries", "11.png");
		yowIconsAssociations.put("nt_chancerain", "10.png");
		yowIconsAssociations.put("nt_chancesleet", "9.png");
		yowIconsAssociations.put("nt_chancesnow", "11.png");
		yowIconsAssociations.put("nt_chancetstorms", "20.png");
		yowIconsAssociations.put("nt_clear", "3.png");
		yowIconsAssociations.put("nt_cloudy", "1.png");
		yowIconsAssociations.put("nt_flurries", "11.png");
		yowIconsAssociations.put("nt_fog", "2.png");
		yowIconsAssociations.put("nt_hazy", "2.png");
		yowIconsAssociations.put("nt_mostlycloudy", "2.png");
		yowIconsAssociations.put("nt_mostlysunny", "3.png");
		yowIconsAssociations.put("nt_partlycloudy", "2.png");
		yowIconsAssociations.put("nt_partlysunny", "2.png");
		yowIconsAssociations.put("nt_sleet", "9.png");
		yowIconsAssociations.put("nt_rain", "10.png");
		yowIconsAssociations.put("nt_sleet", "9.png");
		yowIconsAssociations.put("nt_snow", "11.png");
		yowIconsAssociations.put("nt_sunny", "3.png");
		yowIconsAssociations.put("nt_tstorms", "20.png");
		yowIconsAssociations.put("nt_cloudy", "1.png");
		yowIconsAssociations.put("nt_partlycloudy", "2.png");
	}
	
	private Context context;
	private Date fetchTime;
	private ArrayList<ActualCondition> actualConditionList;
	private ArrayList<DailyForecast> dailyForecastList;

	public WeatherData(Context context) {
		this.context = context;
		this.fetchTime = new Date();
		this.actualConditionList = new ArrayList<ActualCondition>();
		this.dailyForecastList = new ArrayList<DailyForecast>();
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Date fetchTime) {
		this.fetchTime = fetchTime;
	}

	public void addActualCondition(ActualCondition condition) {
		actualConditionList.add(condition);
	}

	public void addDailyForecast(DailyForecast forecast) {
		dailyForecastList.add(forecast);
	}

	public ArrayList<ActualCondition> getActualConditionList() {
		return actualConditionList;
	}

	public ActualCondition getActualCondition(int i) {
		if (i < 0 || i >= actualConditionList.size()) {
			return null;
		}
		return actualConditionList.get(i);
	}

	public ArrayList<DailyForecast> getDailyForecastList() {
		return dailyForecastList;
	}

	public DailyForecast getDailyForecast(int i) {
		if (i < 0 || i >= dailyForecastList.size()) {
			return null;
		}
		return dailyForecastList.get(i);
	}

	public class ActualCondition implements Serializable {
		private static final long serialVersionUID = 8492846115841636039L;
		private String lastUpdate;
		private String lastUpdateMilliseconds;
		private String description;
		private String iconUrl;
		private Drawable iconDrawable;
		private String temp;
		private String maxTemp;
		private String minTemp;
		private String humidity;
		private String rainToday;
		private String pressure;
		private String pressureTrend;
		private String windDir;
		private String windSpeed;
		private String stationID;
		private String stationName;
		private String stationWebcamUrl;
		private String stationWebcamOptimizedUrl;

		public ActualCondition(String lastUpdate, String lastUpdateMilliseconds, String description, String iconUrl, String temp, 
				String maxTemp, String minTemp, String humidity, String rainToday, String pressure, String pressureTrend, 
				String windDir, String windSpeed, String stationID, String stationName, String stationWebcamUrl) {
			this.lastUpdate = lastUpdate;
			this.lastUpdateMilliseconds = lastUpdateMilliseconds;
			this.description = description;
			this.iconUrl = iconUrl;
			this.iconDrawable = WeatherData.this.getIconDrawable(iconUrl);
			this.temp = temp.replace("C", "").trim();
			this.maxTemp = maxTemp;
			this.minTemp = minTemp;
			this.humidity = humidity;
			this.rainToday = rainToday;
			this.pressure = pressure;
			this.pressureTrend = pressureTrend;
			this.windDir = windDir;
			this.windSpeed = windSpeed;
			this.stationID = stationID;
			this.stationName = stationName;
			this.stationWebcamUrl = stationWebcamUrl;
			try {
				this.stationWebcamOptimizedUrl = OPTIMIZED_WEBCAM_IMG_URL + URLEncoder.encode(stationID, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				this.stationWebcamOptimizedUrl = null;
			} 
		}

		public String getLastUpdate() {
			return lastUpdate;
		}

		public String getLastUpdateMilliseconds() {
			return lastUpdateMilliseconds;
		}
		
		public String getDescription() {
			return description;
		}

		public Drawable getIconDrawable() {
//			return WeatherData.this.getIconDrawable(iconUrl);
			return iconDrawable;
		}

		public String getTemp() {
			return temp;
		}

		public String getMaxTemp() {
			return maxTemp;
		}

		public String getMinTemp() {
			return minTemp;
		}

		public String getHumidity() {
			return humidity;
		}

		public String getRainToday() {
			return rainToday;
		}

		public String getPressure() {
			return pressure;
		}
		
		public String getPressureTrend() {
			return pressureTrend;
		}

		public String getWindDir() {
			return windDir;
		}

		public String getWindSpeed() {
			return windSpeed;
		}

		public String getStationID() {
			return stationID;
		}

		public String getStationName() {
			return stationName;
		}

		public String getStationWebcamUrl() {
			return stationWebcamUrl;
		}
		
		public String getStationWebcamOptimizedUrl() {
			return stationWebcamOptimizedUrl;
		}
	}

	public class DailyForecast implements Serializable {
		private static final long serialVersionUID = -5934884410005634828L;
		private String validThrough;
		private String lastUpdateMilliseconds;
		private String description;
		private String iconUrl;
		private Drawable iconDrawable;
		private String maxTemp;
		private String minTemp;
		private String avgHumidity;
		private String avgWindDir;
		private String avgWindSpeed;
		private String probOfPrec;

		public DailyForecast(String validThrough, String lastUpdateMilliseconds, String description, String iconUrl, String maxTemp, 
				String minTemp, String avgHumidity, String avgWindDir, String avgWindSpeed, String probOfPrec) {
			String shortDay = validThrough.trim().substring(0, 3).toUpperCase(Locale.ITALY);
			this.validThrough = shortDay;
			this.lastUpdateMilliseconds = lastUpdateMilliseconds;
			this.description = description;
			this.iconUrl = iconUrl;
			this.iconDrawable = WeatherData.this.getIconDrawable(iconUrl);
			this.maxTemp = maxTemp;
			this.minTemp = minTemp;
			this.avgHumidity = avgHumidity;
			this.avgWindDir = avgWindDir;
			this.avgWindSpeed = avgWindSpeed;
			this.probOfPrec = probOfPrec;
		}

		public String getValidThrough() {
			return validThrough;
		}

		public String getLastUpdateMilliseconds() {
			return lastUpdateMilliseconds;
		}

		public String getDescription() {
			return description;
		}

		public Drawable getIconDrawable() {
//			return WeatherData.this.getIconDrawable(iconUrl);
			return iconDrawable;
		}

		public String getMaxTemp() {
			return maxTemp;
		}

		public String getMinTemp() {
			return minTemp;
		}

		public String getAvgHumidity() {
			return avgHumidity;
		}

		public String getAvgWindDir() {
			return avgWindDir;
		}

		public String getAvgWindSpeed() {
			return avgWindSpeed;
		}

		public String getProbOfPrec() {
			return probOfPrec;
		}
	}
	
	private static String filenameFromUrl(String iconUrl) {
		String file = iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
		String name = file.substring(0, file.lastIndexOf("."));
		return name;
	}
	
	private Drawable getIconDrawable(String iconUrl) {
		try {
			String imageFileName = filenameFromUrl(iconUrl);
			String yowImageFileName = yowIconsAssociations.get(imageFileName);
			return Drawable.createFromStream(context.getAssets().open(YOW_PATH + yowImageFileName), null);
		} catch (Exception e) {
			return context.getResources().getDrawable(R.drawable.transparent);
		}
	}
}