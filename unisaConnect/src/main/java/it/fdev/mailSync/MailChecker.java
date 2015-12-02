package it.fdev.mailSync;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sun.mail.imap.IMAPFolder;

import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

public class MailChecker {

	public final static String IMAP_SERVER = "imap.studenti.unisa.it";
	public static final int MAIL_NOTIFICATION_ID = 2;

	//	public static final long ALARM_INTERVAL = 30 * 1000;
	//	public static final long ALARM_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	public static final long ALARM_INTERVAL = AlarmManager.INTERVAL_HOUR * 2;
	public static final long FIRST_ALARM_DELAY = 1;

	public void doRead(Context context) {

		Log.v(Utils.TAG, "Checking webmail...");

		SharedPrefDataManager mDM = SharedPrefDataManager.getInstance(context);
		if (!mDM.loginDataExists()) {
			return;
		}

		IMAPFolder folder = null;
		Store store = null;

		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			Session session = Session.getDefaultInstance(props, null);
			store = session.getStore("imaps");
			store.connect(IMAP_SERVER, mDM.getUser() + "@studenti.unisa.it", mDM.getPass());

			folder = (IMAPFolder) store.getFolder("inbox");
			if (!folder.isOpen()) {
				folder.open(Folder.READ_ONLY);
			}

			int numMsg = folder.getMessageCount();
			if (numMsg < 1) {
				return;
			}

			Message lastMessage = folder.getMessages(numMsg, numMsg)[0];
			Date lastReceivedDate = lastMessage.getReceivedDate();
			if (!lastReceivedDate.after(mDM.getMailLastRead())) {
//				Log.v(Utils.TAG, "Last mail already notified");
				return;
			}

			int unreadNumMsg = folder.getUnreadMessageCount();
			if (unreadNumMsg > 0)
				createNofitication(context, unreadNumMsg);

			mDM.setMailLastRead(lastReceivedDate);
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (folder != null && folder.isOpen()) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}

	}
	
	private void createNofitication(Context context, int numUnread) {
		Intent intent_notification = new Intent(context, MainActivity.class);
		intent_notification.setAction(MainActivity.INTENT_LAUNCH_FRAGMENT);
		intent_notification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent_notification.putExtra("launch_fragment", MainActivity.BootableFragmentsEnum.WEBMAIL);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent_notification, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
																			.setContentTitle("Unisa Webmail")
																			.setContentText(context.getString(R.string.mail_non_lette, numUnread))
																			.setAutoCancel(true)
																			.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
																			.setSmallIcon(R.drawable.ic_stat_mail)
																			.setContentIntent(pIntent)
																			.setOngoing(false)
																			.setOnlyAlertOnce(true)
																			.setWhen(System.currentTimeMillis());
		Notification cNotification = mNotifyBuilder.build();
		cNotification.defaults |=  Notification.DEFAULT_ALL;
		cNotification.audioStreamType = AudioManager.STREAM_NOTIFICATION;
		mNotificationManager.notify(MAIL_NOTIFICATION_ID, cNotification);
	}

	/**
	 * Controlla se sono memorizzati i dati utente (user/pass)
	 * Controlla la preferenza di sistema per il controllo automatico della mail
	 * @param context
	 */
	public static void autoSetAlarm(Context context) {
		SharedPrefDataManager mDM = SharedPrefDataManager.getInstance(context);
		if (!mDM.loginDataExists()) {
			cancelAlarm(context);
			return;
		}
		boolean mailCheckEnabled = mDM.getMailDoCheck();
		if (mailCheckEnabled) {
			setAlarm(context);
		} else {
			cancelAlarm(context);
		}
	}

	/**
	 * Set alarm for background sync service
	 * @param context
	 */
	private static void setAlarm(Context context) {
		if (isAlarmUp(context)) {
			return;
		}
//		Log.d(Utils.TAG, "Set alarm");
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, FIRST_ALARM_DELAY, ALARM_INTERVAL, pendingIntent);
	}
	
	private static void cancelAlarm(Context context) {
		Intent i = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		if (pendingI != null) {
//			Log.d(Utils.TAG, "Canceling alarm...");
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingI);
			pendingI.cancel();
		}
	}

	private static boolean isAlarmUp(Context context) {
		Intent i = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE);
//		Log.d(Utils.TAG, "Alarm exists: " + (pendingI!=null));
		return pendingI != null;
	}
}
