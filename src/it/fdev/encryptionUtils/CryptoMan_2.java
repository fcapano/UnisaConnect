package it.fdev.encryptionUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.util.Base64;

public class CryptoMan_2 {

	public static final String PKCS12_DERIVATION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	private static String DELIMITER = "]";

	private static int KEY_LENGTH = 256;
	// minimum values recommended by PKCS#5, increase as necessary
	private static int ITERATION_COUNT = 1000;
	private static final int PKCS5_SALT_LENGTH = 8;

	private static SecureRandom random = new SecureRandom();

	private static final String PASS = "0qTK0VWXvojSQdX";

	private static SecretKey key;

	private static SecretKey deriveKey(String password, byte[] salt) {
		return CryptoMan_2.deriveKeyPkcs12(salt, password);
	}

	private static String encrypt(String plaintext, String password) {
		byte[] salt = CryptoMan_2.generateSalt();
		key = deriveKey(password, salt);
		// Log.d(TAG, "Generated key: " + getRawKey());

		return CryptoMan_2.encryptPkcs12(plaintext, key, salt);
	}

	public static String encrypt(String plaintext) {
		return encrypt(plaintext, PASS);
	}

	private static String decrypt(String ciphertext, String password) {
		return CryptoMan_2.decryptPkcs12(ciphertext, password);
	}

	public static String decrypt(String ciphertext) {
		return decrypt(ciphertext, PASS);
	}

//	private static String getRawKey() {
//		if (key == null) {
//			return null;
//		}
//		return CryptoMan_2.toHex(key.getEncoded());
//	}

	private static SecretKey deriveKeyPkcs12(byte[] salt, String password) {
		try {
//			long start = System.currentTimeMillis();
			KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PKCS12_DERIVATION_ALGORITHM);
			SecretKey result = keyFactory.generateSecret(keySpec);
//			long elapsed = System.currentTimeMillis() - start;
//			Log.d(TAG, String.format("PKCS#12 key derivation took %d [ms].", elapsed));
			return result;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] generateSalt() {
		byte[] b = new byte[PKCS5_SALT_LENGTH];
		random.nextBytes(b);

		return b;
	}

	private static String encryptPkcs12(String plaintext, SecretKey key, byte[] salt) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
			cipher.init(Cipher.ENCRYPT_MODE, key, pbeSpec);
//			Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

			return String.format("%s%s%s", toBase64(salt), DELIMITER, toBase64(cipherText));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

//	private static String toHex(byte[] bytes) {
//		StringBuffer buff = new StringBuffer();
//		for (byte b : bytes) {
//			buff.append(String.format("%02X", b));
//		}
//
//		return buff.toString();
//	}

	private static String toBase64(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	private static byte[] fromBase64(String base64) {
		return Base64.decode(base64, Base64.NO_WRAP);
	}

	private static String decryptPkcs12(byte[] cipherBytes, SecretKey key, byte[] salt) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
			cipher.init(Cipher.DECRYPT_MODE, key, pbeSpec);
//			Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] plainBytes = cipher.doFinal(cipherBytes);
			String plainrStr = new String(plainBytes, "UTF-8");

			return plainrStr;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String decryptPkcs12(String ciphertext, String password) {
		String[] fields = ciphertext.split(DELIMITER);
		if (fields.length != 2) {
			throw new IllegalArgumentException("Invalid encypted text format");
		}

		byte[] salt = fromBase64(fields[0]);
		byte[] cipherBytes = fromBase64(fields[1]);
		SecretKey key = deriveKeyPkcs12(salt, password);

		return decryptPkcs12(cipherBytes, key, salt);
	}
}