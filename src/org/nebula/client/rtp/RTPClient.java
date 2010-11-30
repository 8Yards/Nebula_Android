/*
 * author - michel
 * 
 */
package org.nebula.client.rtp;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class RTPClient extends RTPSession {
	private static final long serialVersionUID = -8698305335360954254L;

	public RTPClient(int rtpPort, int rtpcPort) throws SocketException {
		super(new DatagramSocket(rtpPort), new DatagramSocket(rtpcPort));
	}
}
