package it.fdev.unisaconnect.data;

import android.os.Parcel;
import android.os.Parcelable;

public class BookDetails extends Book implements Parcelable {
	
	private String edition;
	private String publication;
	private String descr;
	private String series;
	private String lang;
	private String subject;
	private String cdd;
	private String isbn;
	
	public BookDetails(String author, String title, String detailsUrl, String position, String edition, String publication, 
					   String descr, String series, String lang, String subject, String cdd, String isbn) {
		super(null, author, null, title, null, detailsUrl, position);
		this.edition = edition;
		this.publication = publication;
		this.descr = descr;
		this.series = series;
		this.lang = lang;
		this.subject = subject;
		this.cdd = cdd;
		this.isbn = isbn;
	}

	public BookDetails(Parcel in) {
		super(in);
		edition = in.readString();
		publication = in.readString();
		descr = in.readString();
		series = in.readString();
		lang = in.readString();
		subject = in.readString();
		cdd = in.readString();
		isbn = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(edition);
		dest.writeString(publication);
		dest.writeString(descr);
		dest.writeString(series);
		dest.writeString(lang);
		dest.writeString(subject);
		dest.writeString(cdd);
		dest.writeString(isbn);
	}
	
	public static final Parcelable.Creator<BookDetails> CREATOR = new Parcelable.Creator<BookDetails>() {
		public BookDetails createFromParcel(Parcel in) {
			return new BookDetails(in);
		}

		public BookDetails[] newArray(int size) {
			return new BookDetails[size];
		}
	};

	public String getEdition() {
		return edition;
	}

	public String getPublication() {
		return publication;
	}

	public String getDescr() {
		return descr;
	}

	public String getSeries() {
		return series;
	}

	public String getLang() {
		return lang;
	}

	public String getSubject() {
		return subject;
	}

	public String getCdd() {
		return cdd;
	}

	public String getIsbn() {
		return isbn;
	}
	
}
