package pt.lsts.neptusmobile;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;
import pt.lsts.neptusmobile.imc.ImcManager;
import pt.lsts.util.WGS84Utilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AccuMainActivity extends FragmentActivity {
	public static final String NAME = "MainActivity";

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;
	private ConcurrentHashMap<Integer, Marker> sysMarkers;

	ImcManager imcManager;
	// Hook imcHook;
	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Log.w(NAME, msg.toString());
			EstimatedState state = (EstimatedState) msg.obj;
			int srcId = state.getSrc();
			double[] wgs84displace = WGS84Utilities.WGS84displace(
					Math.toDegrees(state.getLat()),
					Math.toDegrees(state.getLon()), state.getHeight(),
					state.getX(), state.getY(), state.getZ());
			LatLng stateloc = new LatLng((wgs84displace[0]), wgs84displace[1]);
			double alt = wgs84displace[2];
			double speed = state.getU();
			Marker marker = sysMarkers.get(srcId);
			DecimalFormat df = new DecimalFormat("#.##");
			String snippet = "Height " + df.format(alt) + "; Speed: "
					+ df.format(speed);
			if (marker == null) {
				MarkerOptions mo =
						new MarkerOptions()
								.title(state.getSourceName())
								.snippet(snippet)
						.position(stateloc);
				addNewMarker(srcId, mo);
			}
			else{
				marker.setPosition(stateloc);
				marker.setSnippet(snippet);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		setContentView(R.layout.activity_main);
		// If we're being restored from a previous state,
		// then we don't need to do anytohing and should return or else
		// we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			return;
		}

		setUpMapIfNeeded();

		IMCProtocol proto = new IMCProtocol(5000);
		proto.addMessageListener(
				new MessageListener<MessageInfo, IMCMessage>() {
					@Override
					public void onMessage(MessageInfo info, IMCMessage msg) {
						;

						String sysName = msg.getSourceName();
						// Log.w(NAME,
						// msg.getAbbrev() + " received from "
						// + sysName);
						EstimatedState state = (EstimatedState) msg;
						uiHandler.sendMessage(uiHandler.obtainMessage(0, state));
					}
				}, "EstimatedState");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_fragment)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		// Markers
		sysMarkers = new ConcurrentHashMap<Integer, Marker>();
		// String markerKey = "Marker";
		// addNewMarker(markerKey, new MarkerOptions().position(new LatLng(0,
		// 0))
		// .title(markerKey));
		// LatLng sydney = new LatLng(-33.867, 151.206);
		// markerKey = "Sydney";
		// addNewMarker(
		// markerKey,
		// new MarkerOptions().title(markerKey)
		// .snippet("The most populous city in Australia.")
		// .position(sydney));
		// My location
		mMap.setMyLocationEnabled(true);
		// Camera
		// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
	}

	private void addNewMarker(Integer key, MarkerOptions markerOptions) {
		Marker marker = mMap.addMarker(markerOptions);
		sysMarkers.put(key, marker);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
