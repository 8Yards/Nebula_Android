package org.nebula.rtpClient;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class RTPClient extends RTPSession implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8698305335360954254L;

	public RTPClient(int rtpPort, int rtpcPort, RTPAppIntf intf, boolean receiver) throws SocketException {
		super(new DatagramSocket(rtpPort), new DatagramSocket(rtpcPort));
		start(intf, receiver);
	}

	public RTPClient(DatagramSocket rtpSocket, DatagramSocket rtcpSocket, RTPAppIntf intf, boolean receiver) {
		super(rtpSocket, rtcpSocket);
		start(intf, receiver);
	}
	
	private void start(RTPAppIntf intf, boolean receiver) {
		if(receiver)
			this.naivePktReception(true);
		this.RTPSessionRegister(intf, null, null);
	}
}
