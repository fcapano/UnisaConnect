package it.fdev.unisaconnect.data;

import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusCorsa;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusFermata;
import it.fdev.unisaconnect.model.bustrattaendpoint.model.BusTratta;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class BusDB extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "bus.db";
	private static final int DATABASE_VERSION = 9;

	private final String TABLE_TRATTE_NAME = "tratta";
	private final String TABLE_CORSE_NAME = "corse";
	private final String TABLE_FERMATE_NAME = "fermate";

	public BusDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		setForcedUpgrade();
	}

	public ArrayList<String> getStazioniConTesto(String nomeStazione) {
		ArrayList<String> nomiStazioni = new ArrayList<String>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(true, TABLE_FERMATE_NAME, new String[] { "stazione" }, "stazione LIKE ?", new String[] { "%" + nomeStazione + "%" }, null, null, "stazione DESC", null);
		cursor.moveToFirst();

		int nomeIndex = cursor.getColumnIndex("stazione");
		while (!cursor.isAfterLast()) {
			String nome = cursor.getString(nomeIndex);
			nomiStazioni.add(nome);
			cursor.moveToNext();
		}
		return nomiStazioni;
	}

	private ArrayList<Integer> getIDTrattePerStazione(String stazione) {
		ArrayList<Integer> idTratte = new ArrayList<Integer>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(true, TABLE_FERMATE_NAME, new String[] { "id_tratta" }, "stazione LIKE ?", new String[] { "%" + stazione + "%" }, null, null, null, null);
		cursor.moveToFirst();
		int idTrattaIndex = cursor.getColumnIndex("id_tratta");
		while (!cursor.isAfterLast()) {
			int idTratta = cursor.getInt(idTrattaIndex);
			idTratte.add(idTratta);
			cursor.moveToNext();
		}
		return idTratte;
	}

	private BusTratta getTratta(int idTrattaDaCercare) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_TRATTE_NAME, null, "id = ?", new String[] { Integer.toString(idTrattaDaCercare) }, null, null, null, null);
		if (!cursor.moveToFirst()) {
			return null;
		}

		int idTrattaIndex = cursor.getColumnIndex("id");
		int compagniaIndex = cursor.getColumnIndex("compagnia");
		int capolineaIndex = cursor.getColumnIndex("capolinea");

		int idTratta = cursor.getInt(idTrattaIndex);
		String compagnia = cursor.getString(compagniaIndex);
		String capolinea = cursor.getString(capolineaIndex);

		ArrayList<BusCorsa> cCorse = getCorsePerTratta(idTrattaDaCercare);
		ArrayList<String> stazioniVersoUni = getStazioniTratta(idTrattaDaCercare, true);
		ArrayList<String> stazioniDaUni = getStazioniTratta(idTrattaDaCercare, false);
		
