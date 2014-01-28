package it.fdev.unisaconnect.data;

import it.fdev.unisaconnect.R;
import it.fdev.utils.ListAdapter.ListItem;

public class StaffMemberSummary extends ListItem {

	private String matricola;

	public StaffMemberSummary(String matricola, String fullname, String imgUrl) {
		super(fullname, imgUrl, true, R.color.activity_background);
		this.matricola = matricola;
	}

	public String getMatricola() {
		return matricola;
	}

	public String getFullname() {
		return super.text;
	}

}
