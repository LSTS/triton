package pt.lsts.neptusmobile.imc;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import pt.lsts.imc.net.UDPTransport;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ImcService extends IntentService
// implements
// MessageListener<MessageInfo, IMCMessage>
{
	public static final String NAME = "ImcService";

	private int localId;
	private final boolean commActive = false;
	private UDPTransport announceListener, comm;
	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public ImcService() {
		super("ImcService");
	}

	// @Override
	// public int onStartCommand(Intent intent, int flags, int startId) {
	// int superOutput = super.onStartCommand(intent, flags, startId);
	// String localIp = getLocalIpAddress();
	// int lastIpNumber;
	// if (localIp == null)
	// lastIpNumber = 0;
	// else
	// lastIpNumber = Integer.valueOf(localIp.split("\\.")[3]);
	//
	// localId = 0x4100 | lastIpNumber;
	// Log.w(NAME, "System ID: " + localId);
	// startImcComms();
	// return superOutput;
	// }

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

	// public void startImcComms() {
	// if (!commActive) {
	// Log.w("IMCManager", "Starting Comms");
	// announceListener = new UDPTransport("224.0.75.69", 30100);
	// comm = new UDPTransport(6001, 1);
	// announceListener.addMessageListener(this);
	// comm.addMessageListener(this);
	//
	// announceListener.setIsMessageInfoNeeded(false);
	// comm.setIsMessageInfoNeeded(false);
	// commActive = true;
	// }
	// }
	//
	// public void killComms() {
	// if (commActive) {
	// Log.w("IMCManager", "Killing Comms");
	// announceListener.removeMessageListener(this);
	// comm.removeMessageListener(this);
	// announceListener.stop();
	// comm.stop();
	// announceListener = null;
	// comm = null;
	//
	// // subscribers.clear();
	//
	// commActive = false;
	// }
	// }

	// @Override
	// public void onDestroy() {
	// super.onDestroy();
	// killComms();
	// }
	//
	// @Override
	// public void onMessage(MessageInfo arg0, IMCMessage arg1) {
	// Log.w(NAME, "Got message " + arg1.toString());
	//
	// }

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.w(NAME, ">>>handlingIntent()");
	}
}
