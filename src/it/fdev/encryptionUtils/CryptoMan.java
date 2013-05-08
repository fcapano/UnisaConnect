package it.fdev.encryptionUtils;


import it.fdev.unisaconnect.data.SharedPrefDataManager;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.SharedPreferences;

@Deprecated
public class CryptoMan {
	
	private static final String ENCODER = "AES";
	
	public static String encrypt(String cleartext, SharedPreferences preferences) throws Exception {
        byte[] rawKey = getRawKey(preferences);
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }
	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, ENCODER);
        Cipher cipher = Cipher.getInstance(ENCODER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
	
	public static String decrypt(String encrypted, SharedPreferences preferences)	throws Exception {
		byte[] rawKey = getRawKey(preferences);
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}
	
	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, ENCODER);
        Cipher cipher = Cipher.getInstance(ENCODER);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
	
	protected static byte[] getRawKey(SharedPreferences preferences) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCODER);
		SecureRandom secureRandom = new SecureRandom();
		
		String seed = preferences.getString(SharedPrefDataManager.PREF_KEY, null);
		
		if(seed!=null && seed.contains(SharedPrefDataManager.NO_ENCODING))
			return null;

		byte[] seedBytes;
		if (seed == null) {
			seedBytes = secureRandom.generateSeed(128);
			seed = toHex(seedBytes);
			preferences.edit().putString(SharedPrefDataManager.PREF_KEY, seed).commit();
		} else {
			seedBytes = toByte(seed);
		}
		
		secureRandom.setSeed(seedBytes);
		keyGenerator.init(128, secureRandom); // 192 and 256 bits may not be available
		SecretKey skey = keyGenerator.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}
	
	public static boolean generateKey(SharedPreferences preferences) {
		try {
			getRawKey(preferences);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
//	private static String toHex(String txt) {
//        return toHex(txt.getBytes());
//    }
//	
//	private static String fromHex(String hex) {
//        return new String(toByte(hex));
//    }

	public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

	public static String toHex(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}
