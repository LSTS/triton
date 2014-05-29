package pt.lsts.neptusmobile.data;

import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

@SuppressLint("UseSparseArrays")
public class DataFragment extends Fragment{
	public static final String TAG = "DataFragment";
	private HashMap<Integer, ImcSystem> knownSystems;
	
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
		knownSystems = new HashMap<Integer, ImcSystem>();
		addDummySystems();
		setRetainInstance(true);
		Log.i(TAG, "DataFragment onCreate.");
	}
	
	public void addSystem(ImcSystem sys){
		knownSystems.put(sys.getImcId(), sys);
	}

	public ImcSystem getSystem(int imcId) {
		return knownSystems.get(imcId);
	}

	public Set<Integer> getimcIdAllSytems() {
		return knownSystems.keySet();
	}

	private void addDummySystems(){
		// Test X8-00
		String title = "Test X8-00";
		LatLng coord = new LatLng(0, 0);
		int id = 99;
		int rotation = 90;
		float heightIn = 100f;
		float speedIn = 18.2f;
		ImcSystem sys = new ImcSystem(id, title, heightIn, speedIn, coord,
				rotation);
		knownSystems.put(id, sys);
		// Test X8-02
		title = "Test X8-02";
		id = 98;
		coord = new LatLng(0, 20.5);
		rotation = 45;
		heightIn = 180f;
		speedIn = 15.3f;
		sys = new ImcSystem(id, title, heightIn, speedIn, coord, rotation);
		knownSystems.put(id, sys);
	}

	public ImcSystem getSystem(String title) {
		Set<Integer> allImcIds = knownSystems.keySet();
		for (Integer imcId : allImcIds) {
			ImcSystem imcSystem = knownSystems.get(imcId);
			if (imcSystem.getImcName().equals(title))
				return imcSystem;
		}
		return null;
	}

}
