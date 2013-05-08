package it.fdev.unisaconnect.wifilogin;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Questa classe che implementa AsyncTask gestisce gli eventi asincroni. 
 * Permette di effettuare la login/logout senza bloccare la ui ed aggiorna 
 * gli elementi tipo la scritta e l'immagine del wifi
 * @author francesco
 *
 */
public class AsyncLogin extends AsyncTask<Boolean, Integer, Integer> {
	
	private Context context;
	public static boolean isLoginRunning = false;
	
	public AsyncLogin(Context context){
		this.context = context;
	}
	
	/**
	 * Eseguita prima del doInBackground
	 */
	@Override
	protected void onPreExecute() {
		isLoginRunning = true;
	}
	
	/**
	 * Se il parametro è TRUE effettua la login, altrimenti la logout
	 */
	@Override
	protected Integer doInBackground(Boolean... params) {
		if(params[0]) { //Login
			publishProgress(0);
			int response_connect = LoginManager.login(context);
			return response_connect;
		} else {		//Logout
			publishProgress(1);
			boolean response_disconnect = LoginManager.logout(context);
			if(response_disconnect)
				return 5;
			else
				return 6;
		}
	}
	
	/**
	 * Aggiorna la UI all'inizio del processo di login/logout
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		 super.onProgressUpdate(values);
	}
	
	/**
	 * Eseguita dopo il doInBackground
	 */
	@Override
	protected void onPostExecute(Integer result) {
		isLoginRunning = false;
	}
}