package pt.lsts.neptusmobile.imc;

import java.text.DecimalFormat;

import pt.lsts.imc.EstimatedState;
import pt.lsts.neptusmobile.R;
import pt.lsts.util.WGS84Utilities;
import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Sys {
	
	private final Marker marker;
	private float height;
	private float speed;
	
	public Sys(Marker marker) {
		this.marker = marker;
		this.marker.setFlat(true);
		this.marker.setIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_sys));
		this.marker.setAnchor((float)0.5, (float)0.5);
	}

	public void setAsSelectedVehicle() {
		marker.setIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_main_sys));
	}

	public void setAsUnselectedVehicle() {
		marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sys));
	}


	@SuppressLint("UseValueOf")
	public void update(EstimatedState state) {
		// Height
		double[] wgs84displace = WGS84Utilities.WGS84displace(
				Math.toDegrees(state.getLat()),
				Math.toDegrees(state.getLon()), state.getHeight(),
				state.getX(), state.getY(), state.getZ());
		LatLng stateloc = new LatLng((wgs84displace[0]), wgs84displace[1]);
		this.height = new Float(Math.abs(wgs84displace[2]));
		// Speed
		this.speed = new Float(state.getU());
		// Marker
		marker.setTitle(state.getSourceName());
		marker.setPosition(stateloc);
		Float angDeg = (float) Math.toDegrees(state.getPsi());
		marker.setRotation(angDeg);
		DecimalFormat df = new DecimalFormat("#.##");
		String snippet = "Height " + df.format(height) + "; Speed: "
				+ df.format(speed) + "; Heading:" + angDeg;
		marker.setSnippet(snippet);
	}

	public void fillTestData(String title, LatLng coord, int rotation,
			float heightIn, float speedIn) {
		marker.setTitle(title);
		marker.setPosition(coord);
		marker.setRotation(rotation);
		marker.setSnippet("Height " + speedIn + "m; Speed: " + speedIn + "m/s");
		height = heightIn;
		speed = speedIn;
	}

	public float getHeight() {
		return height;
	}

	public float getSpeed() {
		return speed;
	}

	public String getName() {
		return marker.getTitle();
	}
}
