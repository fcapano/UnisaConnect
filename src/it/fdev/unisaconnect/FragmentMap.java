// This class uses the library https://github.com/moagrius/MapView published under CC license

package it.fdev.unisaconnect;

import it.fdev.utils.CustomMapMarker;
import it.fdev.utils.MySimpleFragment;
import it.fdev.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qozix.mapview.MapView;
import com.qozix.mapview.hotspots.Poly;

public class FragmentMap extends MySimpleFragment {
	
	private final static int ORIGINAL_IMG_WIDTH = 5000; 	// Dimensione dell'immagine da dove vengono prese le cordinate
	private final static int ORIGINAL_IMG_HEIGHT = 3692;
	private final static int RESIZED_IMG_WIDTH = 3000;		// Dimensione dell'immagine su cui devono essere trasposte le cordinate 
	private final static int RESIZED_IMG_HEIGHT = 2215;
	
	private MapView mapView;
	private View lastMarkerAdded;
	
	private static ArrayList<HotspotItem> hotspotList = new ArrayList<HotspotItem>();
	static {
		String facoltaPrefix = "Facoltà di ";
		
		String labelEconomia = facoltaPrefix + "Economia";
		Point[] verticesEconomia1 = { new MyPoint(3106, 1586), new MyPoint(3293, 1585), new MyPoint(3295, 1771), new MyPoint(3183, 1772), new MyPoint(3182, 1727), 
									  new MyPoint(3151, 1727), new MyPoint(3151, 1666), new MyPoint(3106, 1666) }; 
		Point[] verticesEconomia2 = { new MyPoint(3098, 1225), new MyPoint(3177, 1226), new MyPoint(3177, 1200), new MyPoint(3308, 1200), new MyPoint(3308, 1305), 
									  new MyPoint(3290, 1305), new MyPoint(3290, 1399), new MyPoint(3188, 1398), new MyPoint(3188, 1358), new MyPoint(3149, 1358), 
									  new MyPoint(3149, 1331), new MyPoint(3133, 1331), new MyPoint(3133, 1310), new MyPoint(3098, 1310) };
		hotspotList.add(new HotspotItem(verticesEconomia1, labelEconomia));
		hotspotList.add(new HotspotItem(verticesEconomia2, labelEconomia));
		
		
		String labelFarmacia = facoltaPrefix + "Farmacia";
		Point[] verticesFarmacia = { new MyPoint(1348, 2163), new MyPoint(1547, 2160), new MyPoint(1549, 2356), new MyPoint(1350, 2359) }; 
		hotspotList.add(new HotspotItem(verticesFarmacia, labelFarmacia));
		
		String labelGiurisprudenza = facoltaPrefix + "Giurisprudenza";
		Point[] verticesGiurisprudenza = { new MyPoint(3824, 1491), new MyPoint(4001, 1488), new MyPoint(4000, 1455), new MyPoint(4032, 1454), new MyPoint(4033, 1489), 
				new MyPoint(4205, 1486), new MyPoint(4205, 1453), new MyPoint(4234, 1452), new MyPoint(4235, 1479), new MyPoint(4246, 1479), new MyPoint(4246, 1496), 
				new MyPoint(4337, 1494), new MyPoint(4340, 1619), new MyPoint(4340, 1625), new MyPoint(4413, 1624), new MyPoint(4415, 1728), new MyPoint(4439, 1728), 
				new MyPoint(4442, 1833), new MyPoint(4310, 1836), new MyPoint(4309, 1810), new MyPoint(4231, 1811), new MyPoint(4227, 1638), new MyPoint(3825, 1647), 
				new MyPoint(3825, 1635), new MyPoint(3798, 1635), new MyPoint(3795, 1502), new MyPoint(3824, 1502) }; 
		hotspotList.add(new HotspotItem(verticesGiurisprudenza, labelGiurisprudenza));
		
		String labelIngegneria = facoltaPrefix + "Ingegneria";
		Point[] verticesIngegneria1 = { new MyPoint(1650, 1714), new MyPoint(2062, 1709), new MyPoint(2062, 1716), new MyPoint(2154, 1715), new MyPoint(2154, 1701), 
									    new MyPoint(2164, 1701), new MyPoint(2164, 1670), new MyPoint(2193, 1669), new MyPoint(2194, 1706), new MyPoint(2368, 1705), 
									    new MyPoint(2367, 1670), new MyPoint(2401, 1670), new MyPoint(2401, 1707), new MyPoint(2572, 1705), new MyPoint(2572, 1716), 
									    new MyPoint(2624, 1715), new MyPoint(2624, 1670), new MyPoint(2593, 1670), new MyPoint(2593, 1587), new MyPoint(2780, 1587), 
									    new MyPoint(2780, 1771), new MyPoint(2702, 1771), new MyPoint(2702, 1815), new MyPoint(2672, 1815), new MyPoint(2672, 1850), 
									    new MyPoint(1651, 1863) };
		Point[] verticesIngegneria2 = { new MyPoint(1983, 1912), new MyPoint(2096, 1912), new MyPoint(2096, 1934), new MyPoint(2135, 1934), new MyPoint(2135, 1985), 
										new MyPoint(2171, 1985), new MyPoint(2172, 2095), new MyPoint(1984, 2096) };
		hotspotList.add(new HotspotItem(verticesIngegneria1, labelIngegneria));
		hotspotList.add(new HotspotItem(verticesIngegneria2, labelIngegneria));
		
		String labelLettereEFilosofia = facoltaPrefix + "Lettere e Filosofia";
		Point[] verticesLettereEFilosofia = { new MyPoint(3313, 1395), new MyPoint(3486, 1393), new MyPoint(3486, 1358), new MyPoint(3519, 1357), new MyPoint(3520, 1393), 
				new MyPoint(3695, 1391), new MyPoint(3694, 1356), new MyPoint(3724, 1356), new MyPoint(3725, 1382), new MyPoint(3736, 1382), new MyPoint(3737, 1402), 
				new MyPoint(3826, 1401), new MyPoint(3828, 1484), new MyPoint(3818, 1484), new MyPoint(3818, 1495), new MyPoint(3791, 1496), new MyPoint(3791, 1534), 
				new MyPoint(3721, 1535), new MyPoint(3721, 1561), new MyPoint(3698, 1561), new MyPoint(3698, 1541), new MyPoint(3521, 1543), new MyPoint(3521, 1564), 
				new MyPoint(3489, 1565), new MyPoint(3489, 1542), new MyPoint(3314, 1545) }; 
		hotspotList.add(new HotspotItem(verticesLettereEFilosofia, labelLettereEFilosofia));
		
		String labelLingueELetteratureStraniere	= facoltaPrefix + "Lingue e Letterature Straniere";
		Point[] verticesLingueELett1 = { new MyPoint(2674, 1817), new MyPoint(2704, 1817), new MyPoint(2704, 1804), new MyPoint(2877, 1801), new MyPoint(2877, 1768), 
										 new MyPoint(2909, 1768), new MyPoint(2910, 1804), new MyPoint(2967, 1803), new MyPoint(2967, 1953), new MyPoint(2676, 1956) };
		Point[] verticesLingueELett2 = { new MyPoint(2704, 2072), new MyPoint(2718, 2072), new MyPoint(2718, 2033), new MyPoint(2777, 2032), new MyPoint(2777, 1995), 
										 new MyPoint(2888, 1994), new MyPoint(2890, 2179), new MyPoint(2705, 2181) }; 
		hotspotList.add(new HotspotItem(verticesLingueELett1, labelLingueELetteratureStraniere));
		hotspotList.add(new HotspotItem(verticesLingueELett2, labelLingueELetteratureStraniere));
		
//		String labelMedicinaEChirurgia = facoltaPrefix + "Medicina e Chirurgia";

		String labelScienzeDellaFormazione 		= facoltaPrefix + "Scienze della Formazione";
		Point[] verticesScienzeDellaFormazione = { new MyPoint(2772, 1360), new MyPoint(2802, 1360), new MyPoint(2803, 1398), new MyPoint(2977, 1396), new MyPoint(2977, 1360), 
								new MyPoint(3008, 1360), new MyPoint(3009, 1396), new MyPoint(3183, 1394), new MyPoint(3183, 1406), new MyPoint(3311, 1405), new MyPoint(3313, 1565), 
								new MyPoint(3293, 1565), new MyPoint(3293, 1535), new MyPoint(3182, 1537), new MyPoint(3182, 1545), new MyPoint(3007, 1547), new MyPoint(3007, 1565), 
								new MyPoint(2986, 1565), new MyPoint(2985, 1549), new MyPoint(2806, 1551), new MyPoint(2806, 1543), new MyPoint(2774, 1543), new MyPoint(2772, 1406), 
								new MyPoint(2764, 1406), new MyPoint(2764, 1392), new MyPoint(2772, 1392) }; 
		hotspotList.add(new HotspotItem(verticesScienzeDellaFormazione, labelScienzeDellaFormazione));
		
		String labelSMMFFNN = facoltaPrefix + "Scienze MM.FF.NN.";
		Point[] verticesSMMFFNN1 = { new MyPoint(685, 1670), new MyPoint(870, 1670), new MyPoint(870, 1860), new MyPoint(767, 1860), new MyPoint(767, 1838), 
									 new MyPoint(724, 1838), new MyPoint(724, 1785), new MyPoint(684, 1785) };
		Point[] verticesSMMFFNN2 = { new MyPoint(1066, 1618), new MyPoint(1308, 1618), new MyPoint(1308, 1858), new MyPoint(1066, 1858) }; 
		Point[] verticesSMMFFNN3 = { new MyPoint(375, 1942), new MyPoint(547, 1941), new MyPoint(546, 1892), new MyPoint(600, 1892), new MyPoint(600, 1942), 
									 new MyPoint(600, 1943), new MyPoint(750, 1942), new MyPoint(750, 1893), new MyPoint(750, 1892), new MyPoint(807, 1892), 
									 new MyPoint(807, 1942), new MyPoint(955, 1941), new MyPoint(955, 1890), new MyPoint(1010, 1890), new MyPoint(1010, 1940), 
									 new MyPoint(1163, 1939), new MyPoint(1163, 1896), new MyPoint(1210, 1895), new MyPoint(1211, 1940), new MyPoint(1369, 1939), 
									 new MyPoint(1369, 1897), new MyPoint(1414, 1896), new MyPoint(1414, 1938), new MyPoint(1575, 1937), new MyPoint(1575, 1896), 
									 new MyPoint(1616, 1896), new MyPoint(1617, 2090), new MyPoint(376, 2097) };
		hotspotList.add(new HotspotItem(verticesSMMFFNN1, labelSMMFFNN));
		hotspotList.add(new HotspotItem(verticesSMMFFNN2, labelSMMFFNN));
		hotspotList.add(new HotspotItem(verticesSMMFFNN3, labelSMMFFNN));
		
		String labelScienzePolitiche = facoltaPrefix + "Scienze Politiche";
		Point[] verticesScienzePolitiche = { new MyPoint(3109, 1940), new MyPoint(3297, 1937), new MyPoint(3298, 2038), new MyPoint(3323, 2039), new MyPoint(3324, 2147), 
											 new MyPoint(3212, 2148), new MyPoint(3212, 2125), new MyPoint(3111, 2126) }; 
		hotspotList.add(new HotspotItem(verticesScienzePolitiche, labelScienzePolitiche));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(activity); 
		return mapView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
//		mapView = (MapView) view;
		mapView.addZoomLevel(RESIZED_IMG_WIDTH, RESIZED_IMG_HEIGHT, "map/uni_map_" + RESIZED_IMG_WIDTH + "_" + RESIZED_IMG_HEIGHT + "/tile_%row%_%col%.png", "map/uni_map_lr.png");
		
		try {
			ImageView marker = new ImageView( activity );
			marker.setImageDrawable( Drawable.createFromStream(activity.getAssets().open("map/try.png"), null) );
			double x = ((1065) * RESIZED_IMG_WIDTH) / ORIGINAL_IMG_WIDTH;
			double y = ((1616) * RESIZED_IMG_HEIGHT) / ORIGINAL_IMG_HEIGHT;
//			mapView.addMarker( marker, x, y, 0f, 0f );
			mapView.addMarkerAtZoom(marker, x, y, 5);
//			mapView.addView(marker, (int)x, (int)y);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mapView.moveToAndCenter( RESIZED_IMG_WIDTH/2, RESIZED_IMG_HEIGHT/2, true );
		
		for(final HotspotItem item : hotspotList) {
			Log.d(Utils.TAG, "Adding hotspot: " + item.getLabel());
			mapView.addHotSpot(item.getZone(), new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(Utils.TAG, "Hotspot clicked: " + item.getLabel());
					if(lastMarkerAdded != null)
						mapView.removeMarker(lastMarkerAdded);
					CustomMapMarker marker = new CustomMapMarker(activity, item.getLabel());
					mapView.addMarker( marker, item.getPoint()[0], item.getPoint()[1], -0.5f, -1.0f );
					lastMarkerAdded = marker;
				}
			});
		}
		
	}
	
	// when a marker is clicked, show a callout
	// the callout can be any view, and can have it's own event listeners
