package pt.lsts.neptusmobile.imc;

import java.text.DecimalFormat;

import pt.lsts.imc.EstimatedState;
import pt.lsts.neptusmobile.R;
import pt.lsts.util.WGS84Utilities;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Sys {
	
	private final Marker marker;
	private final LatLng lastPos;
	
	public Sys(Marker marker) {
		this.marker = marker;
		lastPos = marker.getPosition();
		this.marker.setFlat(true);
		this.marker.setIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_system));
		this.marker.setAnchor((float)0.5, (float)0.5);
	}

	public void update(EstimatedState state) {
		double[] wgs84displace = WGS84Utilities.WGS84displace(
				Math.toDegrees(state.getLat()),
				Math.toDegrees(state.getLon()), state.getHeight(),
				state.getX(), state.getY(), state.getZ());
		LatLng stateloc = new LatLng((wgs84displace[0]), wgs84displace[1]);
		double alt = wgs84displace[2];
		double speed = state.getU();
		DecimalFormat df = new DecimalFormat("#.##");
		marker.setTitle(state.getSourceName());
		marker.setPosition(stateloc);
		
		Float angDeg = (float) Math.toDegrees(state.getPsi());
		marker.setRotation(angDeg);
		String snippet = "Height " + df.format(alt) + "; Speed: "
				+ df.format(speed) + "; Heading:" + angDeg;
		marker.setSnippet(snippet);
	}

	public void fillTestData() {
		marker.setTitle("Test vehicle");
		marker.setPosition(new LatLng(0, 0));
		marker.setRotation(90);
		marker.setSnippet("Height 100m; Speed: 18m/s");
	}

}
