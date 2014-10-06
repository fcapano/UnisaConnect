package it.fdev.utils;

import it.fdev.unisaconnect.R;
import it.fdev.utils.CardsAdapter.CardItem;

import java.util.ArrayList;
import java.util.Collection;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CardsAdapter extends ArrayAdapter<CardItem> {

	private int layout;
	private ArrayList<CardItem> itemsList;

	private static class ViewHolder {
		private TextView titleView;
		private TextView textView;
		private TextView dateView;
	}

	public CardsAdapter(Context context, int layout, ArrayList<CardItem> itemsList) {
		super(context, layout, itemsList);
		this.layout = layout;
		this.itemsList = itemsList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder viewHolder = null;

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(layout, null);

			if (view != null) {
				viewHolder = new ViewHolder();
				viewHolder.titleView = (TextView) view.findViewById(R.id.card_title);
				viewHolder.textView = (TextView) view.findViewById(R.id.card_text);
				viewHolder.dateView = (TextView) view.findViewById(R.id.card_date);
				view.setTag(viewHolder);
			}
		} else {
			view = convertView;
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (viewHolder != null) {
			CardItem item = itemsList.get(position);
			if (item != null) {
				if (item.title.isEmpty()) {
					viewHolder.titleView.setVisibility(View.GONE);
				} else {
					viewHolder.titleView.setVisibility(View.VISIBLE);
				}
				viewHolder.titleView.setText(item.title);
				
				if (item.text.isEmpty()) {
					viewHolder.textView.setVisibility(View.GONE);
				} else {
					viewHolder.textView.setVisibility(View.VISIBLE);
				}
				viewHolder.textView.setText(item.text);
				
				if (item.date == null) {
					item.date = "";
				}
				
				viewHolder.dateView.setText(item.date);
				viewHolder.titleView.setSingleLine(item.isSingleLine());
			}
		}
		return view;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void addAll(Collection<? extends CardItem> collection) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(collection);
		} else {
			for (CardItem entry : collection) {
				super.add(entry);
			}
		}
	}

	public static class CardItem {
		private String title = null;
		private String link = null;
		private String text = null;
		private String date = null;
		private boolean singleLine;
		private boolean isButton;

		public CardItem(String title, String link, String text, String date, boolean singleLine) {
			this.title = title;
			this.link = link;
			this.text = text;
			this.date = date;
			this.singleLine = singleLine;
		}
		
		public CardItem(String title, String link, String text, String date) {
			this(title, link, text, date, true);
		}
		
		public CardItem(String title) {
			this.title = title;
			this.isButton = true;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}
		
		public boolean isSingleLine() {
			return singleLine;
		}
		
		public boolean isButton() {
			return isButton;
		}
		
	}
}
