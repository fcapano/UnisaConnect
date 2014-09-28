package it.fdev.utils;

import it.fdev.unisaconnect.FragmentInternetError;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.R;

import java.io.Serializable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout.LayoutParams;

public class Utils {

	public final static String TAG = "UnisaConnect";
	public final static String TOGGLE_TESTING_STRING = "testing!";

	private static AlertDialog alert = null;

	public static void goToInternetError(MainActivity activity, Fragment goBackFragment) {
		try {
			FragmentInternetError errorFrag = new FragmentInternetError();
			errorFrag.setBackFragment(goBackFragment);
			activity.getSupportFragmentManager().popBackStack();
			((MainActivity) activity).switchContent(errorFrag);
			dismissAlert();
		} catch (Exception e) {
			Log.e(TAG, "Error loading interneterror frame");
			e.printStackTrace();
			return;
		}
	}

	public static void startDial(FragmentActivity fragmentActivity, String number) {
		try {
			String uri = "tel:" + number.trim();
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse(uri));
			fragmentActivity.startActivity(intent);
		} catch (Exception e) {
			// No Activity found to handle Intent { act=android.intent.action.DIAL...
			// Tablet? La chiamata non partirà. Ignora l'errore
		}
	}

	public static void sendSupportMail(MainActivity activity, String title, String subject) {
		String email = activity.getString(R.string.dev_email);
		sendMail(activity, email, title, subject);
	}

	public static void sendMail(MainActivity activity, String recipient, String title, String subject) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipient, null));
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject);
		activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.email_chooser_titolo)));
	}

	public static void createAlert(final MainActivity activity, String message, final BootableFragmentsEnum goToFragmentEnum, final boolean shouldReturnToMenu) {
		try {
			dismissAlert();
			alert = new AlertDialog.Builder(activity).create();
			alert.setCancelable(true);
			alert.setCanceledOnTouchOutside(false);
			alert.setIcon(R.drawable.ic_launcher);
			alert.setMessage(message);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							if (goToFragmentEnum != null)
								activity.switchContent(goToFragmentEnum, true);
							if (shouldReturnToMenu)
								((MainActivity) activity).toggleDrawer();
						}
					});
				}
			});
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void dismissAlert() {
		if (alert != null && alert.isShowing()) {
			try {
				alert.dismiss();
				alert = null;
			} catch (Exception e) {
			}
		}
		return;
	}

	/**
	 * Checks if the device has Internet connection.
	 * 
	 * @return <code>true</code> if the phone is connected to the Internet.
	 */
	public static boolean hasConnection(MainActivity activity) {
		try {
			ConnectivityManager cm = (ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetwork != null && wifiNetwork.isConnected()) {
				return true;
			}
			NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mobileNetwork != null && mobileNetwork.isConnected()) {
				return true;
			}
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null && activeNetwork.isConnected()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error checking internet connection.");
			return true;
		}
	}
	
	/*
	 * General send message through app/broadcasts
	 */
	public static boolean broadcastStatus(Context ctx, String action, String name, Object object) {
		Intent localIntent = new Intent(action);
		if (object != null) {
			if (object instanceof Parcelable) {
				localIntent.putExtra(name, (Parcelable) object);
			} else if (object instanceof Serializable) {
				localIntent.putExtra(name, (Serializable) object);
			} else {
				Log.w(TAG, "Object not serializable/parcelable: " + name);
				return false;
			}
		}
		ctx.sendBroadcast(localIntent);
		return true;
	}

	public static void sendLoadingMessage(Context ctx, int messageRes) {
		Intent localIntent = new Intent(MainActivity.BROADCAST_LOADING_MESSAGE);
		localIntent.putExtra("message_res", messageRes);
		ctx.sendBroadcast(localIntent);
	}
	
	/////// Expand / Collapse animations ///////////
	public static void expand(final View v) {
		expand(v, 3);
	}
	
	public static void expand(final View v, int slowness) {
		if (v.getVisibility() == View.VISIBLE) {
			return;
		}
		slowness = Math.max(Math.min(slowness, 10), 0);
		v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final int targettHeight = v.getMeasuredHeight();

		v.getLayoutParams().height = 0;
		v.setVisibility(View.VISIBLE);
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				v.getLayoutParams().height = interpolatedTime == 1 ? LayoutParams.WRAP_CONTENT : (int) (targettHeight * interpolatedTime);
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 1dp/slowness*ms
		a.setDuration((int) (targettHeight / v.getContext().getResources().getDisplayMetrics().density) * slowness);
		v.startAnimation(a);
	}
	
	public static void collapse(final View v) {
		collapse(v, 3);
	}

	public static void collapse(final View v, int slowness) {
		if (v.getVisibility() == View.GONE) {
			return;
		}
		slowness = Math.max(Math.min(slowness, 10), 0);
		final int initialHeight = v.getMeasuredHeight();

		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				} else {
					v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		// 1dp/slowness*ms
		a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) * slowness);
		v.startAnimation(a);
	}

}
