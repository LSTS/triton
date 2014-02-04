package pt.lsts.neptusmobile.imc;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import pt.lsts.imc.Announce.SYS_TYPE;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.imc.net.UDPTransport;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;
import android.util.Log;

public class ImcManager {
	public static final String NAME = "ImcManager";
	private final ConcurrentHashMap<String, Sys> systems;

	private int localId;
	private boolean commActive = false;
	private UDPTransport announceListener, comm;

	public ImcManager(MessageListener<MessageInfo, IMCMessage> mapHook) {
		systems = new ConcurrentHashMap<String, Sys>();
		createUdpSockets();
		// Internal listener to handle announces
		announceListener.addMessageListener(new Hook());
		// Regular communication
		comm.addMessageListener(mapHook);
	}

	/**
	 * TODO this should be redone so that more fragments use the same connection
	 */
	public void createUdpSockets() {
		if (!commActive) {
			Log.w(NAME, "Starting Comms");
			String localIp = getLocalIpAddress();
			int lastIpNumber;
			if (localIp == null)
				lastIpNumber = 0;
			else
				lastIpNumber = Integer.valueOf(localIp.split("\\.")[3]);
			// Use mask with the last octect of ip
			localId = 0x4100 | lastIpNumber;
			Log.w(NAME, "System ID: " + localId);
			String multicastAddress = "224.0.75.69";
			// has to be one between 30100 and 30104
			int listeningPort = 30100;
			announceListener = new UDPTransport(multicastAddress, listeningPort);
			int ccuPortConvention = 6001;
			comm = new UDPTransport(ccuPortConvention, 1);
			announceListener.setIsMessageInfoNeeded(true);
			comm.setIsMessageInfoNeeded(false);
			commActive = true;
		}
	}

	public void destroyUdpSockets(MessageListener<MessageInfo, IMCMessage> hook) {
		if (commActive) {
			Log.w(NAME, "Killing Comms");
			announceListener.removeMessageListener(hook);
			comm.removeMessageListener(hook);
			announceListener.stop();
			comm.stop();
			announceListener = null;
			comm = null;
			commActive = false;
		}
	}

