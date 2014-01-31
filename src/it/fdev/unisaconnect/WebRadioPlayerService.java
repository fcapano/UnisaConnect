package it.fdev.unisaconnect;

import it.fdev.utils.DrawableManager;
import it.fdev.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;

public class WebRadioPlayerService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener, AudioManager.OnAudioFocusChangeListener, OnInfoListener {

	private static final String ICECAST_URL = "http://streamingradio.unisa.it/status.xsl?mount=/stream";
	private static final String LASTFM_API_KEY = "5d28b9b80d89cebd38f1f604e5ddf01d";
	public static final String BROADCAST_STATUS_CHANGED = "it.fdev.webradio.STATUS_CHANGED";
	public static final String ACTION_PLAY = "it.fdev.webradio.PLAY";
	public static final String ACTION_STOP = "it.fdev.webradio.STOP";
	public static final String ACTION_UPDATE = "it.fdev.webradio.UPDATE";
	public static final int NOTIFICATION_ID = 1;
	public static final int INTERVAL_FETCH_METADATA = 15 * 1000; // Milliseconds

	private static MediaPlayer mMediaPlayer = null;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> matedataRetrieverHandle;
	private NotificationManager mNotificationManager = null;
	private NotificationCompat.Builder mNotifyBuilder = null;
	private RemoteViews mNotifyContentView = null;

