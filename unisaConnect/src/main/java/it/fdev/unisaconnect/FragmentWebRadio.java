package it.fdev.unisaconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import it.fdev.utils.MySimpleFragment;

public class FragmentWebRadio extends MySimpleFragment {

	private final String STREAMING_URL = "http://streamingradio.unisa.it/stream";
	private final String STREAMING_URL_64 = "http://streamingradio.unisa.it/stream64";
	private final String UNISOUND_WEB_PAGE = "http://iunisa.unisa.it/WEBRADIO-6.html";

	private ImageView unisoundLogoBig, albumArtView, btnPlayView, btnStopView;
	private TextView titleView, artistView;
	private ProgressBar bufferingSpinnerView;

	IntentFilter mPlayerFilter = new IntentFilter();

	private final BroadcastReceiver mHandlerPlayerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			boolean isPlaying, isBuffering;
			String title, artist;
			Drawable albumArt = null;

			isPlaying = extras.getBoolean("is_playing");
			isBuffering = extras.getBoolean("is_buffering");
			title = extras.getString("title");
			artist = extras.getString("artist");
			String imageFile = extras.getString("imageFile");
			if (imageFile != null) {
				File filePath = mActivity.getFileStreamPath(imageFile);
				albumArt = Drawable.createFromPath(filePath.toString());
			}
			if (isBuffering && !isPlaying) {
				btnPlayView.setVisibility(View.GONE);
				btnStopView.setVisibility(View.GONE);
				bufferingSpinnerView.setVisibility(View.VISIBLE);
			} else {
				if (isPlaying) {
					btnPlayView.setVisibility(View.GONE);
					btnStopView.setVisibility(View.VISIBLE);
					bufferingSpinnerView.setVisibility(View.GONE);
				} else {
					btnPlayView.setVisibility(View.VISIBLE);
					btnStopView.setVisibility(View.GONE);
					bufferingSpinnerView.setVisibility(View.GONE);
				}
			}
			if (!isBuffering) {
				titleView.setText(title);
				artistView.setText(artist);
				if (albumArt != null) {
					albumArtView.setImageDrawable(albumArt);
				} else {
					albumArtView.setImageResource(R.drawable.music_note_dark);
				}
			}
			if (isBuffering || title.isEmpty()) {
				titleView.setText(R.string.unisound);
				artistView.setText("");
				albumArtView.setImageResource(R.drawable.music_note_dark);
			}
		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.fragment_web_radio, container, false);
		mPlayerFilter.addAction(WebRadioPlayerService.BROADCAST_STATUS_CHANGED);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		unisoundLogoBig = (ImageView) view.findViewById(R.id.unisound_logo_big);
		albumArtView = (ImageView) view.findViewById(R.id.song_album_art);
		btnPlayView = (ImageView) view.findViewById(R.id.button_play);
		btnStopView = (ImageView) view.findViewById(R.id.button_stop);
		bufferingSpinnerView = (ProgressBar) view.findViewById(R.id.buffering_spinner);
		titleView = (TextView) view.findViewById(R.id.song_name);
		artistView = (TextView) view.findViewById(R.id.song_artist);

		// Set spinner color
		bufferingSpinnerView.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

		unisoundLogoBig.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(UNISOUND_WEB_PAGE));
				startActivity(i);
				return true;
			}
		});

		btnPlayView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent playerService = new Intent(mActivity, WebRadioPlayerService.class);
				playerService.setAction(WebRadioPlayerService.ACTION_PLAY);
				playerService.putExtra("streamingURL", STREAMING_URL);
				mActivity.startService(playerService);
			}
		});

		btnStopView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent playerService = new Intent(mActivity, WebRadioPlayerService.class);
				playerService.setAction(WebRadioPlayerService.ACTION_STOP);
				mActivity.startService(playerService);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.registerReceiver(mHandlerPlayerReceiver, mPlayerFilter);
		requestUpdateFromService();
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mHandlerPlayerReceiver);
	}

	@Override
	public int getTitleResId() {
		return R.string.unisound;
	}

	private void requestUpdateFromService() {
		Intent playerService = new Intent(mActivity, WebRadioPlayerService.class);
		playerService.setAction(WebRadioPlayerService.ACTION_UPDATE);
		mActivity.startService(playerService);
	}

}