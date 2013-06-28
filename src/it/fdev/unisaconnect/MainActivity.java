package it.fdev.unisaconnect;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.MyFragment;
import it.fdev.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.actionbarsherlock.app.ActionBar;
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
	public static final String APP_TITLE = "";
	public static boolean isTestingAPK = false;
	
	// Fragments which can be started at application startup (do not require special configuration to start)
	public static enum BootableFragmentsEnum {
		WIFI_PREF, MENSA, WEBMAIL, ESSE3_SERVICES, STAFF_SEARCH, TIMETABLE, MAP, WEATHER, LIBRETTO, ESSE3_WEB, APPELLI, PRESENZE, PREFERENCES
	}

	public static HashMap<BootableFragmentsEnum, Class<? extends MyFragment>> fragmentsIDs = 
			new HashMap<MainActivity.BootableFragmentsEnum, Class<? extends MyFragment>>();
	static {
		fragmentsIDs.put(BootableFragmentsEnum.WIFI_PREF, WifiPreferencesFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.MENSA, MensaFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.WEBMAIL, WebmailFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.ESSE3_SERVICES, Esse3ServicesFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.STAFF_SEARCH, StaffSearchFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.TIMETABLE, TimetableFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.MAP, MapFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.WEATHER, WeatherFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.LIBRETTO, LibrettoFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.ESSE3_WEB, Esse3WebFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.APPELLI, AppelliFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.PRESENZE, PresenzeFragment.class);
		fragmentsIDs.put(BootableFragmentsEnum.PREFERENCES, PreferencesFragment.class);
	}
	
	public static final HashSet<Integer> actions = new HashSet<Integer>();
	static {
		actions.add(R.id.action_accept_button);
		actions.add(R.id.action_edit_button);
		actions.add(R.id.action_add_button);
		actions.add(R.id.action_refresh_button);
	}
	
	private static Set<Integer> actionsToShow = new HashSet<Integer>();
	
	private boolean menuAlreadyToggled = false; // After the first time in onPostCreate the menu shouldn't be toggled automatically
	private boolean showMenuOnStartup = true;	// Show the sliding menu on startup
	private SlidingMenuFragment menuFragment;
	private Menu sherlockMenu;
	private SlidingMenu sm;
	private View loadingAnimationView;
	private SharedPrefDataManager sharedPref;
	private List<WeakReference<Fragment>> mFragments = new ArrayList<WeakReference<Fragment>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {	// The catch blocks exceptions before initialization of crittercism
				// isTestingAPK is true only if the app wasn't signed with a production key
			isTestingAPK = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
			if (isTestingAPK) {
				Log.d(Utils.TAG, "The applications is running as debuggable!");
			}
		} catch (Exception e) {
			isTestingAPK = false;
		}
		
		// Initialize Crittercism
		Crittercism.init(getApplicationContext(), "5135ccc2558d6a05f7000024");
		Crittercism.setOptOutStatus(isTestingAPK);

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
		
		// set the Above View
		setContentView(R.layout.content_frame);
		loadingAnimationView = findViewById(R.id.content_loading_animation);
		
		// customize the actionbar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// customize the SlidingMenu
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidthRes(R.dimen.slidingmenu_width);
		sm.setOnCloseListener(this);
		sm.setOnOpenListener(this);
        setSlidingActionBarEnabled(false);

		// Initialize image downloader used by fragments
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
												.defaultDisplayImageOptions(defaultOptions)
												.memoryCacheExtraOptions(100, 100)
												.build();
		ImageLoader.getInstance().init(config);

		 // show loading progress bar
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//		 setSupportProgressBarIndeterminateVisibility(true);
//		 setProgressBarIndeterminateVisibility(Boolean.FALSE);
		
		// Initialize views
		menuFragment = new SlidingMenuFragment();
		
		// set the Behind View
		setBehindContentView(R.layout.sliding_menu_container);
		getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.menu_frame, menuFragment)
					.commit();
		switchContent(BootableFragmentsEnum.WIFI_PREF, true);

	}
		
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			MyFragment fragment = (MyFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
			switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
			case R.id.action_add_button:
				fragment.actionAdd();
				break;
			case R.id.action_edit_button:
				fragment.actionEdit();
				break;
			case R.id.action_accept_button:
				fragment.actionAccept();
				break;
			case R.id.action_refresh_button:
				fragment.actionRefresh();
				return true;
			}
		} catch (ClassCastException e) {
			Log.e(Utils.TAG, e.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOpen() {
		clearKeyboardFocus();
	}

	@Override
	public void onClose() {
		clearKeyboardFocus();
	}

	public void goToLastFrame() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.getBackStackEntryCount() == 1) {
			sm.showMenu();
		} else {
			fragmentManager.popBackStack();
		}
	}
	
	/**
	 * http://stackoverflow.com/questions/9984089/memory-issues-fragments
	 * Memory management
	 */
	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment instanceof SlidingMenuFragment) {
			return;
		}
	    mFragments.add(new WeakReference<Fragment>(fragment));
