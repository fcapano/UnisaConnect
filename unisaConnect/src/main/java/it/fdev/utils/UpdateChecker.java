package it.fdev.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager.BadTokenException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import it.fdev.unisaconnect.R;

/**
 * Version 1.1 - For latest version check here: http://blog.fdev.eu/updatechecker/
 * 
 * @author Francesco Capano
 */
public class UpdateChecker {

	public final static String VERSION_URL = "http://fdev.eu/unisaconnect/version";
	public final static String BETA_VERSION_URL = "http://fdev.eu/unisaconnect/version_beta";

	private final static int TIME_TOLERANCE = (24 * 60 * 60 * 1000); // 24 ore

	private final String TAG_SUFFIX = "UpdateChecker";
	private String TAG = Utils.TAG;

	// The dialog that tells you to update
	private String dialogTitle = "Aggiorna Unisa Connect";
	private String dialogMessage = "E' disponibile una nuova versione di Unisa Connect. Vuoi aggiornare l'app adesso?";
	private String okButtonText = "Aggiorna";
	private String laterButtonText = "Più tardi";
	private String nextVersionButtonText = "No, grazie";

	private int newVersionCode;
	// private String newVersionName;
	// private String changelog;

	private Handler mHandler;
	private Context mContext;
	private String versionUrl;
	private long lastUpdateTime;
	private int versionToIgnore;
	private SharedPreferences mPref;
	private boolean isBetaVersion;

	/* This Thread checks for Updates in the Background */
	private Thread checkUpdate;

	public UpdateChecker(Context c) {
		this.mContext = c;

		try {
			isBetaVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName.toLowerCase(Locale.ENGLISH).contains("beta");
		} catch (NameNotFoundException e) {
			isBetaVersion = false;
		}
		if (isBetaVersion) {
			this.versionUrl = BETA_VERSION_URL;
		} else {
			this.versionUrl = VERSION_URL;
		}

		/* Get Last Update Time from Preferences */
		mPref = mContext.getSharedPreferences("updatechecker", Context.MODE_PRIVATE);
		// mPref.edit().remove("lastUpdateTime").commit();
		// mPref.edit().remove("versionToIgnore").commit();
		lastUpdateTime = mPref.getLong("lastUpdateTime", 0);
		versionToIgnore = mPref.getInt("versionToIgnore", 0);

		mHandler = new Handler();
	}

	public void start() {
		try {
			/* Should Activity Check for Updates Now? */
			if ((lastUpdateTime + TIME_TOLERANCE) > System.currentTimeMillis()) {
				Log.v(TAG, "I checked for updates a short time ago.");
				return;
			}

			if (checkUpdate == null || !checkUpdate.isAlive() || checkUpdate.isInterrupted()) {
				checkUpdate = new Thread() {
					public void run() {
						checkupdate();
					}
				};
				checkUpdate.start();
			}
		} catch (Exception e) {
			Log.e(Utils.TAG, "Error checking for updates!", e);
		}
	}

	private Runnable showUpdate = new Runnable() {
		public void run() {
			try {
				QustomDialogBuilder builder = new QustomDialogBuilder(mContext);

				builder.setTitle(dialogTitle);
				builder.setMessage(dialogMessage);
				builder.setIcon(R.drawable.ic_action_logo_dark);
				builder.setDividerColor(mContext.getResources().getColor(R.color.orange_actionbar));

				builder.setPositiveButton(okButtonText, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName()));
						mContext.startActivity(intent);
					}
				});

				builder.setNeutralButton(laterButtonText, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});

				builder.setNegativeButton(nextVersionButtonText, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
//						versionToIgnore = newVersionCode;
//						SharedPreferences.Editor editor = mPref.edit();
//						editor.putInt("versionToIgnore", versionToIgnore);
//						editor.commit();
						
						/* Save futire timestamp for next Check */
						long futureTime = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7 * 2); // Two weeks
						SharedPreferences.Editor editor = mPref.edit();
						editor.putLong("lastUpdateTime", futureTime);
						editor.commit();
					}
				});

				builder.show();
			} catch (BadTokenException e) {
				// Activity not running anymore
			}
		}
	};

	private void checkupdate() {
		Log.v(TAG, "Checking updates...");

		try {
			URL url = new URL(versionUrl);
			URLConnection conn = url.openConnection();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(conn.getInputStream());
			// Node newVersionNameElement = doc.getElementsByTagName("version_name").item(0);
			// newVersionName = newVersionNameElement.getTextContent();
			Node versionCodeElement = doc.getElementsByTagName("v").item(0);
			newVersionCode = Integer.parseInt(versionCodeElement.getTextContent());
			// Node changelogElement = doc.getElementsByTagName("changelog").item(0);
			// changelog = changelogElement.getTextContent();

			/* Save current timestamp for next Check */
			lastUpdateTime = System.currentTimeMillis();
			SharedPreferences.Editor editor = mPref.edit();
			editor.putLong("lastUpdateTime", lastUpdateTime);
			editor.commit();

			int curVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;

			Log.d(TAG, "Current version is: " + curVersion + " and new one is: " + newVersionCode);
			/* Is a higher version than the current already out? */
			if (curVersion < newVersionCode) {
				if (newVersionCode != versionToIgnore) {
					Log.d(TAG, "Showing dialog...");
					mHandler.post(showUpdate);
				} else {
					Log.d(TAG, "Version " + versionToIgnore + " was marked to ignore");
				}
			} else {
				Log.v(TAG, "The software is updated to the latest version: " + newVersionCode);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public void setTagPrefix(String tagPrefix) {
		TAG = tagPrefix + " " + TAG_SUFFIX;
	}

}