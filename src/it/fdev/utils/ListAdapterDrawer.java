package it.fdev.utils;

import it.fdev.unisaconnect.R;
import it.fdev.utils.ListAdapterDrawer.ListItemDrawer;

import java.util.ArrayList;
import java.util.Collection;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapterDrawer extends ArrayAdapter<ListItemDrawer> {

	private final static int LAYOUT = R.layout.drawer_row;
	private ArrayList<ListItemDrawer> itemsList;

	private static class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}

	public ListAdapterDrawer(Context context, ArrayList<ListItemDrawer> itemsList) {
		super(context, LAYOUT, itemsList);
		this.itemsList = itemsList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder viewHolder = null;

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(LAYOUT, null);

			if (view != null) {
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) view.findViewById(R.id.row_icon);
				viewHolder.textView = (TextView) view.findViewById(R.id.row_title);
				view.setTag(viewHolder);
			}
		} else {
			view = convertView;
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (viewHolder != null) {
			ListItemDrawer item = itemsList.get(position);
			if (item != null) {
				viewHolder.textView.setText(item.text);
				view.setBackgroundResource(R.color.drawer_background);
				if (item.testing) {
					viewHolder.textView.setTextColor(parent.getResources().getColor(R.color.testing_red));
				} else {
					viewHolder.textView.setTextColor(parent.getResources().getColor(R.color.drawer_text_color));
				}
				viewHolder.imageView.setImageDrawable(null);
				viewHolder.imageView.setVisibility(View.VISIBLE);
					if(item.iconRes != R.drawable.transparent && item.iconRes != -1) {
					viewHolder.imageView.setImageResource(item.iconRes);
				} else {
					viewHolder.imageView.setVisibility(View.INVISIBLE);
				}
			}
		}
		return view;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void addAll(Collection<? extends ListItemDrawer> collection) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(collection);
		} else {
			for (ListItemDrawer entry : collection) {
				super.add(entry);
			}
		}
	}

	public static class ListItemDrawer {
		private String text = null;
		private int iconRes = -1;
		// it testing=true the text is red
		private boolean testing = false;

		public ListItemDrawer(String tag, int iconRes) {
			this(tag, iconRes, false);
		}

		public ListItemDrawer(String tag, int iconRes, boolean testing) {
			this.text = tag;
			this.iconRes = iconRes;
			this.testing = testing;
		}

	}
}
