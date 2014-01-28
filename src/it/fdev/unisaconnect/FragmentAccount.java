package it.fdev.unisaconnect;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.unisaconnect.wifilogin.AsyncLogin;
import it.fdev.unisaconnect.wifilogin.NetworkStateChanged;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentAccount extends MySimpleFragment {
	
	private SharedPrefDataManager dataManager;
	private AsyncLogin runningAsyncTask;
	
	private CheckBox checkboxLoginAutomatica;
	private EditText editTextUser, editTextPass;
	private TextView bottoneSalva;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataManager = new SharedPrefDataManager(activity);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }
    
    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
    	checkboxLoginAutomatica = (CheckBox) view.findViewById(R.id.loginAutomatica);
    	editTextUser = (EditText) view.findViewById(R.id.user);
    	editTextPass = (EditText) view.findViewById(R.id.pass);
		bottoneSalva = (TextView) view.findViewById(R.id.buttonSave);
		
    	InputFilter filter = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if((source.charAt(i)+"").equals("@")) {
						Toast.makeText(activity, getString(R.string.non_inserire_at), Toast.LENGTH_SHORT).show();
						return "";
					}
					if((source.charAt(i)+"").equals(" "))
						return "";
				}
				return null;
			}
		};
		editTextUser.setFilters(new InputFilter[]{filter}); 

		// Check tooggle test status
		editTextUser.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable e) {
				if(editTextUser.getText().toString().equals(Utils.TOGGLE_TESTING_STRING)) {
					editTextUser.setText("");
					boolean testing = ! dataManager.isTestingingEnabled();
					dataManager.setTestingEnabled(testing);
		        	Toast.makeText(activity, "Testing enabled: " + testing, Toast.LENGTH_LONG).show();
		        	activity.finish();
		        	activity.startActivity(activity.getIntent());
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
    	
		bottoneSalva.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String user, pass;
				Boolean loginAutomatica = checkboxLoginAutomatica.isChecked();
				user = editTextUser.getText().toString().trim().replace(" ", "");
				pass = editTextPass.getText().toString();
				
				if(user.length()>0 && pass.length()>0)
					salvaDati(user, pass, loginAutomatica);
				else
					Toast.makeText(activity.getApplicationContext(), getString(R.string.dati_non_validi), Toast.LENGTH_SHORT).show();
			}
		});
		fillData();
	}
    
	private void salvaDati(String user, String pass, boolean loginAutomatica) {
		
		try {
			dataManager.setUser(user);
			dataManager.setPass(pass);
			dataManager.setLoginAutomatica(loginAutomatica);
			
			Context context = activity.getApplicationContext();
			NetworkStateChanged.setEnableBroadcastReceiver(context, loginAutomatica);
			Toast.makeText(context, getString(R.string.dati_salvati), Toast.LENGTH_LONG).show();
			
			if(!AsyncLogin.isLoginRunning || runningAsyncTask == null || runningAsyncTask.isCancelled()) {	//Avvia la login
				runningAsyncTask = new AsyncLogin(context);
		     	runningAsyncTask.execute(true);
			}
//			if (!Esse3ScraperService.isRunning) {
//				activity.startService(new Intent(activity, Esse3ScraperService.class).setAction(Esse3ScraperService.BROADCAST_STATE_E3_LIBRETTO));
//			}
			
			activity.switchContent(BootableFragmentsEnum.STUDENT_SERVICES, true);
		} catch(Exception e) {
			Log.e(Utils.TAG, "Error saving data!", e);
		}
		fillData();
	}
	
	public void fillData() {
		// Restore preferences
		if (!isAdded()) {
			return;
		}
		if(!dataManager.loginDataExists()) {
			editTextUser.setText("");
			editTextPass.setText("");
			checkboxLoginAutomatica.setChecked(true);
			return;
		}
		
		String user = dataManager.getUser();
		String pass = dataManager.getPass();
		boolean loginAutomatica = dataManager.isLoginAutomatica();
		
		editTextUser.setText((user!=null?user:""));
		editTextPass.setText((pass!=null?pass:""));
		checkboxLoginAutomatica.setChecked(loginAutomatica);
	}
	
	@Override
	public int getTitleResId() {
		return R.string.account;
	}
	
}