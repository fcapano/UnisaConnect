package it.fdev.unisaconnect;

import it.fdev.utils.MyFragment;
import it.fdev.utils.Utils;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements OnCloseListener, OnOpenListener {

	public static final String TAG = Utils.TAG;
	private boolean menuAlreadyToggled = false; // After the first time in onPostCreate the menu shouldn't be toggled automatically

	protected SlidingMenuFragment menuFragment;
	private Menu sherlockMenu;
	private SlidingMenu sm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// set the Above View
		setContentView(R.layout.content_frame);

		// Initialize Crittercism
		Crittercism.init(getApplicationContext(), "5135ccc2558d6a05f7000024");
		Crittercism.setOptOutStatus(true);

		// customize the SlidingMenu
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidthRes(R.dimen.slidingmenu_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setOnCloseListener(this);
		sm.setOnOpenListener(this);

		// Initialize image downloader used by fragments
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).memoryCacheExtraOptions(100, 100).build();
		ImageLoader.getInstance().init(config);

		// Initialize views
		menuFragment = new SlidingMenuFragment();
		Fragment startFragment = new MapFragment(); //new WifiPreferencesFragment();
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, menuFragment).replace(R.id.content_frame, startFragment, startFragment.getClass().toString()).commit();

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
	 * @param content
	 *            fragment frame to show
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
				if (!menuAlreadyToggled) {
//					toggle();
//					sm.showMenu();
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
