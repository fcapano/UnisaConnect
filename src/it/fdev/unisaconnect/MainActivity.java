package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyFragment;
import it.fdev.utils.Utils;

import java.util.ArrayList;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements OnCloseListener, OnOpenListener {

	public static final String TAG = Utils.TAG;
	public static boolean isDebugMode = false;
	
	// Fragments which can be started at application startup (do not require special configuration to start)
//	public static enum bootableFragments {
//		WIFI_PREF, MENSA, WEBMAIL, ESSE3_SERVICES, STAFF_SEARCH, TIMETABLE, MAP, WEATHER
//	}
//	public static HashMap<bootableFragments, Class<? extends MyFragment>> fragmentsIDs = new HashMap<MainActivity.bootableFragments, Class<? extends MyFragment>>();
//	static {
//		fragmentsIDs.put(bootableFragments.WIFI_PREF, WifiPreferencesFragment.class);
//		fragmentsIDs.put(bootableFragments.MENSA, MensaFragment.class);
//		fragmentsIDs.put(bootableFragments.WEBMAIL, WebmailFragment.class);
//		fragmentsIDs.put(bootableFragments.ESSE3_SERVICES, Esse3ServicesFragment.class);
//		fragmentsIDs.put(bootableFragments.STAFF_SEARCH, StaffSearchFragment.class);
//		fragmentsIDs.put(bootableFragments.TIMETABLE, TimetableFragment.class);
//		fragmentsIDs.put(bootableFragments.MAP, MapFragment.class);
//		fragmentsIDs.put(bootableFragments.WEATHER, WeatherFragment.class);
//	}
	public static ArrayList<Class<? extends MyFragment>> bootableFragments = new ArrayList<Class<? extends MyFragment>>();
	static {
		bootableFragments.add(WifiPreferencesFragment.class);
		bootableFragments.add(MensaFragment.class);
		bootableFragments.add(WebmailFragment.class);
		bootableFragments.add(Esse3ServicesFragment.class);
		bootableFragments.add(StaffSearchFragment.class);
		bootableFragments.add(TimetableFragment.class);
		bootableFragments.add(MapFragment.class);
		bootableFragments.add(WeatherFragment.class);
	}
	
	private boolean menuAlreadyToggled = false; // After the first time in onPostCreate the menu shouldn't be toggled automatically
	private boolean showMenuOnStartup = true;	// Show the sliding menu on startup
	private SlidingMenuFragment menuFragment;
	private Menu sherlockMenu;
	private SlidingMenu sm;
	private SharedPrefDataManager sharedPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			isDebugMode = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
			if (isDebugMode) {
				Log.d(Utils.TAG, "The applications is running as debuggable!");
			}
		} catch (Exception e) {
			isDebugMode = false;
		}
		
		// Initialize Crittercism
		Crittercism.init(getApplicationContext(), "5135ccc2558d6a05f7000024");
		Crittercism.setOptOutStatus(isDebugMode);

		// Initialize Google Analytics
		EasyTracker.getInstance().setContext(this);
		EasyTracker.getInstance().activityStart(this);
		
		sharedPref = SharedPrefDataManager.getDataManager(this);
		
		// Start the last used fragment
