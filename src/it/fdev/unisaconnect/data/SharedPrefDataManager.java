package it.fdev.unisaconnect.data;

import it.fdev.encryptionUtils.CryptoMan_2;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.utils.MyFragment;
import it.fdev.utils.ObjectSerializer;
import it.fdev.utils.Utils;

import java.util.Date;

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
	
	private static SharedPrefDataManager dm = null;
	private SharedPreferences settings = null;
	private final static String[] SSID = new String[] {"Studenti","Personale"};
	
	public static final String PREFERENCES_KEY 	= "PREFERENCES";
	
	// Boot Fragment
	public static final String PREF_BOOTABLE_FRAGMENT = "bootableFragment";
	private Class<? extends MyFragment> bootFragmentClass;
	
	// Login
	public static final String PREF_USER 		= "user";
	public static final String PREF_PASS 		= "pass";
	public static final String PREF_ACCTYPE 	= "tipoAccountIndex";
	public static final String PREF_AUTOLOGIN 	= "loginAutomatica";
	private String user, pass;
	private boolean loginAutomatica;
	private int tipoAccountIndex;
	
	// Mensa
	public static final String PREF_MENU_MENSA = "menu";
	private MenuMensa menuMensa;
	
	// Weather
	public static final String PREF_WEATHER = "weather";
	private WeatherData weather;
	
	// Presenze
	public static final String PREF_PRESENZE = "presenze";
	private Presenze presenze;
	
	// Presenze
	public static final String PREF_APPELLI = "appelli";
	private Appelli appelli;
	
	// Libretto
	public static final String PREF_LIBRETTO_DATE = "libretto_date";
	private Date librettoFetchDate;
	
	// Testing
	public static final String PREF_TESTING_ENABLED = "testingEnabled";
	private boolean testingEnabled = MainActivity.isTestingAPK;
	
	// Crypto
	public static final String PREF_IS_NEW_ENCRYPTION = "isNewEncryption";
	public static final String PREF_ENCRYPTION_VERSION = "encryptionVersion";
	public static final String NO_ENCODING 		= "NOENC";
	public static final String PREF_KEY 		= "enc_key";
	public static final int CRYPTO_VERSION = 3;	// 2=0.5 3=0.6.2
	
	
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
			testingEnabled = settings.getBoolean(PREF_TESTING_ENABLED, MainActivity.isTestingAPK);
			menuMensa = (MenuMensa) ObjectSerializer.deserialize(settings.getString(PREF_MENU_MENSA, null));
			weather = (WeatherData) ObjectSerializer.deserialize(settings.getString(PREF_WEATHER, null));
			presenze = (Presenze) ObjectSerializer.deserialize(settings.getString(PREF_PRESENZE, null));
			appelli = (Appelli) ObjectSerializer.deserialize(settings.getString(PREF_APPELLI, null));
			librettoFetchDate = new Date(settings.getLong(PREF_LIBRETTO_DATE, 0));
			testingEnabled = settings.getBoolean(PREF_TESTING_ENABLED, MainActivity.isTestingAPK);
			bootFragmentClass = MainActivity.fragmentsIDs.get(settings.getInt(PREF_BOOTABLE_FRAGMENT, 0));
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(MainActivity.TAG, "Eccezione decodificando i dati...resetto tutto", e);
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
			editor.putString(PREF_PRESENZE, ObjectSerializer.serialize(presenze));
			editor.putString(PREF_APPELLI, ObjectSerializer.serialize(appelli));
			editor.putLong(PREF_LIBRETTO_DATE, librettoFetchDate.getTime());
//			editor.putInt(PREF_BOOTABLE_FRAGMENT, Math.max(MainActivity.BootableFragmentsEnum.indexOf(bootFragmentClass), 0));
			editor.commit();
			return true;
		} catch(Exception e) {
			Log.e(MainActivity.TAG, "Eccezione codificando i dati...resetto tutto", e);
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
	
	public String getTipoAccount() {
		return SSID[tipoAccountIndex];
	}
	
	public void setTipoAccountIndex(int tipoAccountIndex) {
		this.tipoAccountIndex = tipoAccountIndex;
	}
	
	public boolean isTestingingEnabled() {
		return this.testingEnabled;
	}
	
	public void setTestingEnabled(boolean enabled) {
		this.testingEnabled = enabled;
	}
	
	public MenuMensa getMenuMensa() {
		return menuMensa;
	}
	
	public void setMenuMensa(MenuMensa menuMensa) {
		this.menuMensa = menuMensa;
	}
	
	public WeatherData getWeather() {
		return weather;
	}
	
	public void setWeather(WeatherData weather) {
		this.weather = weather;
	}
	
	public Presenze getPresenze() {
		return presenze;
	}
	
	public void setPresenze(Presenze presenze) {
		this.presenze = presenze;
	}
	
	public Date getLibrettoFetchDate() {
		return librettoFetchDate;
	}
	
	public void setLibrettoFetchDate(Date librettoFetchDate) {
		this.librettoFetchDate = librettoFetchDate;
	}
	
	public Appelli getAppelli() {
		return appelli;
	}
	
	public void setAppelli(Appelli appelli) {
		this.appelli = appelli;
	}

	public Class<? extends MyFragment> getBootFragmentClass() {
		return bootFragmentClass;
	}

	public void setBootFragmentClass(Class<? extends MyFragment> bootFragmentClass) {
		this.bootFragmentClass = bootFragmentClass;
	}

}
