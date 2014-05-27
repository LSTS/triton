package pt.lsts.neptusmobile.data;

import java.text.DecimalFormat;

import pt.lsts.imc.EstimatedState;
import pt.lsts.util.WGS84Utilities;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class ImcSystem {
	private String imcName;
	private float height;
	private float speed;
	private LatLng location;
	private float rotation;

	public ImcSystem (EstimatedState msg){
		imcName = msg.getSourceName();
		update(msg);
	}
	
	// For creating dummy data
	public ImcSystem(String imcName, float height, float speed,
			LatLng location, float rotation) {
		super();
		this.imcName = imcName;
		this.height = height;
		this.speed = speed;
		this.location = location;
		this.rotation = rotation;
	}

	public void update(EstimatedState state){
		// Location
		double[] wgs84displace = WGS84Utilities.WGS84displace(
				Math.toDegrees(state.getLat()),
				Math.toDegrees(state.getLon()), state.getHeight(),
				state.getX(), state.getY(), state.getZ());
		location = new LatLng((wgs84displace[0]), wgs84displace[1]);
		// Height
		this.height = (float)Math.abs(wgs84displace[2]);
		// Speed
		this.speed = (float)state.getU();
		// Rotation
		rotation = (float) Math.toDegrees(state.getPsi());
	}
	
	public String getName(){
		return imcName;
	}

	public void updateMarker(Marker marker) {
		marker.setTitle(imcName);
		marker.setPosition(location);
		marker.setRotation(rotation);
		DecimalFormat df = new DecimalFormat("#.##");
		String snippet = "Height " + df.format(height) + "; Speed: "
				+ df.format(speed) + "; Heading:" + rotation;
		marker.setSnippet(snippet);
	}

	public float getHeight() {
		return height;
	}
	

	public String getImcName() {
		return imcName;
	}

	public float getSpeed() {
		return speed;
	}
}
