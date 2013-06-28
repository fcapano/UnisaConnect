package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.WeatherData.ActualCondition;
import it.fdev.utils.DrawableManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherActualConditionFragment extends Fragment {
	
	private Activity activity;
	private DrawableManager dm;
	
	private ActualCondition condition;
	
	private TextView lastUpdateTimeView;
	private ImageView webcamView;
	private ImageView iconView;
	private TextView tempView;
	private TextView descriptionView;
	private TextView humidityView;
	private TextView windView;
	private TextView windDirView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView;
		if (condition == null) {
			mainView = null;
		} else {
			dm = new DrawableManager();
			mainView = (View) inflater.inflate(R.layout.weather_actual, container, false);
		}
		return mainView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		webcamView = (ImageView) view.findViewById(R.id.webcam_image);
		if (condition != null) {
			dm.fetchDrawableOnThread(condition.getStationWebcamOptimizedUrl(), webcamView);
		}
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public void setCondition(ActualCondition condition) {
		this.condition = condition;
	}
	
	public void setViews(TextView lastUpdateTimeView, ImageView iconView, TextView tempView, TextView descriptionView, TextView humidityView, TextView windView, TextView windDirView) {
		this.lastUpdateTimeView = lastUpdateTimeView;
		this.iconView = iconView;
		this.tempView = tempView;
		this.descriptionView = descriptionView;
		this.humidityView = humidityView;
		this.windView = windView;
		this.windDirView = windDirView;
	}
	
	public void showCondition() {
		if (condition != null && lastUpdateTimeView != null && iconView != null && tempView != null && descriptionView != null && humidityView != null && windView != null) {
//			lastUpdateTimeView.setText(condition.getLastUpdate());
			iconView.setImageDrawable(condition.getIconDrawable(activity));
			tempView.setText(condition.getTemp());
			descriptionView.setText(condition.getDescription());
			humidityView.setText(condition.getHumidity());
			windView.setText(condition.getWindSpeed());
			windDirView.setText(condition.getWindDir());
		}
	}

}
