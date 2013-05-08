package it.fdev.unisaconnect.data;

import it.fdev.encryptionUtils.CryptoMan;
import it.fdev.encryptionUtils.CryptoMan_2;
import it.fdev.encryptionUtils.SimpleCrypto;
import it.fdev.unisaconnect.MainActivity;
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
	public static final String NO_ENCODING 		= "NOENC";
	public static final String PREF_KEY 		= "enc_key";
	public static final String PREF_USER 		= "user";
	public static final String PREF_PASS 		= "pass";
	public static final String PREF_ACCTYPE 	= "tipoAccountIndex";
	public static final String PREF_AUTOLOGIN 	= "loginAutomatica";
	public static final String PREF_IS_NEW_ENCRYPTION = "isNewEncryption";
	public static final String PREF_ENCRYPTION_VERSION = "encryptionVersion";
	public static final String PREF_TESTING_ENABLED = "testingEnabled";
	
	public static final int CRYPTO_VERSION = 2;	// 2=0.5
	
	private static SharedPrefDataManager dm = null;
	private SharedPreferences settings = null;
	private String user, pass;
	private boolean loginAutomatica;
	private int tipoAccountIndex;
	private boolean testingEnabled;
	
	private final static String[] SSID = new String[] {"Studenti","Personale"};
	
	public static SharedPrefDataManager getDataManager(Context context) {
		if(dm == null) {
			dm = new SharedPrefDataManager(context);
		}
		return dm;
	}
	
	private SharedPrefDataManager(Context context) {
		settings = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
		updateBackwardsCompatibility();
		settings.edit().putInt(PREF_ENCRYPTION_VERSION, CRYPTO_VERSION).commit();
		loadData();
	}
	
	public void updateBackwardsCompatibility() {
		
		if(settings.getInt(PREF_ENCRYPTION_VERSION, -1) == CRYPTO_VERSION)
			return;
		
		boolean dataExists = settings.contains(PREF_USER);
		boolean isVersion1 = !settings.contains(PREF_KEY);		//if false -> isversion2
		if(!dataExists)
			return;
		else {
			if (isVersion1) {
				Log.d(Utils.TAG, "Migrating V1 data");
				try {
					user = settings.getString(PREF_USER, null);
					pass = settings.getString(PREF_PASS, null);
					if(user==null || pass==null) {
						removeData();
						return;
					}
					String SEED = "Q8nupuPhAPHuqEyubr5J";
					user = SimpleCrypto.decrypt(SEED, user);
					pass = SimpleCrypto.decrypt(SEED, pass);
					String userCod = CryptoMan_2.encrypt(user);
					String passCod = CryptoMan_2.encrypt(pass);
					Editor editor = settings.edit();
					editor.putString(PREF_USER, userCod);
					editor.putString(PREF_PASS, passCod);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
					removeData();
					return;
				}
			} else { //isversion2
				Log.d(Utils.TAG, "Migrating V2 data");
				try {
					user = settings.getString(PREF_USER, null);
					pass = settings.getString(PREF_PASS, null);
					if(user==null || pass==null) {
						removeData();
						return;
					}
					String raw_key = settings.getString(PREF_KEY, NO_ENCODING);
					if(!raw_key.equals(NO_ENCODING)) {
						user = CryptoMan.decrypt(user, settings);
						pass = CryptoMan.decrypt(pass, settings);
					}
					String userCod = CryptoMan_2.encrypt(user);
					String passCod = CryptoMan_2.encrypt(pass);
					Editor editor = settings.edit();
					editor.putString(PREF_USER, userCod);
					editor.putString(PREF_PASS, passCod);
					editor.commit();
				} catch(Exception e) {
					e.printStackTrace();
					removeData();
				}
			}
		}
	}
	
	public boolean loadData() {
		try {
			String userCod = settings.getString(PREF_USER, null);
			String passCod = settings.getString(PREF_PASS, null);
			if(userCod==null || passCod==null)
				return false;
			user = CryptoMan_2.decrypt(userCod);
			pass = CryptoMan_2.decrypt(passCod);
			tipoAccountIndex = settings.getInt(PREF_ACCTYPE, 0);
			loginAutomatica = settings.getBoolean(PREF_AUTOLOGIN, true);
			testingEnabled = settings.getBoolean(PREF_TESTING_ENABLED, false);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(MainActivity.TAG, "Eccezione decodificando i dati...resetto tutto");
			removeData();
			return false;
		}
	}
	
	public boolean saveData() {
		if(user==null || pass==null)
			return false;
		
		try {
			String userCod = CryptoMan_2.encrypt(user);
			String passCod = CryptoMan_2.encrypt(pass);
			
			Editor editor = settings.edit();
			editor.putString(PREF_USER, userCod);
			editor.putString(PREF_PASS, passCod);
			editor.putBoolean(PREF_AUTOLOGIN, loginAutomatica);
			editor.putInt(PREF_ACCTYPE, tipoAccountIndex);
			editor.putBoolean(PREF_TESTING_ENABLED, testingEnabled);
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
	public boolean dataExists() {
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

}
