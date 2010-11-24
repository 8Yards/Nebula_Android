/*
 * author: michel hognerund
 */
package org.nebula.client.sip;

import javax.sip.RequestEvent;

public interface SIPInterface {
	public void processRequest(RequestEvent requestReceivedEvent);
}
