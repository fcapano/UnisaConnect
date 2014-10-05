package it.fdev.unisaconnect.map;

import java.io.Serializable;

public class MapFocusPoint implements Serializable {

	private static final long serialVersionUID = 4889400068039632619L;

	private String title;
	private String subtitle;
	private Double latitude;
	private Double Longitude;

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return Longitude;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Double longitude) {
		Longitude = longitude;
	}
}