//	    reloadActionButtons(fragment);
	}

	private void recycleFragments() {
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    for (WeakReference<Fragment> ref : mFragments) {
	        Fragment fragment = ref.get();
	        if (fragment != null) {
	            ft.remove(fragment);
	        }
	    }
	    ft.commit();
	    mFragments.clear();
	}
	
	/**
	 * Changes the fragment shown. Using Enum to avoid creation of duplicate fragments which would be deferenced 
	 * if similar fragment already exists as switchsContent(Fragment newFragment) does
	 * @param content fragment enum to show
	 * @param emptyBackStack if true all existing fragments are removed from the backstack
	 */
	public void switchContent(BootableFragmentsEnum newFragmentEnum, boolean emptyBackStack) {
		if (newFragmentEnum == null)
			return;
		if (!fragmentsIDs.containsKey(newFragmentEnum)) {
			return;
		}
		try {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			Fragment currFrag = fragmentManager.findFragmentById(R.id.content_frame);
			Class<? extends Fragment> currClass = null;
			if (currFrag != null) {
				currClass = fragmentManager.findFragmentById(R.id.content_frame).getClass();						// Classe del fragment attualmente visualizzato
			}
			Class<? extends MyFragment> newClass = fragmentsIDs.get(newFragmentEnum);								// Classe del fragment da visualizzare
			if (currFrag==null || !newClass.equals(currClass)) {													// I fragment sono diversi
				Fragment fragmentToShow = (Fragment) newClass.newInstance();
				if (emptyBackStack) {
					fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
					recycleFragments();
				}
				fragmentTransaction.replace(R.id.content_frame, fragmentToShow, fragmentToShow.getClass().toString());
//				reloadActionButtons(fragmentToShow);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				
				sharedPref.setBootFragmentClass(newClass);
				sharedPref.saveData();
			}
			sm.showContent();
		} catch (Exception e) {
			e.printStackTrace();
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
//			reloadActionButtons(newFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
		sm.showContent();

		clearKeyboardFocus();
	}

	/**
	 * Versioni recenti di Android hanno un bug sull'actionbar
	 * una volta impostata come nascosta un action, il menu dev'essere invalidado
	 * affinchè venga mostrata nuovamente 
	 */
	public void reloadActionButtons(Fragment newFragment) {
		if (sherlockMenu != null && newFragment != null) {
			Log.d(Utils.TAG, "Reload act of: " + newFragment.getClass().toString());
//			invalidateOptionsMenu();
			try {
				actionsToShow = ((MyFragment) newFragment).getActionsToShow();
				if (actionsToShow == null) {
					actionsToShow = new HashSet<Integer>();
				}
				invalidateOptionsMenu();
			} catch (Exception e) {
				Log.e(Utils.TAG, "Error in reloadActionButtons", e);
			}
		}
	}
	
	public void setLoadingVisible(final boolean showActionbarAnimation) {
		if (sherlockMenu != null) {
			if (showActionbarAnimation) {
				actionsToShow.add(R.id.action_loading_animation);
			} else {
				actionsToShow.remove(R.id.action_loading_animation);
			}
//			showActionBarLoadingAnimation = showActionbarAnimation;
			invalidateOptionsMenu();
//			MenuItem refrButton = sherlockMenu.findItem(R.id.action_refresh_button);
//			if (showActionbarAnimation) {
////				if(!refrButton.isVisible())
////					refrButton.setVisible(true);
//				refrButton.setActionView(R.layout.refresh_action_view);		
//			} else {
//				refrButton.setActionView(null);
//			}
		}
	}
	
	public void setLoadingVisible(final boolean showActionbarAnimation, boolean showContentFrameAnimation) {
		setLoadingVisible(showActionbarAnimation);
		if (showContentFrameAnimation) {
			loadingAnimationView.setVisibility(View.VISIBLE);
		} else {
			loadingAnimationView.setVisibility(View.GONE);
		}
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (actionsToShow == null) {
			actionsToShow = new HashSet<Integer>();
		}
		if (actionsToShow.contains(R.id.action_loading_animation)) {
			actionsToShow.add(R.id.action_refresh_button);
		}
		for (Integer cItem : actions) {
			menu.findItem(cItem).setVisible(actionsToShow.contains(cItem));
		}
//		setLoadingVisible(actionsToShow.contains(R.id.action_loading_animation));
		
		MenuItem refrButton = sherlockMenu.findItem(R.id.action_refresh_button);
		if (actionsToShow.contains(R.id.action_loading_animation)) {
			refrButton.setActionView(R.layout.refresh_action_view);		
		}
		
		
		return true;
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
			if (fragmentManager.getBackStackEntryCount() == 1) {
				if (sm.isMenuShowing())
					finish();
				else
					sm.showMenu();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void clearKeyboardFocus() {
		try {
			View focus = getCurrentFocus();
			if (focus != null) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
				focus.clearFocus();
			}
		} catch (Exception e) {
			// Ho avuto nullpointerexception su focus.clearFocus();
			// Non è un'eccezione importante
		}
	}

}