	private boolean isBuffering = false;
	private String lastMetadata;
	private String mSongTitle, mSongArtist, mAlbumArtFileName;
	private Bitmap mAlbumArt;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mNotificationManager == null) {
			if (intent == null) {
				return super.onStartCommand(intent, flags, startId);
			}
			if (!intent.getAction().equals(ACTION_PLAY)) {
				stopService();
				return super.onStartCommand(intent, flags, startId);
			}

			Intent intent_notification = new Intent(this, MainActivity.class);
			intent_notification.setAction(MainActivity.INTENT_LAUNCH_FRAGMENT);
			intent_notification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent_notification.putExtra("launch_fragment", MainActivity.BootableFragmentsEnum.WEB_RADIO);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent_notification, PendingIntent.FLAG_UPDATE_CURRENT);

			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotifyContentView = new RemoteViews(getPackageName(), R.layout.web_radio_notification);
			mNotifyContentView.setTextViewText(R.id.song_name, "Title");
			mNotifyContentView.setTextViewText(R.id.song_artist, "Artist");

			CloseButtonListener closeButtonListener = new CloseButtonListener();
			registerReceiver(closeButtonListener, new IntentFilter("it.fdev.unisaconnect.close_listener"));
			Intent switchIntent = new Intent("it.fdev.unisaconnect.close_listener");
			PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mNotifyContentView.setOnClickPendingIntent(R.id.button_stop, pendingSwitchIntent);

			mNotifyBuilder = new NotificationCompat.Builder(this).setContentTitle("Title").setContentText("Artist").setAutoCancel(false).setSmallIcon(R.drawable.ic_stat_music).setContentIntent(pIntent).setOngoing(true).setWhen(0).setContent(mNotifyContentView);
		}

		if (intent == null || intent.getAction() == null) {
			return super.onStartCommand(intent, flags, startId);
		}

		if (intent.getAction().equals(ACTION_PLAY)) {
			if (mMediaPlayer != null) { // Already playing
				return START_STICKY;
				// return super.onStartCommand(intent, flags, startId);
			}
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				return START_STICKY;
			}
			isBuffering = true;
			sendStatusBroadcast();
			try {
				final String streamingURL = intent.getStringExtra("streamingURL");
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setDataSource(streamingURL);
				mMediaPlayer.setOnCompletionListener(this);
				mMediaPlayer.setOnPreparedListener(this);
				mMediaPlayer.setOnErrorListener(this);
				mMediaPlayer.setOnInfoListener(this);
				mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
				mMediaPlayer.prepareAsync();
				isBuffering = true;

				Runnable retrieverTask = new Runnable() {
					public void run() {
						try {
							retrieveMetadata();
//							retrieveMetadata(streamingURL);
						} catch (Exception e) {
							Log.e(Utils.TAG, "Exception in WebRadioPlayerService", e);
						}
					};
				};
				matedataRetrieverHandle = scheduler.scheduleWithFixedDelay(retrieverTask, 0, INTERVAL_FETCH_METADATA, TimeUnit.MILLISECONDS);
				return START_STICKY;
			} catch (Exception e) {
				Log.e(Utils.TAG, "Exception in WebRadioPlayerService", e);
				stopService();
			}
		} else if (intent.getAction().equals(ACTION_STOP)) {
			stopService();
		} else if (intent.getAction().equals(ACTION_UPDATE)) {
			sendStatusBroadcast();
			return START_STICKY;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void stopService() {
		if (mNotificationManager != null) {
			mNotificationManager.cancelAll();
		}
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (matedataRetrieverHandle != null) {
			scheduler.shutdownNow();
			matedataRetrieverHandle.cancel(true);
			matedataRetrieverHandle = null;
		}
		isBuffering = false;
		sendStatusBroadcast();
		Log.d("Player Service", "Player Service Stopped");
		stopForeground(true);
		stopSelf();
	}

	// Create Notification
	private void updateNotification() {
		if (mMediaPlayer==null || !mMediaPlayer.isPlaying()) {
			return;
		}
		if (mSongTitle == null) {
			mSongTitle = getString(R.string.unisound);
		}
		mNotifyContentView.setTextViewText(R.id.song_name, mSongTitle);
		mNotifyContentView.setTextViewText(R.id.song_artist, mSongArtist);
		if (mAlbumArt == null) {
			mNotifyContentView.setImageViewResource(R.id.song_album_art, R.drawable.music_note);
		} else {
			mNotifyContentView.setImageViewBitmap(R.id.song_album_art, mAlbumArt);
		}

		mNotifyBuilder.setContentTitle(mSongTitle).setContentText(mSongArtist);

		mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
	}

	private void sendStatusBroadcast() {
		boolean isPlaying = false;
		if (mMediaPlayer != null) {
			isPlaying = mMediaPlayer.isPlaying();
		}
		if (mSongTitle == null) {
			mSongTitle = getString(R.string.unisound);
		}
		Intent intent = new Intent(BROADCAST_STATUS_CHANGED);
		intent.putExtra("is_playing", isPlaying);
		intent.putExtra("is_buffering", isBuffering);
		intent.putExtra("title", mSongTitle);
		intent.putExtra("artist", mSongArtist);
		intent.putExtra("imageFile", mAlbumArtFileName);
		getApplicationContext().sendBroadcast(intent);
	}

	/**
	 * Listeners
	 */
	@Override
	public void onDestroy() {
		stopService();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(Utils.TAG, "WebRadioPlayerService | onCompletition");
		isBuffering = false;
		stopService();
		sendStatusBroadcast();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(Utils.TAG, "WebRadioPlayerService | onPrepared");
		isBuffering = false;
		mp.start();
		updateNotification();
		sendStatusBroadcast();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.d(Utils.TAG, "WebRadioPlayerService | onInfo: " + what);
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
			isBuffering = true;
			mp.pause();
		} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			isBuffering = false;
			mp.start();
		}
		updateNotification();
		sendStatusBroadcast();
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(Utils.TAG, "WebRadioPlayerService | onError: " + what);
		stopService();
		return false;
	}

	// http://developer.android.com/guide/topics/media/mediaplayer.html
	@Override
	public void onAudioFocusChange(int focusChange) {
		Log.d(Utils.TAG, "WebRadioPlayerService | onAudioFocusChange: " + focusChange);
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			if (mMediaPlayer == null)
				return;
			else if (!mMediaPlayer.isPlaying())
				mMediaPlayer.start();
			mMediaPlayer.setVolume(1.0f, 1.0f);
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and release media player
			if (mMediaPlayer!=null && mMediaPlayer.isPlaying())
				stopService();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
				isBuffering = false;
				mMediaPlayer.pause();
			}
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
//			if (mMediaPlayer.isPlaying()) {
//				isBuffering = false;
//				mMediaPlayer.pause();
//			}
			if (mMediaPlayer!=null && mMediaPlayer.isPlaying())
				mMediaPlayer.setVolume(0.2f, 0.2f);
			break;
		}
		updateNotification();
		sendStatusBroadcast();
	}

	public void fetchAndSaveAlbumArt() {
		mAlbumArt = null;
		mAlbumArtFileName = null;
		try {
			Caller.getInstance().setCache(null);
			Collection<Track> trcks = Track.search(mSongArtist, mSongTitle, 1, LASTFM_API_KEY);
			if (trcks.size() <= 0) {
				return;
			}
			Track mTrack = (Track) trcks.toArray()[0];
			String albumArtURL = mTrack.getImageURL(ImageSize.LARGE);
			Drawable image = new DrawableManager().fetchDrawable(albumArtURL);
			mAlbumArt = ((BitmapDrawable) image).getBitmap();
			saveAlbumArt();
			return;
		} catch (Exception e) {
			Log.e(Utils.TAG, "Exception in WebRadioPlayerService", e);
			mAlbumArt = null;
			return;
		}
	}

	public void saveAlbumArt() {
		String fileName = "current_album_art.png";
		if (mAlbumArt == null) {
			mAlbumArtFileName = null;
			return;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mAlbumArt.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] b = baos.toByteArray();
			FileOutputStream fileOutStream = openFileOutput(fileName, MODE_PRIVATE);
			fileOutStream.write(b);
			fileOutStream.close();
		} catch (Exception e) {
			Log.e(Utils.TAG, "Exception in WebRadioPlayerService", e);
			mAlbumArtFileName = null;
			return;
		}
		mAlbumArtFileName = fileName;
		return;
	}

	public class CloseButtonListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent playerService = new Intent(context, WebRadioPlayerService.class);
			playerService.setAction(WebRadioPlayerService.ACTION_STOP);
			context.startService(playerService);
		}
	}
	
	private void retrieveMetadata() {
		try {
			Response response = Jsoup.connect(ICECAST_URL).timeout(30000).execute();
			Document document = response.parse();
			String metadata = document.getElementsContainingOwnText("Current Song").first().nextElementSibling().text();
			if (metadata.equalsIgnoreCase(lastMetadata))
				return;
			lastMetadata = metadata;
			mAlbumArt = null;
			mAlbumArtFileName = null;
			metadata = metadata.replaceAll("^UniS@und - ", "");
			if (metadata.contains("-")) {
				int indexSep = metadata.indexOf("-");
				mSongArtist = metadata.substring(0, indexSep).trim();
				mSongTitle = metadata.substring(indexSep + 1).trim();
			} else {
				mSongTitle = metadata;
				mSongArtist = null;
				mAlbumArt = null;
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "Exception in WebRadioPlayerService", e);
			mSongTitle = null;
			mSongArtist = null;
			mAlbumArt = null;
			mAlbumArtFileName = null;
		}
		
		if (mSongTitle == null) {
			// Problema ner reperire il tag. -> imposto una notifica generica
			mSongArtist = null;
			mAlbumArt = null;
			mAlbumArtFileName = null;
		}

		updateNotification();
		sendStatusBroadcast();

		if (mSongTitle != null && mSongArtist != null) {
			fetchAndSaveAlbumArt();
			updateNotification();
			sendStatusBroadcast();
		}
	}

}