package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.WeatherData.ActualCondition;
import it.fdev.utils.DrawableManager;
import it.fdev.utils.DrawableManager.DrawableManagerListener;
import it.fdev.utils.MyDateUtils;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentWeatherActualCondition extends Fragment {

	private Activity mActivity;
	private DrawableManager dm;

	private ActualCondition condition;

	private TextView lastUpdateTimeView;
	private ImageView lastUpdateIconView;
	private ImageView webcamView;
	private ImageView iconView;
	private TextView tempView;
	private TextView descriptionView;
	private TextView humidityView;
	private TextView windView;
	private TextView windDirView;

	private Drawable downloadedWebcamImg = null;

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
			if (downloadedWebcamImg == null) {
				// If the image has not been downloaded yet, download and cache
				dm.fetchDrawableOnThread(condition.getStationWebcamOptimizedUrl(), new DrawableManagerListener() {
					@Override
					public void onLoadingComplete(Drawable image) {
						downloadedWebcamImg = image;
						webcamView.setImageDrawable(downloadedWebcamImg);
					}

					@Override
					public void onLoadingError() {
					}
				});
			} else {
				// The image was already downloaded
				webcamView.setImageDrawable(downloadedWebcamImg);
			}
		}
	}

	public void setActivity(Activity activity) {
		this.mActivity = activity;
	}

	public void setCondition(ActualCondition condition) {
		this.condition = condition;
	}

	public void setViews(TextView lastUpdateTimeView, ImageView lastUpdateIconView, ImageView iconView, TextView tempView, TextView descriptionView, TextView humidityView, TextView windView, TextView windDirView) {
		this.lastUpdateTimeView = lastUpdateTimeView;
		this.lastUpdateIconView = lastUpdateIconView;
		this.iconView = iconView;
		this.tempView = tempView;
		this.descriptionView = descriptionView;
		this.humidityView = humidityView;
		this.windView = windView;
		this.windDirView = windDirView;
	}

	public void showCondition() {
		if (condition == null) {
			return;
		}

		long millis = Long.parseLong(condition.getLastUpdateMilliseconds());
		String updateText = MyDateUtils.getLastUpdateString(mActivity, millis, false);

		if (updateText == null || updateText.isEmpty()) {
			lastUpdateTimeView.setVisibility(View.GONE);
			lastUpdateIconView.setVisibility(View.GONE);
		} else {
			lastUpdateTimeView.setVisibility(View.VISIBLE);
			lastUpdateIconView.setVisibility(View.VISIBLE);
			lastUpdateTimeView.setText(updateText);
		}

		Drawable icon = condition.getIconDrawable(mActivity);
		if (icon == null) {
			iconView.setVisibility(View.INVISIBLE);
		} else {
			iconView.setVisibility(View.VISIBLE);
			iconView.setImageDrawable(icon);
		}
		
		if(condition.getTemp() == null || condition.getTemp().isEmpty()) {
			tempView.setVisibility(View.GONE);
		} else {
			tempView.setVisibility(View.VISIBLE);
			tempView.setText(condition.getTemp());
		}
		
		if(condition.getDescription() == null || condition.getDescription().isEmpty()) {
			descriptionView.setVisibility(View.GONE);
		} else {
			descriptionView.setVisibility(View.VISIBLE);
			descriptionView.setText(condition.getDescription());
		}
		
		if(condition.getHumidity() == null || condition.getHumidity().isEmpty()) {
			humidityView.setVisibility(View.GONE);
		} else {
			humidityView.setVisibility(View.VISIBLE);
			humidityView.setText(condition.getHumidity());
		}
		
		if(condition.getWindSpeed() == null || condition.getWindSpeed().isEmpty()) {
			windView.setVisibility(View.GONE);
		} else {
			windView.setVisibility(View.VISIBLE);
			windView.setText(condition.getWindSpeed());
		}
		
		if(condition.getWindDir() == null || condition.getWindDir().isEmpty()) {
			windDirView.setVisibility(View.GONE);
		} else {
			windDirView.setVisibility(View.VISIBLE);
			windDirView.setText(condition.getWindDir());
		}
	}

	public void showCondition1() {
		if (condition != null && lastUpdateTimeView != null && iconView != null && tempView != null && descriptionView != null && humidityView != null && windView != null) {
			long millis = Long.parseLong(condition.getLastUpdateMilliseconds());
			String updateText = MyDateUtils.getLastUpdateString(mActivity, millis, false);
			if (updateText != null && !updateText.isEmpty()) {
				lastUpdateTimeView.setText(updateText);
				lastUpdateTimeView.setVisibility(View.VISIBLE);
				lastUpdateIconView.setVisibility(View.VISIBLE);
			} else {
				lastUpdateTimeView.setVisibility(View.GONE);
				lastUpdateIconView.setVisibility(View.GONE);
			}
			iconView.setImageDrawable(condition.getIconDrawable(mActivity));
			tempView.setText(condition.getTemp());
			descriptionView.setText(condition.getDescription());
			humidityView.setText(condition.getHumidity());
			windView.setText(condition.getWindSpeed());
			windDirView.setText(condition.getWindDir());
		}
	}

}
