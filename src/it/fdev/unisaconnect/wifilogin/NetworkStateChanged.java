package it.fdev.unisaconnect.wifilogin;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class NetworkStateChanged extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPrefDataManager mDataManager = new SharedPrefDataManager(context);
		
		// Check preference
		if (!mDataManager.isLoginAutomatica()) {
			// Disable the BroadcastReceiver so it isn't called in the future
			Log.d(Utils.TAG, "Autologin is disabled, disabling broadcast");
			setEnableBroadcastReceiver(context, false);
			return;
		}
		
		if(AsyncLogin.isLoginRunning)
			return;
		
		Log.v(Utils.TAG, "Autologin is enabled");
		
		Intent i = new Intent(context, LoginManager.class);
		context.startService(i);
		
	}
	
	public static void setEnableBroadcastReceiver(Context context, boolean enabled) {
		Log.v(Utils.TAG, "Setting BroadcastReceiver status to: " + enabled);
		ComponentName receiver = new ComponentName(context, NetworkStateChanged.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		context.getPackageManager().setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
	}
}
