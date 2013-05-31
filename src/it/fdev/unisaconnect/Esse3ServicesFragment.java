package it.fdev.unisaconnect;

import it.fdev.utils.CustomButtonWithImg;
import it.fdev.utils.MySimpleFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class Esse3ServicesFragment extends MySimpleFragment {
	
	CustomButtonWithImg webBtn, librettoBtn, appelliBtn, pagamentiBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.esse3_services, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		webBtn = (CustomButtonWithImg) activity.findViewById(R.id.web_page_btn);
		librettoBtn = (CustomButtonWithImg) activity.findViewById(R.id.libretto_btn);
		appelliBtn = (CustomButtonWithImg) activity.findViewById(R.id.appelli_btn);
		pagamentiBtn = (CustomButtonWithImg) activity.findViewById(R.id.pagamenti_btn);
		
		webBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(new Esse3WebFragment());
			}
		});
		
		librettoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(new LibrettoFragment());
			}
		});
		
		appelliBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(new AppelliFragment());
			}
		});
		
		pagamentiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchContent(new Esse3WebFragment());
			}
		});
	}
	
	@Override
	public void setVisibleActions() {
	}

	@Override
	public void actionRefresh() {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}