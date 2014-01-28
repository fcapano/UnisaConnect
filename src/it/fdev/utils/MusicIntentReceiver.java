package it.fdev.utils;

import it.fdev.unisaconnect.WebRadioPlayerService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// http://developer.android.com/guide/topics/media/mediaplayer.html
public class MusicIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
//			Log.d(Utils.TAG, "ACTION_AUDIO_BECOMING_NOISY");
			Intent playerService = new Intent(ctx, WebRadioPlayerService.class);
			playerService.setAction(WebRadioPlayerService.ACTION_STOP);
			ctx.startService(playerService);
		}
	}

}
