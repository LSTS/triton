package pt.lsts.neptusmobile;

import java.util.concurrent.ConcurrentHashMap;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;
import pt.lsts.neptusmobile.imc.Sys;
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
	private ConcurrentHashMap<Integer, Sys> sysMarkers;

	// ImcManager imcManager;
	// Hook imcHook;
	private final Handler uiHandler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			Log.w(NAME, msg.toString());
			EstimatedState state = (EstimatedState) msg.obj;
			int srcId = state.getSrc();
			Sys system = sysMarkers.get(srcId);
			if (system == null) {
				Marker marker = mMap.addMarker(new MarkerOptions()
						.position(new LatLng(0, 0)));
				system = new Sys(marker);
				sysMarkers.put(srcId, system);
			}
			system.update(state);

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
						EstimatedState state = (EstimatedState) msg;
						uiHandler.sendMessage(uiHandler.obtainMessage(0, state));
						Log.w(NAME, "Got msg.");
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
		sysMarkers = new ConcurrentHashMap<Integer, Sys>();
		// Test
		Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(
				0, 0)));
		Sys sys = new Sys(marker);
		sys.fillTestData();
		sysMarkers.put(24, sys);
		// My location
		mMap.setMyLocationEnabled(true);
		// Camera
		// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
