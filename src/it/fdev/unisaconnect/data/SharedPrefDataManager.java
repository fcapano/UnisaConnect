package it.fdev.unisaconnect.data;

import it.fdev.encryptionUtils.CryptoMan_2;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.utils.MyFragment;
import it.fdev.utils.ObjectSerializer;
import it.fdev.utils.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Gestisce i dati salvati nelle sharedpreferences
 * Cifra username e password
 * @author francesco
 *
 */
public class SharedPrefDataManager {
	
	public static final String PREFERENCES_KEY 	= "PREFERENCES";
	
	// Settings Pref
	public static final String PREF_BOOTABLE_FRAGMENT = "bootableFragment";
	
	// Login Pref
	public static final String PREF_USER 		= "user";
	public static final String PREF_PASS 		= "pass";
	public static final String PREF_ACCTYPE 	= "tipoAccountIndex";
	public static final String PREF_AUTOLOGIN 	= "loginAutomatica";
	
	// Mensa data
	public static final String PREF_MENU_MENSA = "menu";
	
	// Weather data
	public static final String PREF_WEATHER = "weather";
	
	public static final String PREF_IS_NEW_ENCRYPTION = "isNewEncryption";
	public static final String PREF_ENCRYPTION_VERSION = "encryptionVersion";
	public static final String PREF_TESTING_ENABLED = "testingEnabled";
	
	// Crypto data
	public static final String NO_ENCODING 		= "NOENC";
	public static final String PREF_KEY 		= "enc_key";
	public static final int CRYPTO_VERSION = 3;	// 2=0.5 3=0.6.2
	
	private static SharedPrefDataManager dm = null;
	private SharedPreferences settings = null;
	
	// Settings
	private Class<? extends MyFragment> bootFragmentClass;
	// Login
	private String user, pass;
	private boolean loginAutomatica;
	private int tipoAccountIndex;
	// Mensa
	private MenuMensa menuMensa;
	// Weather
	private WeatherData weather;
	
	private boolean testingEnabled = MainActivity.isDebugMode;
	private final static String[] SSID = new String[] {"Studenti","Personale"};
	
	
	public static SharedPrefDataManager getDataManager(Context context) {
		if(dm == null) {
			dm = new SharedPrefDataManager(context);
		}
		return dm;
	}
	
	private SharedPrefDataManager(Context context) {
		settings = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
		settings.edit().putInt(PREF_ENCRYPTION_VERSION, CRYPTO_VERSION).commit();
		loadData();
	}
	
	public boolean loadData() {
		try {
			String userCod = settings.getString(PREF_USER, null);
			String passCod = settings.getString(PREF_PASS, null);
			if (userCod != null && passCod != null) {
				user = CryptoMan_2.decrypt(userCod);
				pass = CryptoMan_2.decrypt(passCod);
				tipoAccountIndex = settings.getInt(PREF_ACCTYPE, 0);
				loginAutomatica = settings.getBoolean(PREF_AUTOLOGIN, true);
			}
			testingEnabled = settings.getBoolean(PREF_TESTING_ENABLED, MainActivity.isDebugMode);
			menuMensa = (MenuMensa) ObjectSerializer.deserialize(settings.getString(PREF_MENU_MENSA, null));
			weather = (WeatherData) ObjectSerializer.deserialize(settings.getString(PREF_WEATHER, null));
			bootFragmentClass = MainActivity.bootableFragments.get(settings.getInt(PREF_BOOTABLE_FRAGMENT, 0));
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(MainActivity.TAG, "Eccezione decodificando i dati...resetto tutto");
			removeData();
			return false;
		}
	}
	
	public boolean saveData() {
		try {
			Editor editor = settings.edit();
			if (user != null && pass != null) {
				String userCod = CryptoMan_2.encrypt(user);
				String passCod = CryptoMan_2.encrypt(pass);
				editor.putString(PREF_USER, userCod);
				editor.putString(PREF_PASS, passCod);
			}
			editor.putBoolean(PREF_AUTOLOGIN, loginAutomatica);
			editor.putInt(PREF_ACCTYPE, tipoAccountIndex);
			editor.putBoolean(PREF_TESTING_ENABLED, testingEnabled);
			editor.putString(PREF_MENU_MENSA, ObjectSerializer.serialize(menuMensa));
			editor.putString(PREF_WEATHER, ObjectSerializer.serialize(weather));
			editor.putInt(PREF_BOOTABLE_FRAGMENT, Math.max(MainActivity.bootableFragments.indexOf(bootFragmentClass), 0));
			editor.commit();
			return true;
		} catch(Exception e) {
			Log.e(MainActivity.TAG, "Eccezione codificando i dati...resetto tutto");
			e.printStackTrace();
			removeData();
			return false;
		}
	}
	
	//Controlla che siano stati precedentemente salvati i dati di login
	public boolean loginDataExists() {
		return (settings.contains(PREF_USER) && user!=null &&
				settings.contains(PREF_PASS) && pass!=null &&
				settings.contains(PREF_ACCTYPE) && tipoAccountIndex>=0 && tipoAccountIndex<SSID.length &&
				settings.contains(PREF_AUTOLOGIN));
	}
	
	private void removeData() {
		Log.d(Utils.TAG, "DataManager vacuum preferences");
		Editor editor = settings.edit();
		editor.clear().commit();
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		user = (user.contains("@") ?  user.substring(0,user.lastIndexOf("@")) : user); //elimino la @ e seguito
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public boolean isLoginAutomatica() {
		return loginAutomatica;
	}

	public void setLoginAutomatica(boolean loginAutomatica) {
		this.loginAutomatica = loginAutomatica;
	}

	public int getTipoAccountIndex() {
		return tipoAccountIndex;
	}
	
	public void setTipoAccountIndex(int tipoAccountIndex) {
		this.tipoAccountIndex = tipoAccountIndex;
	}
	
	public String getTipoAccount() {
		return SSID[tipoAccountIndex];
	}
	
	public void setTestingEnabled(boolean enabled) {
		this.testingEnabled = enabled;
	}
	
	public boolean isTestingingEnabled() {
		return this.testingEnabled;
	}
	
	public void setMenuMensa(MenuMensa menuMensa) {
		this.menuMensa = menuMensa;
	}
	
	public MenuMensa getMenuMensa() {
		return menuMensa;
	}
	
	public void setWeather(WeatherData weather) {
		this.weather= weather;
	}
	
	public WeatherData getWeather() {
		return weather;
	}

	public Class<? extends MyFragment> getBootFragmentClass() {
		return bootFragmentClass;
	}

	public void setBootFragmentClass(Class<? extends MyFragment> bootFragmentClass) {
		this.bootFragmentClass = bootFragmentClass;
	}

}
