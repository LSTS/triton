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
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Class that aggregates IMC messaging functions and delivers message to the subscribers
 * @author jqcorreia
 *
 */
public class IMCManagerMsgListener implements MessageListener<MessageInfo, IMCMessage>
{
	public static final String TAG = "IMCManager";
	
	
	UDPTransport announceListener, comm;
	
	final static boolean LOG_DEBUG=false; // Message Logging flag
	public boolean commActive = false;
	int localId;
	
	public IMCManagerMsgListener()
	{
		// startComms(); // Commented out means you have to explicitly call startComms to initialize sockets
		String localIp = getLocalIpAddress();
		int lastIpNumber;
		if(localIp == null) 
			lastIpNumber = 0;
		else
			lastIpNumber = Integer.valueOf(localIp.split("\\.")[3]);
			
		localId = 0x4100 | lastIpNumber;
		Log.d(TAG,"System ID: "+localId);
	}
	
	/**
	 * Android function to retrieve the IP independent of the connection used
	 * (3G or WiFi)
	 * 
	 * @return string containing the IP
	 */
	public String getLocalIpAddress() {
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
			Log.e("LocalIpGetter", ex.toString());
		}
		return null;
	}
	
    /**
     * Method that delivers the message to the various subscribers.
     * Leaving the task of seeing which is the Active Sys to the various components for low level
     * flexibility.
     * @param message Message to be delivered
     */
	public void processMessage(IMCMessage message)
	{
		try
		{
			if (LOG_DEBUG)
				Log.v(TAG, message.toString()); 

			int id = (Integer) message.getHeaderValue("mgid");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.e("ERROR","ERROR IN MESSAGE PROCESSING, IGNORING MESSAGE: "+e.getMessage());
//			Log.e("ERROR",message.toString());
			return;
		}
	}
	
	Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		switch(msg.what){
		     case 1:
		            /*Refresh UI*/
		            processMessage((IMCMessage)msg.obj);
		            break;
		   }
		}
	};
	@Override
	public void onMessage(MessageInfo info, IMCMessage message) {
		handle.sendMessage(Message.obtain(handle, 1, message));
	}
	
	public void killComms()
	{
		if(commActive)
		{
			Log.i("IMCManager", "Killing Comms");
			announceListener.removeMessageListener(this);
			comm.removeMessageListener(this);
			announceListener.stop();
			comm.stop();
			announceListener = null;
			comm = null;

//			subscribers.clear();

			commActive = false;
		}
	}
	public void startComms()
	{
		if (!commActive) {
			Log.i("IMCManager", "Starting Comms");
			announceListener = new UDPTransport("224.0.75.69", 30100);
			comm = new UDPTransport(6001, 1);
			announceListener.addMessageListener(this);
			comm.addMessageListener(this);

			announceListener.setIsMessageInfoNeeded(false);
			comm.setIsMessageInfoNeeded(false);
			commActive = true;
		}
	}
	
	public UDPTransport getComm()
	{
		return comm;
	}
	public UDPTransport getListener()
	{
		return announceListener;
	}
	public int getLocalId()
	{
		return localId;
	}

}
