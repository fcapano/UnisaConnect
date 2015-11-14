package it.fdev.scraper.esse3;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.Utils;

public class Esse3TipoCorsoScraper extends Esse3BasicScraper {
	private final String startUrl = "https://esse3web.unisa.it/unisa/auth/Logon.do";

	public Esse3TipoCorsoScraper(Context context, SharedPrefDataManager dataManager, String base64login, String broadcastID) {
		super(context, dataManager, base64login, broadcastID);
	}

	@Override
	public LoadStates startScraper() {
		try {
			ArrayList<TipoCorso> tipoCorsi = scraperStepTipoCorso();
			if (tipoCorsi != null) {
				broadcastStatus(mContext, broadcastID, tipoCorsi);
				return LoadStates.FINISHED;
			} else
				return LoadStates.NO_DATA;
		} catch (HttpStatusException e) {
			Log.w(Utils.TAG, "ERROR ", e);
			int code = e.getStatusCode();
			if (code == 401)
				return LoadStates.WRONG_DATA;
			else
				return LoadStates.UNKNOWN_PROBLEM;
		} catch (Exception e) {
			Log.w(Utils.TAG, "ERROR ", e);
			return LoadStates.UNKNOWN_PROBLEM;
		}
	}
	
	public static void broadcastStatus(Context ctx, String action, ArrayList<TipoCorso> list) {
		Intent localIntent = new Intent(action);
		localIntent.putExtra("status", LoadStates.FINISHED);
		localIntent.putParcelableArrayListExtra("list", list);
		ctx.sendBroadcast(localIntent);
	}

	private ArrayList<TipoCorso> scraperStepTipoCorso() throws HttpStatusException, IOException, InterruptedException {
		ArrayList<TipoCorso> listaTipoCorso = new ArrayList<TipoCorso>();
		
		Document document = scraperGetUrl(startUrl);
		if (document == null) {
			return null;
		}

		Elements detailsTables = document.getElementsByClass("detail_table");
		if (detailsTables.size() > 0) {
			Elements rows = detailsTables.get(0).getElementsByTag("tr");
			rows.remove(0); //Remove headers
			for (Element cRow : rows) {
				Elements cols = cRow.getElementsByTag("td");
				if (cols.size() != 4) {
					Log.d(Utils.TAG, "Unknown cols number checking tipoCorso: #" + cols.size());
					continue;
				}
				String matricola 		= cols.get(0).text();
				String tipoCorso 		= cols.get(1).text();
				String corsoDiStudio 	= cols.get(2).text();
				String stato 			= cols.get(3).text();
				listaTipoCorso.add(new TipoCorso(matricola, tipoCorso, corsoDiStudio, stato));
			}
		}
		return listaTipoCorso;
	}

	public static class TipoCorso implements Parcelable {
		private String matricola;
		private String tipoCorso;
		private String corsoDiStudio;
		private String stato;

		public TipoCorso(String matricola, String tipoCorso, String corsoDiStudio, String stato) {
			this.matricola = matricola;
			this.tipoCorso = tipoCorso;
			this.corsoDiStudio = corsoDiStudio;
			this.stato = stato;
		}

		public String getMatricola() {
			return matricola;
		}

		public String getTipoCorso() {
			return tipoCorso;
		}

		public String getCorsoDiStudio() {
			return corsoDiStudio;
		}

		public String getStato() {
			return stato;
		}
		
		public TipoCorso(Parcel in) {
			readFromParcel(in);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(matricola);
			dest.writeString(tipoCorso);
			dest.writeString(corsoDiStudio);
			dest.writeString(stato);
		}

		private void readFromParcel(Parcel in) {
			matricola = in.readString();
			tipoCorso = in.readString();
			corsoDiStudio = in.readString();
			stato = in.readString();
		}
		
		public static final Parcelable.Creator<TipoCorso> CREATOR = new Parcelable.Creator<TipoCorso>() {
			public TipoCorso createFromParcel(Parcel in) {
				return new TipoCorso(in);
			}

			public TipoCorso[] newArray(int size) {
				return new TipoCorso[size];
			}
		};

		@Override
		public int describeContents() {
			return 0;
		}
	}

}
