package it.fdev.unisaconnect.data;


public class Appello {
	private String name;
	private String date;
	private String description;
	private String subscribedNum;
	
	public Appello(String name, String date, String description, String subscribedNum){
		this.name = name;
		this.date = date;
		this.description = description;
		this.subscribedNum = subscribedNum;
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getSubscribedNum() {
		return subscribedNum;
	}
	
}