//	private View.OnClickListener markerClickListener = new View.OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			Log.d(Utils.TAG, "Item clicked");
//			// // we saved the coordinate in the marker's tag
//			// double[] point = (double[]) v.getTag();
//			// // lets center the screen to that coordinate
//			// mapView.slideToAndCenter( point[0], point[1] );
//			// // create a simple callout
//			// SampleCallout callout = new SampleCallout( v.getContext() );
//			// // add it to the view tree at the same position and offset as the marker that invoked it
//			// mapView.addCallout( callout, point[0], point[1], -0.5f, -1.0f );
//			// // a little sugar
//			// callout.transitionIn();
//		}
//	};

	@Override
	public void onPause() {
		super.onPause();
		mapView.clear();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.requestRender();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.destroy();
		mapView = null;
	}
	
	public static class MyPoint extends Point {
		public MyPoint(int x, int y) {
			this.x = (x * RESIZED_IMG_WIDTH) / ORIGINAL_IMG_WIDTH;
			this.y = (y * RESIZED_IMG_HEIGHT) / ORIGINAL_IMG_HEIGHT;
		}
	}
	
	public static class HotspotItem {
		private Poly zone;
		private String label;
		private double[] point;
		
		public HotspotItem(Point[] zoneVertices, String label) {
			this.zone = new Poly(zoneVertices);
			this.label = label;
			
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE; 
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			
			for(int i=0; i<zoneVertices.length; i++) {
				Point cVertex = zoneVertices[i];
				if(cVertex.x < minX)
					minX = cVertex.x;
				if(cVertex.x > maxX)
					maxX = cVertex.x;
				if(cVertex.y < minY)
					minY = cVertex.y;
				if(cVertex.y > maxY)
					maxY = cVertex.y;
			}
			
			point = new double[] { minX + ((maxX-minX)/2.0), minY + ((maxY-minY)/2.0) };
			Log.d(Utils.TAG, "Point is: " + point[0] + "," + point[1]);
		}
		
		public Poly getZone() {
			return zone;
		}
		
		public String getLabel() {
			return label;
		}
		
		public double[] getPoint() {
			return point;
		}
	}

}