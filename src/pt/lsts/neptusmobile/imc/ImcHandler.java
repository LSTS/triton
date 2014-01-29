package pt.lsts.neptusmobile.imc;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.UDPTransport;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;
import android.util.Log;

public class ImcHandler implements MessageListener<MessageInfo, IMCMessage> {

	private static final String NAME = "ImcHandler";
	private int localId;
	private boolean commActive = false;
	private UDPTransport announceListener, comm;

	public ImcHandler() {
		startImcComms();
	}

	@Override
	public void onMessage(MessageInfo arg0, IMCMessage arg1) {
		Log.w(NAME, "ImcMessage " + arg1.toString());
	}

	private void startImcComms() {
		String localIp = getLocalIpAddress();
		int lastIpNumber;
		if (localIp == null)
			lastIpNumber = 0;
		else
			lastIpNumber = Integer.valueOf(localIp.split("\\.")[3]);

		localId = 0x4100 | lastIpNumber;
		Log.w(NAME, "System ID: " + localId);
		// -------------------------------------------------------------------------
		if (!commActive) {
			Log.w("IMCManager", "Starting Comms");
			announceListener = new UDPTransport("224.0.75.69", 30100);
			comm = new UDPTransport(6001, 1);
			announceListener.addMessageListener(this);
			comm.addMessageListener(this);

			announceListener.setIsMessageInfoNeeded(false);
			comm.setIsMessageInfoNeeded(false);
			commActive = true;
		}
	}

	public void killComms() {
		if (commActive) {
			Log.w("IMCManager", "Killing Comms");
			announceListener.removeMessageListener(this);
			comm.removeMessageListener(this);
			announceListener.stop();
			comm.stop();
			announceListener = null;
			comm = null;
			commActive = false;
		}
	}

	/**
	 * Android function to retrieve the IP independent of the connection used
	 * (3G or WiFi)
	 * 
	 * @return string containing the IP
	 */
	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.w(NAME, ex.toString());
		}
		return null;
	}

}