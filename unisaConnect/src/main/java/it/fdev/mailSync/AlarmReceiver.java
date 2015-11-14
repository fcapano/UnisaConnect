package it.fdev.mailSync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmService = new Intent(context, AlarmService.class);
		context.startService(alarmService);
	}
}