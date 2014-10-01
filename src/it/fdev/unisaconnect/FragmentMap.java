package it.fdev.unisaconnect;

import it.fdev.unisaconnect.map.CustomMapTileProvider;
import it.fdev.utils.MyMapFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

public class FragmentMap extends MyMapFragment implements OnCameraChangeListener {

	public static final int MIN_ZOOM = 15;
	public static final int MAX_ZOOM = 19;
	private static final int CUSTOM_ANIM_DURATION = 175;
	private static final String OVERLAY_TILES_FOLDER = "map/google_map_overlay";
	private static final LatLng UNISA_CENTER = new LatLng(40.7721671, 14.7904956);
	private static final LatLngBounds UNISA_BOUNDS = new LatLngBounds(new LatLng(40.766, 14.786), new LatLng(40.777,
			14.798));

	private LatLng lastValidPosition;
	private View view;
	private GoogleMap googleMap;

	public static FragmentMap newInstance() {

		return FragmentMap.newInstance(null);
	}

	public static FragmentMap newInstance(GoogleMapOptions googleMapOptions) {

		FragmentMap fragment = new FragmentMap();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = super.onCreateView(inflater, container, savedInstanceState);
		initializeMap();

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (UNISA_BOUNDS.contains(position.target)) {
			lastValidPosition = position.target;
		} else {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastValidPosition, position.zoom),
					CUSTOM_ANIM_DURATION, null);
		}
		float zoom = position.zoom;
		if (zoom > MAX_ZOOM) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastValidPosition, MAX_ZOOM));
		} else if (zoom < MIN_ZOOM) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastValidPosition, MIN_ZOOM));
		}
	}

	@Override
	public int getTitleResId() {
		return R.string.map;
	}

	private void initializeMap() {

		googleMap = getMap();
		UiSettings settings = googleMap.getUiSettings();

		googleMap.setMyLocationEnabled(true);
		googleMap.setOnCameraChangeListener(this);

		TileProvider tileProvider = new CustomMapTileProvider(getActivity().getAssets(), OVERLAY_TILES_FOLDER);

		googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UNISA_CENTER, MIN_ZOOM));

		lastValidPosition = UNISA_CENTER;
	}
}