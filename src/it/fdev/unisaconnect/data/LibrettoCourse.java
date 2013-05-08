package it.fdev.unisaconnect.data;


public class LibrettoCourse {
	private String name;
	private String cfu;
	private String mark;
	
	public LibrettoCourse(String name, String cfu, String mark){
		this.name = name;
		this.cfu = cfu;
		this.mark = mark;
	}
	
	public String getName() {
		return name;
	}

	public String getCFU() {
		return cfu;
	}
	
	public String getMark() {
		return mark;
	}
}
