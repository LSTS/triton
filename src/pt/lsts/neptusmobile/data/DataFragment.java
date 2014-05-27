package pt.lsts.neptusmobile.data;

import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

public class DataFragment extends Fragment{
	public static final String TAG = "DataFragment";
	private HashMap<String, ImcSystem> knownSystems;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "DataFragment onCreateView.");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "DataFragment onAttach.");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "DataFragment onActivityCreated.");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		knownSystems = new HashMap<String, ImcSystem>();
		addDummySystems();
		setRetainInstance(true);
		Log.i(TAG, "DataFragment onCreate.");
	}
	
	public void addSystem(ImcSystem sys){
		knownSystems.put(sys.getName(), sys);
	}

	public ImcSystem getSystem(String sourceName) {
		return knownSystems.get(sourceName);
	}

	public Set<String> getNameAllSytems(){
		return knownSystems.keySet();
	}

	private void addDummySystems(){
		// Test X8-00
		String title = "Test X8-00";
		LatLng coord = new LatLng(0, 0);
		int rotation = 90;
		float heightIn = 100f;
		float speedIn = 18.2f;
		ImcSystem sys = new ImcSystem(title, heightIn, speedIn, coord, rotation);
		knownSystems.put(title, sys);
		// Test X8-02
		title = "Test X8-02";
		coord = new LatLng(0, 20.5);
		rotation = 45;
		heightIn = 180f;
		speedIn = 15.3f;
		sys = new ImcSystem(title, heightIn, speedIn, coord, rotation);
		knownSystems.put(title, sys);
	}

}
