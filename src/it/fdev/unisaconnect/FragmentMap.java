package it.fdev.unisaconnect;

import it.fdev.unisaconnect.map.GoogleMapOverlayMapTileProvider;
import it.fdev.unisaconnect.map.MapFocusPoint;
import it.fdev.utils.MyMapFragment;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

public class FragmentMap extends MyMapFragment implements OnCameraChangeListener {

	public static final String MAP_FOCUS_POINT_ARG = "mapFocusPoint";
	public static final int MIN_ZOOM = 15;
	public static final int MAX_ZOOM = 19;
	public static final int MID_ZOOM = (MIN_ZOOM + MAX_ZOOM) / 2;
	private static final int CUSTOM_ANIM_DURATION = 175;
	private static final String OVERLAY_TILES_FOLDER = "map/google_map_overlay";
	private static final LatLng UNISA_CENTER = new LatLng(40.7721671, 14.7904956);
	private static final LatLngBounds UNISA_BOUNDS = new LatLngBounds(new LatLng(40.766, 14.786), new LatLng(40.777,
			14.798));

	private LatLng lastValidPosition;
	private GoogleMap googleMap;

	public static FragmentMap newInstance() {

		return new FragmentMap();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		googleMap = getMap();
		if (googleMap != null) {
			initializeMap(getArguments());
		}
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

	private void initializeMap(Bundle fragmentArguments) {

		googleMap.setMyLocationEnabled(true);
		googleMap.setOnCameraChangeListener(this);

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UNISA_CENTER, MIN_ZOOM));

		TileProvider tileProvider = new GoogleMapOverlayMapTileProvider(getActivity().getAssets(), OVERLAY_TILES_FOLDER);
		googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));

		if (fragmentArguments != null) {
			MapFocusPoint focusPoint = (MapFocusPoint) fragmentArguments.getSerializable(MAP_FOCUS_POINT_ARG);
			if (focusPoint != null)
				focusMapWithMapFocusPoint(focusPoint);
		} else {
			focusMap(UNISA_CENTER, MIN_ZOOM);
		}
	}

	private void focusMap(LatLng focusCenter, int zoom) {

		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focusCenter, zoom));
		lastValidPosition = focusCenter;
	}

	private void focusMapWithMapFocusPoint(MapFocusPoint focusPoint) {

		LatLng focusCenter = new LatLng(focusPoint.getLatitude(), focusPoint.getLongitude());
		focusMap(focusCenter, MID_ZOOM);

		MarkerOptions markerOptions = new MarkerOptions().title(focusPoint.getTitle())
				.snippet(focusPoint.getSubtitle()).position(focusCenter);
		Marker marker = googleMap.addMarker(markerOptions);
		marker.showInfoWindow();
	}
}