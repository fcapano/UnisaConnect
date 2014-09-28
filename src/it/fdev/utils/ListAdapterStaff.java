package it.fdev.utils;

import it.fdev.unisaconnect.R;
import it.fdev.unisaconnect.data.StaffMemberSummary;

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
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ListAdapterStaff extends ArrayAdapter<StaffMemberSummary> {

	private int layout;
//	private ArrayList<StaffMemberSummary> itemsList;
	private ImageLoader imageLoader;

	private static class ViewHolder {
		private ImageView imageView;
		// private ImageView genericImageView;
		private TextView nameView;
//		private TextView roleView;
		private TextView emailView;
	}

	public ListAdapterStaff(Context context, int layout, ArrayList<StaffMemberSummary> itemsList) {
		super(context, layout, itemsList);
		this.layout = layout;
//		this.itemsList = itemsList;
		this.imageLoader = ImageLoader.getInstance();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(layout, null);
			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.row_icon);
			// viewHolder.genericImageView = (ImageView) view.findViewById(R.id.row_icon_generic);
			viewHolder.nameView = (TextView) convertView.findViewById(R.id.row_name);
//			viewHolder.roleView = (TextView) convertView.findViewById(R.id.row_role);
			viewHolder.emailView = (TextView) convertView.findViewById(R.id.row_email);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
//			viewHolder.imageView.setImageDrawable(null);
			imageLoader.displayImage(null, viewHolder.imageView);
		}

		StaffMemberSummary item = getItem(position);

		viewHolder.nameView.setText(item.getNome());
//		viewHolder.roleView.setText(item.getRuolo());
		viewHolder.emailView.setText(item.getEmail());
		// viewHolder.imageView.setImageResource(R.drawable.generic_profile_icon);

		if (item.iconURL != null) {
			imageLoader.displayImage(item.iconURL, viewHolder.imageView, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					// ((ImageView) arg1).setImageResource(R.drawable.generic_profile_icon);
				}

				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					// ((ImageView) arg1).setImageResource(R.drawable.generic_profile_icon);
					try {
						imageLoader.displayImage(null, ((ImageView) arg1));
					} catch(IllegalArgumentException e) {
						// View was already deleted. Ignore
					}
				}

				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
					// ((ImageView) arg1).setImageBitmap(arg2);
				}

				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					// ((ImageView) arg1).setImageResource(R.drawable.generic_profile_icon);
//					imageLoader.displayImage(null, ((ImageView) arg1));
				}
			});
		} else {
			// viewHolder.imageView.setImageResource(R.drawable.generic_profile_icon);
//			imageLoader.displayImage(null, viewHolder.imageView);
		}

		return convertView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void addAll(Collection<? extends StaffMemberSummary> collection) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(collection);
		} else {
			for (StaffMemberSummary entry : collection) {
				super.add(entry);
			}
		}
	}
}
