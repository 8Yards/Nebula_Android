package org.nebula.sipClient;

import javax.sip.RequestEvent;

public interface SIPInterface {
	public void processRequest(RequestEvent requestReceivedEvent);
}
