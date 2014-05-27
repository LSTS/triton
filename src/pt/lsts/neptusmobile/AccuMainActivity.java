package pt.lsts.neptusmobile;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;
import pt.lsts.neptusmobile.data.DataFragment;
import pt.lsts.neptusmobile.data.ImcSystem;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AccuMainActivity extends FragmentActivity{
	public static final String TAG = "MainActivity";

	private static final String DATA_FRAG_TAG = "data";
	
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;
	private HashMap<String, Marker> markers;
	private Marker selectedSys;
	// TODO transform into DB
	private DataFragment dataFrag;
	private IMCProtocol proto;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		markers = new HashMap<String, Marker>();
		FragmentManager fm = getSupportFragmentManager();
		// If we're being restored from a previous state,
		// then we don't need to add fragments or else
		// we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			Log.i(TAG, "Loading data fragment.");
			// Load fragment
	        dataFrag = (DataFragment) fm.findFragmentByTag(DATA_FRAG_TAG);
		}
		else{
			Log.i(TAG, "Creating data fragment.");
			dataFrag = new DataFragment();
			fm.beginTransaction().add(dataFrag, DATA_FRAG_TAG).commit();
		}
		setUpMapIfNeeded();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
		loadMarkers();
			proto = new IMCProtocol(5000);
			proto.addMessageListener(
					new MessageListener<MessageInfo, IMCMessage>() {
						@Override
						public void onMessage(MessageInfo info, IMCMessage msg) {
							EstimatedState state = (EstimatedState) msg;
							uiHandler.sendMessage(uiHandler.obtainMessage(0, state));
							Log.w(TAG, "Got msg.");
						}
					}, "EstimatedState");
	}
	
	private void loadMarkers(){
		Set<String> nameAllSytems = dataFrag.getNameAllSytems();
		Marker marker;
		ImcSystem system;
		for (String name : nameAllSytems) {
			marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
			markers.put(name, marker);
			system = dataFrag.getSystem(name);
			system.updateMarker(marker);
			setAsUnselectedVehicle(marker);
		}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		proto.stop();
		Log.i(TAG, "MapActivity is about to be destroyed.");
	}

	private void setUpMap() {
		// Padding for left hand info
		mMap.setPadding(300, 0, 0, 0);
		// My location
		mMap.setMyLocationEnabled(true);
		// Camera
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				ImcSystem sys = dataFrag.getSystem(marker.getTitle());
				updateLabels(sys);
				toggleMarkerSelection(marker, sys);
				return true;
			}

			private void toggleMarkerSelection(Marker marker, ImcSystem sys) {
				if (selectedSys != null) {
					setAsUnselectedVehicle(selectedSys);
				}
				setAsSelectedVehicle(marker);
				selectedSys = marker;
			}

		});
	}
	
	// TODO transform into service
	private final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				EstimatedState state = (EstimatedState) msg.obj;
				String sourceName = state.getSourceName();
				Marker marker = markers.get(sourceName);
				ImcSystem system;
				if (marker == null) {
					marker = mMap.addMarker(new MarkerOptions()
							.position(new LatLng(0, 0)));
					markers.put(sourceName, marker);
					system = new ImcSystem(state);
					dataFrag.addSystem(system);
					Log.w(TAG, "Adding system " + sourceName);
				}
				else{
					system = dataFrag.getSystem(sourceName);
					system.update(state);
				}
				system.updateMarker(marker);
				if (selectedSys != null
						&& sourceName.equals(selectedSys.getTitle())) {
					updateLabels(system);
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	};
	
	private void updateLabels(ImcSystem sys) {
		DecimalFormat df = new DecimalFormat("#.##");
		// Update text
		TextView textV = (TextView) findViewById(R.id.vehicle_name);
		textV.setText(sys.getName());
		textV = (TextView) findViewById(R.id.vehicle_height);
		textV.setText(df.format(sys.getHeight()) + "m");
		textV = (TextView) findViewById(R.id.vehicle_speed);
		textV.setText(df.format(sys.getSpeed()) + "m/s");
	}

	private void setAsSelectedVehicle(Marker marker) {
		marker.setIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_main_sys));
	}

	private void setAsUnselectedVehicle(Marker marker) {
		marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sys));
	}
}