package it.fdev.utils;

import it.fdev.unisaconnect.R;
import it.fdev.utils.ListAdapter.ListItem;

import java.util.ArrayList;
import java.util.Collection;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ListAdapter extends ArrayAdapter<ListItem> {

	private int layout;
	private ArrayList<ListItem> itemsList;
	private ImageLoader imageLoader;
	private DrawableManager drawableManager;

	private static class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}

	public ListAdapter(Context context, int layout, ArrayList<ListItem> itemsList) {
		super(context, layout, itemsList);
		this.layout = layout;
		this.itemsList = itemsList;
		this.imageLoader = ImageLoader.getInstance();
		this.drawableManager = new DrawableManager();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder viewHolder = null;

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(layout, null);

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
			ListItem item = itemsList.get(position);
			if (item != null) {
				viewHolder.textView.setText(item.text);
				
				if(item.backgroundColor != -1) {
					view.setBackgroundResource(item.backgroundColor);
				}
				
				if (item.testing)
					viewHolder.textView.setTextColor(parent.getResources().getColor(R.color.testing_red));
				else
					viewHolder.textView.setTextColor(parent.getResources().getColor(android.R.color.white));

				viewHolder.imageView.setImageDrawable(null);
				viewHolder.imageView.setVisibility(View.VISIBLE);
				if (item.iconURL != null) {
					if (item.useCache) {
						imageLoader.displayImage(item.iconURL, viewHolder.imageView, new ImageLoadingListener() {
							@Override
							public void onLoadingStarted(String arg0, View arg1) {
							}
							@Override
							public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
								arg1.setVisibility(View.INVISIBLE);
							}
							@Override
							public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
							}
							@Override
							public void onLoadingCancelled(String arg0, View arg1) {
//								arg1.setVisibility(View.INVISIBLE);
							}
						});
					} else {
						drawableManager.fetchDrawableOnThread(item.iconURL, viewHolder.imageView);
					}
				} else if(item.iconRes != R.drawable.transparent && item.iconRes != -1) {
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
	public void addAll(Collection<? extends ListItem> collection) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(collection);
		} else {
			for (ListItem entry : collection) {
				super.add(entry);
			}
		}
	}

	public static class ListItem {
		public String text = null;
		public int iconRes = -1;
		public String iconURL = null;
		public boolean useCache = false;
		public boolean testing = false;
		
		// On GB when scrolling the rows have black background instead of transparent 
		public int backgroundColor;

		public ListItem(String tag) {
			this(tag, -1);
		}

		public ListItem(String tag, int iconRes) {
			this(tag, iconRes, false);
		}
		
		public ListItem(String tag, int iconRes, boolean testing) {
			this(tag, iconRes, testing, -1);
		}
		
		public ListItem(String tag, int iconRes, int backgroundColor) {
			this(tag, iconRes, false, backgroundColor);
		}

		public ListItem(String tag, int iconRes, boolean testing, int backgroundColor) {
			this.text = tag;
			this.iconRes = iconRes;
			this.testing = testing;
			this.backgroundColor = backgroundColor;
		}

		
		public ListItem(String tag, String iconURL, boolean useCache) {
			this(tag, iconURL, useCache, false);
		}
		
		public ListItem(String tag, String iconURL, boolean useCache, boolean testing) {
			this(tag, iconURL, useCache, testing, -1);
		}
		
		public ListItem(String tag, String iconURL, boolean useCache, int backgroundColor) {
			this(tag, iconURL, useCache, false, backgroundColor);
		}

		public ListItem(String tag, String iconURL, boolean useCache, boolean testing, int backgroundColor) {
			this.text = tag;
			this.iconURL = iconURL;
			this.useCache = useCache;
			this.testing = testing;
			this.backgroundColor = backgroundColor;
		}
	}
}
