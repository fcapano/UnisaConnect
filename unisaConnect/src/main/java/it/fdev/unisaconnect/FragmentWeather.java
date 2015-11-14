package it.fdev.unisaconnect;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.fdev.scraper.WeatherScraper;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.unisaconnect.data.WeatherData;
import it.fdev.unisaconnect.data.WeatherData.DailyForecast;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

public class FragmentWeather extends MySimpleFragment {
	
	private final int MIN_UPDATE_MINUTES_INTERVAL = 10;

	private boolean alreadyStarted = false;
	private WeatherScraper meteoScraper;
	private WeatherData meteo;
	private SharedPrefDataManager mDataManager;

	private RelativeLayout weatherActualContainerView;
//	private RelativeLayout weatherForecastContainerView;
	private GridView weatherForecastGridview;
	private TextView meteoNDView;
//	private TextView forecastNDView;
	private ViewPager pager;

	private TextView lastUpdateView;
	private ImageView lastUpdateIconView;
	private ImageView iconView;
	private TextView tempView;
	private TextView descriptionView;
	private TextView humidityView;
	private TextView windView;
	private TextView windDirView;
	
	private CurrentWeatherAdapter currentWeatherAdapter;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.fragment_weather, container, false);
		
		mActivity.setLoadingVisible(true, true);
		
		meteoNDView = (TextView) view.findViewById(R.id.meteo_non_disponibile);
//		forecastNDView = (TextView) view.findViewById(R.id.forecast_not_available);
		pager = (ViewPager) view.findViewById(R.id.meteo_pager);
		lastUpdateView = (TextView) view.findViewById(R.id.last_update_time);
		lastUpdateIconView = (ImageView) view.findViewById(R.id.last_update_icon);
		iconView = (ImageView) view.findViewById(R.id.weather_icon);
		tempView = (TextView) view.findViewById(R.id.weather_temp);
		descriptionView = (TextView) view.findViewById(R.id.weather_description);
		humidityView = (TextView) view.findViewById(R.id.weather_humidity);
		windView = (TextView) view.findViewById(R.id.weather_wind);
		windDirView = (TextView) view.findViewById(R.id.weather_wind_dir);
		weatherActualContainerView = (RelativeLayout) view.findViewById(R.id.weather_actual_container);
