package pt.lsts.neptusmobile.imc;

import java.util.concurrent.ConcurrentHashMap;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.net.UDPTransport;
import pt.lsts.neptus.messages.listener.MessageInfo;
import pt.lsts.neptus.messages.listener.MessageListener;

public class ImcManager {
	public static final String NAME = "ImcManager";
	private final ConcurrentHashMap<String, Sys> systems;

	private UDPTransport announceListener, comm;

	public ImcManager(MessageListener<MessageInfo, IMCMessage> mapHook) {
		systems = new ConcurrentHashMap<String, Sys>();
		// Internal listener to handle announces
		announceListener.addMessageListener(new Hook());
		// Regular communication
		comm.addMessageListener(mapHook);
	}


	class Hook implements MessageListener<MessageInfo, IMCMessage> {
		private static final boolean DEBUG = true;

		@Override
		public void onMessage(MessageInfo arg0, IMCMessage msg) {

		}
	}
}
