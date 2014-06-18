package pt.lsts.neptusmobile;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
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
	// Avoid concurrency problems between ?update thread? and destruction
	// thread
	private ConcurrentHashMap<String, Marker> markers;
	private Marker selectedSys;
	// TODO transform into DB
	private DataFragment dataFrag;
	private IMCProtocol proto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		markers = new ConcurrentHashMap<String, Marker>();
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
						// Log.w(TAG, "Got msg.");
						}
					}, "EstimatedState");
		// started = true;
	}
	
	private void loadMarkers(){
		Set<Integer> nameAllSytems = dataFrag.getimcIdAllSytems();
		Marker marker;
		ImcSystem system;
		for (Integer id : nameAllSytems) {
			system = dataFrag.getSystem(id);
			String imcName = system.getImcName();
			if (!markers.containsKey(imcName)) {
				marker = createNewMarker(imcName);
			} else {
				marker = markers.get(imcName);
			}
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
	protected void onStop() {
		super.onStop();
		// started = false;
		Log.i(TAG, "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		proto.stop();
		cleanMarkers();
		Log.i(TAG, "MapActivity is about to be destroyed.");
	}

	private void cleanMarkers() {
		// Clean links for garbage collector
		Set<String> keySet = markers.keySet();
		for (String key : keySet) {
			Marker tmp = markers.remove(key);
			tmp.remove();
		}
	}

	private void setUpMap() {
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
		setPaddingForMaps();
	}

	private void setPaddingForMaps() {
		final LinearLayout llTotal = (LinearLayout)
				findViewById(R.id.sidebar);
		final ViewTreeObserver vto = llTotal.getViewTreeObserver();
//		if (vto.isAlive()) {
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					// Padding for left hand info - set here to wait for
					// inflation of layout
					LinearLayout sidebar = (LinearLayout) findViewById(R.id.sidebar);
				// Log.i(TAG, "sidebar has " + sidebar.getWidth() + " px");
					mMap.setPadding(sidebar.getWidth() + 10, 0, 0, 0);
				    llTotal.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	                
				}
			});
//		}
	}
	
	// TODO transform into service
	private final Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// try {
			EstimatedState state = (EstimatedState) msg.obj;
			int imcId = state.getSrc();
			ImcSystem system = dataFrag.getSystem(imcId);
			Marker marker = null;
			String sourceName = state.getSourceName();
			if (system == null) {
				// Really a new system
				// add both in markers and dataFrag
				// if (started)
					marker = createNewMarker(sourceName);
				system = new ImcSystem(state);
				dataFrag.addSystem(system);
				Log.w(TAG, "Adding system " + sourceName
						+ " nothing in dataFrag with id " + imcId);
			} else {
				// Known system
				String oldSysName = system.getImcName();

				system.update(state);
				// if (started) {
					marker = markers.get(oldSysName);
					// After getting the old name update the data
					// Log.w(TAG, "Known system " + sourceName);
					if (!oldSysName.equals(sourceName)) {
						// First time receiving the name
						// Marker old = markers.remove(oldSysName);
						// old.remove();
						// markers.put(sourceName, marker);

						marker.setTitle(sourceName);
						Log.w(TAG, "First time receiving the name "
								+ sourceName);
					}
				// }
			}
			// In any case the data on the system must be updated
			if (marker != null) {
				system.updateMarker(marker);
				if (selectedSys != null
						&& sourceName.equals(selectedSys.getTitle())) {
					updateLabels(system);
				}
			}
			// } catch (Exception e) {
			// Log.e(TAG, e.toString());
			// }
		}

	};

	private Marker createNewMarker(String sourceName) {
		Log.i(TAG, "Creating marker for " + sourceName);
		Marker marker;
		marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
		marker.setFlat(true);
		marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sys));
		marker.setAnchor((float) 0.5, (float) 0.5);
		markers.put(sourceName, marker);
		return marker;
	}
	
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