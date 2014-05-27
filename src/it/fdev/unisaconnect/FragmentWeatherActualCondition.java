package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.WeatherData.ActualCondition;
import it.fdev.utils.DrawableManager;
import it.fdev.utils.DrawableManager.DrawableManagerListener;
import it.fdev.utils.MyDateUtils;
import it.fdev.utils.Utils;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentWeatherActualCondition extends Fragment {

	private Activity activity;
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
						try {
							downloadedWebcamImg = cropDrawable(image, 0, 10, 0, 0);
						} catch (Exception e) {
							downloadedWebcamImg = image;
							Log.w(Utils.TAG, e);
						}
						webcamView.setImageDrawable(downloadedWebcamImg);
					}
				});
			} else {
				// The image was already downloaded
				webcamView.setImageDrawable(downloadedWebcamImg);
			}
		}
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
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
		if (condition != null && lastUpdateTimeView != null && iconView != null && tempView != null && descriptionView != null && humidityView != null && windView != null) {
			long millis = Long.parseLong(condition.getLastUpdateMilliseconds());
			String updateText = MyDateUtils.getLastUpdateString(activity, millis, false);
			if (updateText != null && !updateText.isEmpty()) {
				lastUpdateTimeView.setText(updateText);
				lastUpdateTimeView.setVisibility(View.VISIBLE);
				lastUpdateIconView.setVisibility(View.VISIBLE);
			} else {
				lastUpdateTimeView.setVisibility(View.GONE);
				lastUpdateIconView.setVisibility(View.GONE);
			}
			iconView.setImageDrawable(condition.getIconDrawable(activity));
			tempView.setText(condition.getTemp());
			descriptionView.setText(condition.getDescription());
			humidityView.setText(condition.getHumidity());
			windView.setText(condition.getWindSpeed());
			windDirView.setText(condition.getWindDir());
		}
	}

	public Drawable cropDrawable(Drawable drawable, int left, int top, int right, int bottom) {
		int originalWidth = drawable.getIntrinsicWidth();
		int originalHeight = drawable.getIntrinsicHeight();
		int resizedWidth = originalWidth - left - right;
		int resizedHeight = originalHeight - top - bottom;

		Bitmap mutableBitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, originalWidth, originalHeight);
		drawable.draw(canvas);
		Bitmap resizedbitmap = Bitmap.createBitmap(mutableBitmap, left, top, resizedWidth, resizedHeight);
		return new BitmapDrawable(getResources(), resizedbitmap);
	}

}
