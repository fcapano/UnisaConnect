package it.fdev.scraper.esse3;

import it.fdev.unisaconnect.data.SharedPrefDataManager;

import java.io.IOException;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;

public abstract class Esse3BasicScraper {
	protected Context mContext;
	protected SharedPrefDataManager mDataManager;
	protected String base64login;
	
	protected String broadcastID;
	
	public static enum LoadStates {
		WRONG_DATA, NO_DATA, NO_INTERNET, UNKNOWN_PROBLEM, ESSE3_PROBLEM, FINISHED
	};
	
	public Esse3BasicScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID){
		this.mContext = context;
		this.mDataManager = dataManager;
		this.base64login = base64login;
		this.broadcastID = broadcastID;
	}
	
	public LoadStates run() {
		LoadStates result = startScraper();
		if (broadcastID != null) {
			Esse3ScraperService.broadcastStatus(mContext, broadcastID, result);
		}
		return result;
	}
	
	protected abstract LoadStates startScraper();
	
	
	public Document scraperGetUrl(String url) throws IOException, InterruptedException, HttpStatusException {
		Response res = Jsoup.connect(url).header("Authorization", "Basic " + base64login).method(Method.GET).timeout(30000).execute();
		Document document = res.parse();
		Thread.sleep(100);
		return document;
	}
	
}
