package it.fdev.utils;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.ErrorInternetFragment;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class Utils {

	public final static String TAG = "UnisaConnect";
	public final static String TOGGLE_TESTING_STRING = "!testtoggle!";

	private static ProgressDialog dialog = null;
	private static boolean dialogOnCancelBoBack = false;
	private static AlertDialog alert = null;

//	private static Bitmap mBitmap;
//	private static Canvas mCanvas;
//	private static Rect mBounds;

	public static void goToInternetError(MainActivity activity, Fragment goBackFragment) {
		try {
			ErrorInternetFragment errorFrag = new ErrorInternetFragment();
			errorFrag.setBackFragment(goBackFragment);
			activity.getSupportFragmentManager().popBackStack();
			((MainActivity) activity).switchContent(errorFrag);
			dismissDialog();
			dismissAlert();
		} catch (Exception e) {
			Log.e(TAG, "Error loading interneterror frame");
			e.printStackTrace();
			return;
		}
	}

//	public static void openPlayStore(MainActivity activity) {
//		Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
//		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//		try {
//			activity.startActivity(goToMarket);
//		} catch (ActivityNotFoundException e) {
//			Toast.makeText(activity, "Impossibile aprire il Play Store", Toast.LENGTH_LONG).show();
//		}
//	}

	public static void sendSupportMail(MainActivity activity, String title, String subject) {
		String aEmailList[] = { activity.getString(R.string.dev_email) };
		sendMail(activity, aEmailList, title, subject);
	}

	public static void startDial(FragmentActivity fragmentActivity, String number) {
		String uri = "tel:" + number.trim();
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse(uri));
		fragmentActivity.startActivity(intent);
	}

	public static void sendMail(MainActivity activity, String[] recipients, String title, String subject) {
		// Intent
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		// define TO email
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject);
		// support multiple email clients
		activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.email_chooser_titolo)));
	}

	public static void createDialog(final MainActivity activity, String message, final boolean onCancelGoBack) {
		try {
			dismissAlert();
			if (onCancelGoBack == dialogOnCancelBoBack && dialog != null && dialog.isShowing()) {
				dialog.setMessage(message);
			} else {
				dismissDialog();
				dialogOnCancelBoBack = onCancelGoBack;
				dialog = new ProgressDialog(activity);
				dialog.setMessage(message);
				dialog.setIndeterminate(true);
				dialog.setCancelable(true);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
//						activity.setLoadingVisible(true);
						if (onCancelGoBack) {
							((MainActivity) activity).goToLastFrame();
						}
					}
				});
				dialog.show();
//				activity.setLoadingVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static void dismissDialog() {
		try {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
		} catch (Exception e) {
		}
		return;
	}
	
	public static void createAlert(final MainActivity activity, String message, final BootableFragmentsEnum goToFragmentEnum, final boolean shouldReturnToMenu) {
		try {
			dismissDialog();
			dismissAlert();
			alert = new AlertDialog.Builder(activity).create();
			alert.setCancelable(true);
			alert.setCanceledOnTouchOutside(false);
			alert.setIcon(R.drawable.ic_launcher);
			alert.setMessage(message);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							if (goToFragmentEnum != null)
								activity.switchContent(goToFragmentEnum, false);
							if (shouldReturnToMenu)
								((SlidingFragmentActivity) activity).getSlidingMenu().toggle();
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

//	/**
//	 * This method convets dp unit to equivalent device specific value in
//	 * pixels.
//	 * http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
//	 * -in-android#
//	 * 
//	 * @param dp
//	 *            A value in dp(Device independent pixels) unit. Which we need
//	 *            to convert into pixels
//	 * @param context
//	 *            Context to get resources and device specific display metrics
//	 * @return A float value to represent Pixels equivalent to dp according to
//	 *         device
//	 */
//	public static float convertDpToPixel(float dp, Context context) {
//		Resources resources = context.getResources();
//		DisplayMetrics metrics = resources.getDisplayMetrics();
//		float px = dp * (metrics.densityDpi / 160f);
//		return px;
//	}

//	/**
//	 * http://stackoverflow.com/questions/8089054/get-the-background-color-of-a-
//	 * button-in-android
//	 */
//	private static void initIfNeeded() {
//		if (mBitmap == null) {
//			mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
//			mCanvas = new Canvas(mBitmap);
//			mBounds = new Rect();
//		}
//	}
//
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	public static int getBackgroundColor(View view) {
//		// The actual color, not the id.
//		int color = Color.BLACK;
//
//		if (view.getBackground() instanceof ColorDrawable) {
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//				initIfNeeded();
//
//				// If the ColorDrawable makes use of its bounds in the draw
//				// method,
//				// we may not be able to get the color we want. This is not the
//				// usual
//				// case before Ice Cream Sandwich (4.0.1 r1).
//				// Yet, we change the bounds temporarily, just to be sure that
//				// we are
//				// successful.
//				ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
//
//				mBounds.set(colorDrawable.getBounds()); // Save the original
//														// bounds.
//				colorDrawable.setBounds(0, 0, 1, 1); // Change the bounds.
//
//				colorDrawable.draw(mCanvas);
//				color = mBitmap.getPixel(0, 0);
//
//				colorDrawable.setBounds(mBounds); // Restore the original
//													// bounds.
//			} else {
//				color = ((ColorDrawable) view.getBackground()).getColor();
//			}
//		}
//
//		return color;
//	}

}