//		BusTratta cTratta = new BusTratta(idTratta, compagnia, capolinea, cCorse, stazioniVersoUni, stazioniDaUni);
//		return cTratta;
		return null;
	}

	public ArrayList<BusTratta> getTrattePerStazione(String stazione) {
		ArrayList<BusTratta> tratte = new ArrayList<BusTratta>();
		ArrayList<Integer> idTratte = getIDTrattePerStazione(stazione);
		for (Integer cIdTratta : idTratte) {
			BusTratta cTratta = getTratta(cIdTratta);
			if (cTratta != null) {
				tratte.add(cTratta);
			}
		}
		return tratte;
	}

	private ArrayList<BusCorsa> getCorsePerTratta(int idTrattaDaCercare) {
		ArrayList<BusCorsa> corse = new ArrayList<BusCorsa>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(true, TABLE_CORSE_NAME, null, "id_tratta LIKE ?", new String[] { Integer.toString(idTrattaDaCercare) }, null, null, null, null);
		cursor.moveToFirst();

		int idCorsaIndex = cursor.getColumnIndex("id");
		int idTrattaIndex = cursor.getColumnIndex("id_tratta");
		int versoUniIndex = cursor.getColumnIndex("verso_uni");
		int giorniIndex = cursor.getColumnIndex("giorni");

		while (!cursor.isAfterLast()) {
			int idCorsa = cursor.getInt(idCorsaIndex);
			int idTratta = cursor.getInt(idTrattaIndex);
			boolean versoUni = cursor.getInt(versoUniIndex) == 1;
			String giorni = cursor.getString(giorniIndex);
			ArrayList<BusFermata> cFermate = getFermate(idCorsa);
//			BusCorsa cCorsa = new BusCorsa(idTratta, idCorsa, versoUni, giorni, cFermate);
//			corse.add(cCorsa);
			cursor.moveToNext();
		}
		return corse;
	}

	private ArrayList<String> getStazioniTratta(int idTrattaDaCercare, boolean versoUni) {
		ArrayList<String> stazioni = new ArrayList<String>();
		String idTrattaDaCercareString = Integer.toString(idTrattaDaCercare);
		String versoUniString = versoUni ? "1" : "0";

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_FERMATE_NAME + " JOIN " + TABLE_CORSE_NAME + " ON " + "id_corsa = id");
		String orderBy = "posizione ASC";
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = qb.query(db, new String[] { "stazione" }, TABLE_FERMATE_NAME + ".id_tratta = ? AND verso_uni = ?", new String[] { idTrattaDaCercareString, versoUniString }, null, null, orderBy);

		int stazioneIndex = cursor.getColumnIndex("stazione");
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String stazione = cursor.getString(stazioneIndex);
			stazioni.add(stazione);
			cursor.moveToNext();
		}
		return stazioni;
	}

	private ArrayList<BusFermata> getFermate(int idCorsaDaCercare) {
		ArrayList<BusFermata> fermate = new ArrayList<BusFermata>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_FERMATE_NAME, null, "id_corsa = ?", new String[] { Integer.toString(idCorsaDaCercare) }, null, null, null);
		cursor.moveToFirst();

		int idTrattaIndex = cursor.getColumnIndex("id_tratta");
		int idCorsaIndex = cursor.getColumnIndex("id_corsa");
		int stazioneIndex = cursor.getColumnIndex("stazione");
		int oraIndex = cursor.getColumnIndex("ora");
		int posizioneIndex = cursor.getColumnIndex("posizione");

		while (!cursor.isAfterLast()) {
			int idTratta = cursor.getInt(idTrattaIndex);
			int idCorsa = cursor.getInt(idCorsaIndex);
			String stazione = cursor.getString(stazioneIndex);
			int ora = cursor.getInt(oraIndex);
			int posizione = cursor.getInt(posizioneIndex);

//			BusFermata cFermata = new BusFermata(idTratta, idCorsa, stazione, ora, posizione);
//			fermate.add(cFermata);
			cursor.moveToNext();
		}
		// TODO
//		Collections.sort(fermate);
		return fermate;
	}

//	public BusFermata getFermata(int idCorsaDaCercare, String stazioneDaCercare) {
//		SQLiteDatabase db = getReadableDatabase();
//		Cursor cursor = db.query(TABLE_FERMATE_NAME, null, "id_corsa = ? AND stazione = ?", new String[] { Integer.toString(idCorsaDaCercare), stazioneDaCercare }, null, null, null);
//		return getFermata(cursor);
//	}

//	public BusFermata getFermata(Cursor cursor) {
//		try {
//			int idTrattaIndex = cursor.getColumnIndex("id_tratta");
//			int idCorsaIndex = cursor.getColumnIndex("id_corsa");
//			int stazioneIndex = cursor.getColumnIndex("stazione");
//			int oraIndex = cursor.getColumnIndex("ora");
//			int posizioneIndex = cursor.getColumnIndex("posizione");
//
//			int idTratta = cursor.getInt(idTrattaIndex);
//			int idCorsa = cursor.getInt(idCorsaIndex);
//			String stazione = cursor.getString(stazioneIndex);
//			int ora = cursor.getInt(oraIndex);
//			int posizione = cursor.getInt(posizioneIndex);
//
//			return new BusFermata(idTratta, idCorsa, stazione, ora, posizione);
//		} catch (Exception e) {
//			return null;
//		}
//	}

}