//		Class<? extends MyFragment> fragmentClassToBoot = sharedPref.getBootFragmentClass();
//		MyFragment fragmentToBoot;
//		try {
//			fragmentToBoot = fragmentClassToBoot.newInstance();
//		} catch (Exception e) {
//			fragmentToBoot = new WifiPreferencesFragment();
//		}
		
		MyFragment fragmentToBoot = new WifiPreferencesFragment();
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// set the Above View
		setContentView(R.layout.content_frame);

		// customize the SlidingMenu
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidthRes(R.dimen.slidingmenu_width);
//		sm.setShadowDrawable(R.drawable.shadow);
//		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setOnCloseListener(this);
		sm.setOnOpenListener(this);

		// Initialize image downloader used by fragments
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).memoryCacheExtraOptions(100, 100).build();
		ImageLoader.getInstance().init(config);

		// Initialize views
		hideActions();
		menuFragment = new SlidingMenuFragment();
		Fragment startFragment = (Fragment) fragmentToBoot;
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, menuFragment).replace(R.id.content_frame, startFragment, startFragment.getClass().toString()).commit();

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		try {
			switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
			case R.id.action_add_button:
				((MyFragment) fragment).actionAdd();
				break;
			case R.id.action_edit_button:
				((MyFragment) fragment).actionEdit();
				break;
			case R.id.action_accept_button:
				((MyFragment) fragment).actionAccept();
				break;
			case R.id.action_refresh_button:
				((MyFragment) fragment).actionRefresh();
				break;
			}
		} catch (ClassCastException e) {
			Log.e(Utils.TAG, e.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOpen() {
		View focus;
		if ((focus = getCurrentFocus()) != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			focus.clearFocus();
		}
	}

	@Override
	public void onClose() {
		View focus;
		if ((focus = getCurrentFocus()) != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			focus.clearFocus();
		}
	}

	public void goToLastFrame() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.getBackStackEntryCount() == 0) {
			sm.showMenu();
		} else {
			fragmentManager.popBackStack();
		}
	}

	/**
	 * Changes the fragment shown
	 * 
	 * @param content fragment frame to show
	 */
	public void switchContent(Fragment newFragment) {
		if (newFragment == null)
			return;

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		boolean isSameFragment;
		String currentFragmentClass = fragmentManager.findFragmentById(R.id.content_frame).getClass().toString();
		String newFragmentClass = newFragment.getClass().toString();
		isSameFragment = newFragmentClass.equals(currentFragmentClass);

		if (!isSameFragment) {
			Fragment fragmentToReplace = fragmentManager.findFragmentByTag(newFragmentClass);
			if (fragmentToReplace != null) {
				newFragment = fragmentToReplace;
			}
			fragmentTransaction.replace(R.id.content_frame, newFragment, newFragmentClass);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			
			if (bootableFragments.contains(newFragment.getClass())) {
//				Log.d(Utils.TAG, "class is contained");
				sharedPref.setBootFragmentClass((Class<? extends MyFragment>) newFragment.getClass());
				sharedPref.saveData();
			}
		}
		sm.showContent();

		View focus;
		if ((focus = getCurrentFocus()) != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			focus.clearFocus();
		}

		// // show loading progress bar
		// setSupportProgressBarIndeterminateVisibility(true);
		// setProgressBarIndeterminateVisibility(Boolean.TRUE);
	}

	public void hideActions() {
		setActionAddVisible(false);
		setActionEditVisible(false);
		setActionAcceptVisible(false);
		setActionRefreshVisible(false);
	}

	public void setActionAddVisible(boolean show) {
		if (sherlockMenu != null)
			sherlockMenu.findItem(R.id.action_add_button).setVisible(show);
	}

	public void setActionEditVisible(boolean show) {
		if (sherlockMenu != null)
			sherlockMenu.findItem(R.id.action_edit_button).setVisible(show);
	}

	public void setActionAcceptVisible(boolean show) {
		if (sherlockMenu != null)
			sherlockMenu.findItem(R.id.action_accept_button).setVisible(show);
	}

	public void setActionRefreshVisible(boolean show) {
		if (sherlockMenu != null)
			sherlockMenu.findItem(R.id.action_refresh_button).setVisible(show);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (savedInstanceState != null)
			menuAlreadyToggled = savedInstanceState.getBoolean("menuAlreadyToggled");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!menuAlreadyToggled && showMenuOnStartup) {
					sm.showMenu();
					menuAlreadyToggled = true;
				}
			}
		}, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.actionbar_menu, menu);
		sherlockMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("menuAlreadyToggled", menuAlreadyToggled);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			try {
				MyFragment fragment = (MyFragment) fragmentManager.findFragmentById(R.id.content_frame);
				if (!fragment.goBack())
					return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.w(Utils.TAG, "Cannot cast frame to MyFrame");
			}
			if (fragmentManager.getBackStackEntryCount() == 0) {
				if (sm.isMenuShowing())
					finish();
				else
					sm.showMenu();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