	/**
	 * Method responsible for effectively giving the order to the UDPTransport
	 * to send the message
	 */
	public void sendStartingAnnouce(String address, int port) {
		IMCMessage msg = IMCDefinition.getInstance().create("Announce");
		IMCProtocol.announce("accu-" + localId, localId, SYS_TYPE.CCU, comm);
		Log.w(NAME, "Sending msg to " + address + ":" + port);
		try {
			msg.getHeader().setValue("src", localId);
			msg.getHeader().setValue("timestamp",
					System.currentTimeMillis() / 1000);
			comm.sendMessage(address, port, msg);
		} catch (Exception e) {
			e.printStackTrace();
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

	/**
	 * 
	 * @param msg IMCMessage containing the Announce to extract the address from.
	 * @return The node address for IMC communications.
	 */
	public static String[] getAnnounceIMCAddressPort(IMCMessage msg)
	{
		for(String s: getAnnounceService(msg,"imc+udp"))
		{
			try {
				if(InetAddress.getByName(s.split(":")[0]).isReachable(50))
				{
					String foo[] = s.split(":");
					String res[] = { foo[0], foo[1].substring(0, foo[1].length()-1)};
					return res;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * This function return the full address on a service specified in the Announce message of a node
	 * @param msg the message to be inspected
	 * @param service The name of the service to be looked for
	 * @return Returns a string like &lt;address:port&gt;, null if msg not an announce or service doesnt exist.
	 */
	public static ArrayList<String> getAnnounceService(IMCMessage msg, String service)
	{
		String str = msg.getString("services");
		ArrayList<String> list = new ArrayList<String>();
		String services[] = str.split(";");
		for(String s: services)
		{
			String foo[] = s.split("://");
			if(foo[0].equals(service))
				list.add(foo[1]);
		}
		return list;
	}
	


	class Hook implements MessageListener<MessageInfo, IMCMessage> {
		private static final boolean DEBUG = true;

		@Override
		public void onMessage(MessageInfo arg0, IMCMessage msg) {
			// // Deal with announces
			// String sysName = msg.getString("sys_name");
			// String msgType = msg.getAbbrev();
			//
			// // Process Announce routine
			// if (msg.getAbbrev().equalsIgnoreCase("Announce")) {
			// // If System already exists in host list
			// // if (containsSysName(msg.getString("sys_name"))) {
			// if (systems.containsKey(sysName)) {
			// // Sys s = findSysByName(msg.getString("sys_name"));
			// Sys s = systems.get(sysName);
			//
			// if (DEBUG)
			// Log.i("Log",
			// "Repeated announce from: "
			// + msg.getString("sys_name"));
			//
			// if (!s.isConnected()) {
			// s.lastMessageReceived = System.currentTimeMillis();
			// s.setConnected(true);
			// // changeList(sysList);
			// // Send an Heartbeat to resume communications in case of
			// // system prior crash
			// try {
			// send(s.getAddress(),
			// s.getPort(),
			// IMCDefinition.getInstance().create(
			// "Heartbeat"));
			// } catch (Exception e1) {
			// e1.printStackTrace();
			// }
			// }
			//
			// return;
			// }
			// // If Service IMC+UDP doesnt exist or isnt reachable, return...
			// if (getAnnounceService(msg, "imc+udp") == null) {
			// Log.e(NAME,
			// msg.getString("sys_name")
			// + " node doesn't have IMC protocol or isn't reachable");
			// Log.e(NAME, msg.toString());
			// return;
			// }
			// String[] addrAndPort = getAnnounceIMCAddressPort(msg);
			// if (addrAndPort == null) {
			// Log.e(NAME,
			// "Unreachable System - " + msg.getString("sys_name"));
			// return;
			// }
			// // If Not include it
			// Log.i("Log", "Adding new System");
			// Sys s = new Sys(addrAndPort[0],
			// Integer.parseInt(addrAndPort[1]),
			// msg.getString("sys_name"),
			// (Integer) msg.getHeaderValue("src"),
			// msg.getString("sys_type"), true, false);
			//
			// // sysList.add(s);
			// systems.put(sysName, s);
			//
			// // Update the list of available Vehicles
			// // changeList(sysList);
			//
			// // Send an Heartbeat to register as a node in the vehicle (maybe
			// // EntityList?)
			// try {
			// IMCMessage m = IMCDefinition.getInstance().create(
			// "Heartbeat");
			// m.getHeader().setValue("src", 0x4100);
			// send(s.getAddress(), s.getPort(), m);
			// // sendMessage(s.getAddress(), s.getPort(), m);
			// } catch (Exception e1) {
			// e1.printStackTrace();
			// }
			// }

			// -----------------------------------------------------------------------

			// If Service IMC+UDP doesnt exist or isnt reachable, return...
			// if (getAnnounceService(msg, "imc+udp") == null) {
			// Log.e(NAME, sysName
			// + " node doesn't have IMC protocol or isn't reachable");
			// Log.e(NAME, msg.toString());
			// return;
			// }
			// String[] addrAndPort = getAnnounceIMCAddressPort(msg);
			// String[] addrAndPort = getAddress(arg0, msg);
			// if (addrAndPort == null) {
			// Log.e(NAME, "No address for " + sysName);
			// return;
			// }
			// Log.w(NAME, "Received " + msgType + " from " + sysName + "@"
			// + addrAndPort[0] + ":" + addrAndPort[1]);
			// Sys system = systems.get(sysName);
			// if (systems.containsKey(sysName)) {
			// if (DEBUG)
			// Log.w("Log", "Repeated announce from " + sysName);
			// if (system.isConnected()) {
			// return;
			// } else {
			// system.lastMessageReceived = System.currentTimeMillis();
			// system.setConnected(true);
			// }
			// } else {
			// // If Not include it
			// system = new Sys(addrAndPort[0],
			// Integer.parseInt(addrAndPort[1]), sysName,
			// (Integer) msg.getHeaderValue("src"),
			// msg.getString("sys_type"), true, false);
			// systems.put(sysName, system);
			// Log.w("Log", "Added new System");
			// }
			// // Send an Heartbeat to register as a node in the vehicle
			// try {
			// sendStartingAnnouce(system.getAddress(), system.getPort());
			// } catch (Exception e1) {
			// e1.printStackTrace();
			// }
		}

		public String[] getAddress(MessageInfo info, IMCMessage msg) {
			String sia = info.getPublisherInetAddress();
			InetSocketAddress[] retId = getImcIpsPortsFromMessage(
					msg.getString("services"), "imc+udp");
	        String hostUdp = "";
	        for (InetSocketAddress add : retId) {
	            if (sia.equalsIgnoreCase(add.getAddress().getHostAddress())) {
	                hostUdp = add.getAddress().getHostAddress();
					String[] res = { hostUdp, add.getPort() + "" };
					return res;
	            }
	        }
			return null;
		}
	}

	/**
	 * Method responsible for effectively giving the order to the UDPTransport
	 * to send the message
	 */
	public void send(String address, int port, IMCMessage msg) {
		try {
			// FIXME Fill the header of the messages here
			fillHeader(msg);
			comm.sendMessage(address, port, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillHeader(IMCMessage msg) {
		msg.getHeader().setValue("src", localId);
		msg.getHeader()
				.setValue("timestamp", System.currentTimeMillis() / 1000);
	}

	public InetSocketAddress[] getImcIpsPortsFromMessage(String services,
			String scheme) {
		// NeptusLog.pub().info("<###> "+services);
		String[] listSer = services.split(";");
		LinkedList<String> ipList = new LinkedList<String>();
		LinkedList<Integer> portList = new LinkedList<Integer>();
		for (String rs : listSer) {
			try {
				if (!rs.trim().startsWith(scheme + ":"))
					continue;

				URI url1 = URI.create(rs.trim());
				String host = url1.getHost();
				int port = url1.getPort();
				if (port == -1 || port == 0)
					continue;
				// boolean reachable =
				// NetworkInterfacesUtil.testForReachability(host);
				// if (!reachable)
				// continue;
				portList.add(port);
				ipList.add(host);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		InetSocketAddress[] addL = new InetSocketAddress[portList.size()];
		for (int i = 0; i < portList.size(); i++) {
			addL[i] = new InetSocketAddress(ipList.get(i), portList.get(i));
		}
		return addL;
	}
}
