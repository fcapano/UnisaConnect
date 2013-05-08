package it.fdev.unisaconnect;

import it.fdev.utils.MySimpleFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qozix.mapview.MapView;
import com.slidingmenu.lib.SlidingMenu;

public class MapFragment extends MySimpleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = (View) inflater.inflate(R.layout.map, container, false);
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mapView = new MapView(activity);
		activity.setContentView(mapView);
		mapView.addZoomLevel(1600, 1405, "tiles/uni_map_%row%_%col%.png", "uni_map_u.png");
//		mapView.addZoomLevel(1600, 1405, "uni_map.png");
	}

	@Override
	public void setVisibleActions() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionRefresh() {
		// TODO Auto-generated method stub

	}

	private MapView mapView;

	//
	// public MapView getMapView() {
	// return mapView;
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// return new MapView(getActivity());
	// }
	//
	@Override
	public void onPause() {
		super.onPause();
		mapView.clear();
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.requestRender();
		activity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.destroy();
		mapView = null;
	}

}