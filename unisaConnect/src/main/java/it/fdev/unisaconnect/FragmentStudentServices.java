package it.fdev.unisaconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.unisaconnect.data.SharedPrefDataManager;
import it.fdev.utils.CustomButtonWithImg;
import it.fdev.utils.MySimpleFragment;

public class FragmentStudentServices extends MySimpleFragment {

	private SharedPrefDataManager mDataManager;
	private CustomButtonWithImg tipoCorsoBtn, accountBtn, webBtn, librettoBtn, appelliBtn, presenzeBtn, pagamentiBtn, webmailBtn, bibliotecaButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mDataManager = SharedPrefDataManager.getInstance(mActivity.getApplicationContext());
		if (!mDataManager.loginDataExists()) {
			mActivity.switchContent(BootableFragmentsEnum.ACCOUNT, true);
			return null;
		}
		View mainView = (View) inflater.inflate(R.layout.fragment_student_services, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		tipoCorsoBtn = (CustomButtonWithImg) view.findViewById(R.id.tipo_corso_btn);
		accountBtn = (CustomButtonWithImg) view.findViewById(R.id.account_btn);
		webBtn = (CustomButtonWithImg) view.findViewById(R.id.web_page_btn);
		librettoBtn = (CustomButtonWithImg) view.findViewById(R.id.libretto_btn);
		appelliBtn = (CustomButtonWithImg) view.findViewById(R.id.appelli_btn);
		pagamentiBtn = (CustomButtonWithImg) view.findViewById(R.id.pagamenti_btn);
		webmailBtn = (CustomButtonWithImg) view.findViewById(R.id.webmail_btn);
		bibliotecaButton = (CustomButtonWithImg) view.findViewById(R.id.biblioteca_btn);
		presenzeBtn = (CustomButtonWithImg) view.findViewById(R.id.presenze_btn);

		accountBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.ACCOUNT, false);
			}
		});

		webBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.ESSE3_WEB, false);
			}
		});

		librettoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.LIBRETTO, false);
			}
		});

		appelliBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.APPELLI, false);
			}
		});

		presenzeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.PRESENZE, false);
			}
		});

		pagamentiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.PAGAMENTI, false);
			}
		});

		webmailBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.WEBMAIL, false);
			}
		});
		
		bibliotecaButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.BIBLIO_SEARCH, false);
			}
		});
		
		tipoCorsoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.switchContent(BootableFragmentsEnum.TIPO_CORSO, false);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mDataManager.loginDataExists()) {
			mActivity.switchContent(BootableFragmentsEnum.ACCOUNT, true);
		}
	}

	@Override
	public int getTitleResId() {
		return R.string.servizi_esse3;
	}

}