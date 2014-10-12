package it.fdev.unisaconnect;

import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.WeatherData.DailyForecast;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherForecastAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<DailyForecast> forecastList;
	private LayoutInflater inflater;

	public WeatherForecastAdapter(Context context, ArrayList<DailyForecast> forecastList) {
		this.context = context;
		this.forecastList = forecastList;
		inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return forecastList.size();
	}

	public Object getItem(int position) {
		if (position < 0 || position > forecastList.size()) {
			return null;
		}
		return forecastList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.weather_forecast_column, parent, false);
		}

		DailyForecast cForecast = forecastList.get(position);

		TextView dayView = (TextView) v.findViewById(R.id.forecast_day);
		ImageView iconView = (ImageView) v.findViewById(R.id.forecast_icon);
		TextView descriptionView = (TextView) v.findViewById(R.id.forecast_description);
		TextView minTempView = (TextView) v.findViewById(R.id.forecast_min);
		TextView maxTempView = (TextView) v.findViewById(R.id.forecast_max);
		TextView precipitationsView = (TextView) v.findViewById(R.id.forecast_precipitations);

		dayView.setText(cForecast.getValidDay());
		iconView.setImageDrawable(cForecast.getIconDrawable(context));
		descriptionView.setText(cForecast.getDescription());
		minTempView.setText(cForecast.getMinTemp());
		maxTempView.setText(cForecast.getMaxTemp());
		precipitationsView.setText(cForecast.getProbOfPrec());

		return v;
	}

}