//		weatherForecastContainerView = (RelativeLayout) view.findViewById(R.id.weather_forecast_container);
		weatherForecastGridview = (GridView) view.findViewById(R.id.weather_forecast_gridview);
		
		mDataManager = new SharedPrefDataManager(mActivity);
		meteo = mDataManager.getWeather();
		if (meteo != null) {
			Log.d(Utils.TAG, "Meteo salvato!");
		} else {
			Log.d(Utils.TAG, "Meteo non salvato!");
		}
		getWeather(false);
		
		return view;
	}

	@Override
	public Set<Integer> getActionsToShow() {
		Set<Integer> actionsToShow = new HashSet<Integer>();
		actionsToShow.add(R.id.action_refresh_button);
		if (!alreadyStarted) {
			actionsToShow.add(R.id.action_loading_animation);
		}
		return actionsToShow;
	}

	@Override
	public void actionRefresh() {
		getWeather(true);
	}

	public void getWeather(boolean force) {
		if (!isAdded()) {
			return;
		}
		
		if (meteo != null) {
			// ho un meteo salvato
			if(!alreadyStarted)	// Se non è gia stato mostrato lo visualizzo
				alreadyStarted = true;
				showWeather(null);
			if (!force) {
				// Se non devo forzare l'aggiornamento del meteo vedo se è recente e non devo aggiornarlo
				long lastUpdateTime = meteo.getFetchTime().getTime();
				long minutesPassedLastUpdate = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastUpdateTime);
				if (minutesPassedLastUpdate <= MIN_UPDATE_MINUTES_INTERVAL) {
					alreadyStarted = true;
					mActivity.setLoadingVisible(false, false);
					return;
				}
			}
			// devo aggiornarlo
			mActivity.setLoadingVisible(true, false);
		} else {
			// Nessun meteo scaricato memorizzato
			mActivity.setLoadingVisible(true, true);
		}
		
		if (!Utils.hasConnection(mActivity)) {
			if (meteo == null) {
				Utils.goToInternetError(mActivity, this);
			} else {
				mActivity.setLoadingVisible(false, false);
			}
			alreadyStarted = true;
			return;
		}
		alreadyStarted = true;
		if (meteoScraper != null && meteoScraper.isRunning) {
			return;
		}
		meteoScraper = new WeatherScraper();
		meteoScraper.setCallerMeteoFragment(this);
		meteoScraper.execute(mActivity);
		return;
	}

	public void showWeather(WeatherData newWeatherData) {
		if (!isAdded()) {
			return;			
		}
		if (newWeatherData == null && this.meteo == null) {
			weatherActualContainerView.setVisibility(View.GONE);
//			weatherForecastContainerView.setVisibility(View.GONE);
			meteoNDView.setVisibility(View.VISIBLE);
			mActivity.setLoadingVisible(false, false);
			return;
		}
		weatherActualContainerView.setVisibility(View.VISIBLE);
//		weatherForecastContainerView.setVisibility(View.VISIBLE);
		meteoNDView.setVisibility(View.GONE);
		
		if (newWeatherData != null) {
			this.meteo = newWeatherData;
		}
		
		currentWeatherAdapter = new CurrentWeatherAdapter(mActivity.getSupportFragmentManager());
		pager.setAdapter(currentWeatherAdapter);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				currentWeatherAdapter.getItem(arg0).showCondition();
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		pager.setCurrentItem(0); // Rettorato -> La webcam della stecca 9 non viene aggiornata da un mese! 
		
		ArrayList<DailyForecast> forecastList = this.meteo.getDailyForecastList();
		DailyForecast lastForecast = forecastList.get(forecastList.size()-1);
		
		boolean isForecastPast = Utils.isBefore(lastForecast.getValidThroughDate());
		if (isForecastPast) {
			// Le previsioni sono vecchie...si sarà inceppato lo scraper?
			weatherForecastGridview.setVisibility(View.GONE);
//			forecastNDView.setVisibility(View.VISIBLE);
		} else {
//			forecastNDView.setVisibility(View.GONE);
			weatherForecastGridview.setVisibility(View.VISIBLE);
			weatherForecastGridview.setAdapter(new WeatherForecastAdapter(mActivity, this.meteo.getDailyForecastList()));
		}
		
		if (meteo.getFetchTime().getTime() > 0) {
		    String dateFirstPart = new SimpleDateFormat("dd/MM", Locale.ITALY).format(meteo.getFetchTime());
		    String dateSecondPart = new SimpleDateFormat("HH:mm", Locale.ITALY).format(meteo.getFetchTime());
		    String updateText = getString(R.string.aggiornato_il_alle, dateFirstPart, dateSecondPart);
			lastUpdateView.setText(updateText);
			lastUpdateView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		}
		
		if (newWeatherData != null) {
			// Il metodo è stato chiamato con il meteo aggiornato da salvare
			mActivity.setLoadingVisible(false, false);
			Log.d(Utils.TAG, "saving weather data");
			mDataManager.setWeather(newWeatherData);
		}
		
		FragmentWeatherActualCondition cItem = currentWeatherAdapter.getItem(1);
		if (cItem != null) {	// E' capitato
			cItem.showCondition();
		}
		
	}
	
	class CurrentWeatherAdapter extends FragmentPagerAdapter {
		private FragmentWeatherActualCondition[] fragmentsList;

		public CurrentWeatherAdapter(FragmentManager fm) {
			super(fm);
			fragmentsList = new FragmentWeatherActualCondition[getCount()];
		}

		@Override
		public FragmentWeatherActualCondition getItem(int position) {
			if (position<0 || position>=fragmentsList.length) {
				return null;
			}
			FragmentWeatherActualCondition cFragment;
			if (fragmentsList[position] == null) {
				cFragment = new FragmentWeatherActualCondition();
				cFragment.setActivity(mActivity);
				cFragment.setCondition(meteo.getActualCondition(position));
				cFragment.setViews(lastUpdateView, lastUpdateIconView, iconView, tempView, descriptionView, humidityView, windView, windDirView);
				fragmentsList[position] = cFragment;
			} else {
				cFragment = fragmentsList[position];
			}
			return cFragment;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return meteo.getActualCondition(position).getStationName().toUpperCase(Locale.ITALY);
		}

		@Override
		public int getCount() {
			return meteo.getActualConditionList().size();
		}
		
//		@Override
//		public Parcelable saveState() {
//			// Workaround for forcing fragments to be recreated after activity is resumed
//			return null;
//		}
		
	}
	
	@Override
	public int getTitleResId() {
		return R.string.meteo;
	}

}
