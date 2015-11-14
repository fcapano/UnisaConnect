package it.fdev.unisaconnect.data;


public class StaffMemberSummary {

	public String iconURL = null;

	private String matricola;
	private String nome;
//	private String ruolo;
	private String email;
	
	public StaffMemberSummary(String matricola, String nome, /*String ruolo,*/ String email, String iconURL) {
		this.matricola = matricola;
		this.nome = nome;
//		this.ruolo = ruolo;
		this.email = email;
		this.iconURL = iconURL;
	}

	public String getMatricola() {
		return matricola;
	}
	
	public String getIconURL() {
		return iconURL;
	}

	public String getNome() {
		return nome;
	}

//	public String getRuolo() {
//		return ruolo;
//	}

	public String getEmail() {
		return email;
	}

}
