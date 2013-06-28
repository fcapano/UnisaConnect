package it.fdev.unisaconnect;

import it.fdev.unisaconnect.MainActivity.BootableFragmentsEnum;
import it.fdev.utils.CustomButtonWithImg;
import it.fdev.utils.MySimpleFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class Esse3ServicesFragment extends MySimpleFragment {
	
	CustomButtonWithImg webBtn, librettoBtn, appelliBtn, presenzeBtn, pagamentiBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.esse3_services, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		webBtn = (CustomButtonWithImg) view.findViewById(R.id.web_page_btn);
		librettoBtn = (CustomButtonWithImg) view.findViewById(R.id.libretto_btn);
		appelliBtn = (CustomButtonWithImg) view.findViewById(R.id.appelli_btn);
		presenzeBtn = (CustomButtonWithImg) view.findViewById(R.id.presenze_btn);
		pagamentiBtn = (CustomButtonWithImg) view.findViewById(R.id.pagamenti_btn);
		
		webBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.ESSE3_WEB, false);
			}
		});
		
		librettoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.LIBRETTO, false);
			}
		});
		
		appelliBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.APPELLI, false);
			}
		});
		
		presenzeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.PRESENZE, false);
			}
		});
		
		pagamentiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(BootableFragmentsEnum.ESSE3_WEB, false);
			}
		});
	}
}