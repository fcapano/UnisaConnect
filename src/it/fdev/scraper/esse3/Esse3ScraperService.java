package it.fdev.scraper.esse3;

import it.fdev.scraper.esse3.Esse3BasicScraper.LoadStates;
import it.fdev.unisaconnect.MainActivity;
import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

import java.net.CookieHandler;
import java.net.CookieManager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

public class Esse3ScraperService extends IntentService {

	public final static String BROADCAST_STATE_E3_TIPO_CORSO = "it.fdev.esse3.tipo_corso";
	public final static String BROADCAST_STATE_E3_LIBRETTO = "it.fdev.esse3.status_libretto";
	public final static String BROADCAST_STATE_E3_APPELLI = "it.fdev.esse3.status_appelli";
	public final static String BROADCAST_STATE_E3_PAGAMENTI = "it.fdev.esse3.status_pagamenti";

	public static boolean isRunning = false;

	private SharedPrefDataManager dataManager;
	private String base64login;
	private Context mContext;

	public Esse3ScraperService() {
		super("it.fdev.esse3.scraper_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			isRunning = true;
			mContext = getApplicationContext();

			dataManager = new SharedPrefDataManager(mContext);
			if (!dataManager.loginDataExists()) {
				broadcastStatus(mContext, MainActivity.BROADCAST_ERROR, LoadStates.NO_DATA);
				isRunning = false;
				stopForeground(true);
				stopSelf();
				return;
			}

			base64login = Base64.encodeToString((dataManager.getUser() + ":" + dataManager.getPass()).getBytes(), Base64.NO_WRAP);

			CookieManager cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);

			// 1. Autenticazione
			sendLoadingMessage(mContext, R.string.autenticazione_esse3);
			
			String action = intent.getAction();
			boolean chooseCareer = !BROADCAST_STATE_E3_TIPO_CORSO.equals(action);

			LoadStates loginStatus = new Esse3Login(mContext, dataManager, base64login, null, chooseCareer).run();
			switch (loginStatus) {
			case FINISHED:
				break;
			default:
				Log.d(Utils.TAG, "Checking esse3 msg...");
				Esse3CheckErrorMessage errorChecker = new Esse3CheckErrorMessage(mContext, dataManager, base64login, null);
				LoadStates errorMsgStatus = errorChecker.run();
				String errorMsg = errorChecker.getErrorMessage();
				if (errorMsgStatus == LoadStates.ESSE3_PROBLEM && errorMsg != null && !errorMsg.isEmpty()) {
					Log.d(Utils.TAG, "msg found");
					broadcastStatus(mContext, MainActivity.BROADCAST_ERROR, errorMsgStatus, errorMsg);
				} else {
					broadcastStatus(mContext, MainActivity.BROADCAST_ERROR, loginStatus);
				}
				isRunning = false;
				stopForeground(true);
				stopSelf();
				return;
			}

			// 2. Recupero i dati
			sendLoadingMessage(mContext, R.string.sincronizzazione_esse3);

			if (BROADCAST_STATE_E3_TIPO_CORSO.equals(action)) {
				Log.d(Utils.TAG, "Only scrape Tipo Corsi");
				scrapeTipoCorsi();
			} else {
				
				if (BROADCAST_STATE_E3_LIBRETTO.equals(action)) {
					Log.d(Utils.TAG, "Start by Libretto");
					scrapeLibretto();
					scrapeAppelli();
					scrapePagamenti();
				} else if (BROADCAST_STATE_E3_APPELLI.equals(action)) {
					Log.d(Utils.TAG, "Start by Appelli");
					scrapeAppelli();
					scrapeLibretto();
					scrapePagamenti();
				} else {
					Log.d(Utils.TAG, "Start by Pagamenti");
					scrapePagamenti();
					scrapeLibretto();
					scrapeAppelli();
				}
				
			}

		} catch (Exception e) {
			Log.e(Utils.TAG, "Esse3 service crashed", e);
		}
		isRunning = false;
		stopForeground(true);
		stopSelf();
	}

	private void scrapeTipoCorsi() {
		new Esse3TipoCorsoScraper(getApplicationContext(), dataManager, base64login, BROADCAST_STATE_E3_TIPO_CORSO).run();
		return;
	}

	private void scrapeLibretto() {
		new Esse3LibrettoScraper(getApplicationContext(), dataManager, base64login, BROADCAST_STATE_E3_LIBRETTO).run();
		return;
	}

	private void scrapeAppelli() {
		new Esse3AppelliScraper(getApplicationContext(), dataManager, base64login, BROADCAST_STATE_E3_APPELLI).run();
		return;
	}

	private void scrapePagamenti() {
		new Esse3PagamentiScraper(getApplicationContext(), dataManager, base64login, BROADCAST_STATE_E3_PAGAMENTI).run();
		return;
	}

	public static void broadcastStatus(Context ctx, String action, LoadStates state) {
		broadcastStatus(ctx, action, state, null);
	}

	public static void broadcastStatus(Context ctx, String action, LoadStates state, String message) {
		Intent localIntent = new Intent(action);
		localIntent.putExtra("status", state);
		if (message != null) {
			localIntent.putExtra("message", message);
		}
		ctx.sendBroadcast(localIntent);
	}

	public static void sendLoadingMessage(Context ctx, int messageRes) {
		Intent localIntent = new Intent(MainActivity.BROADCAST_LOADING_MESSAGE);
		localIntent.putExtra("message_res", messageRes);
		ctx.sendBroadcast(localIntent);
	}

}
