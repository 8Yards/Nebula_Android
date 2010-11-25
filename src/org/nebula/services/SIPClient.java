/*
 * author: michel hognurand
 * rearchitecture: prajwol kumar nakarmi, michel hognurand
 * 
 * version 1 - basic signalling
 * version 2 - extending Service
 */

package org.nebula.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.nebula.client.sip.SIPInterface;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;
import org.nebula.utils.Utils;

/**
 * Class for the SIP Client
 */
public class SIPClient implements SipListener {
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;

	private static SipProvider sipProvider;
	private static SipStack sipStack;
	private SIPInterface sipint;
	private MyIdentity myIdentity;

	private ContactHeader contactHeader;
	private ClientTransaction Tid;
	private Dialog dialog;

	private Request ackRequest;// Save the created ACK request, to respond to
	// retransmitted 2xx
	private Response response;

	private ListeningPoint localUDPListeningPoint;
	private String transport = "udp";

	public SIPClient(SIPInterface sipint) throws Exception {
		this.sipint = sipint;
		myIdentity = NebulaApplication.getInstance().getMyIdentity();

		SipFactory sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");

		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "SipClient");

		// Create SipStack object
		sipStack = sipFactory.createSipStack(properties);

		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

		localUDPListeningPoint = sipStack.createListeningPoint(myIdentity
				.getMyIP(), myIdentity.getMySIPPort(), transport);
		sipProvider = sipStack.createSipProvider(localUDPListeningPoint);
		sipProvider.addSipListener(this);

		contactHeader = createContactHeader();
	}

	private ContactHeader createContactHeader() throws ParseException {
		SipURI contactURI = addressFactory.createSipURI(myIdentity
				.getMyUserName(), myIdentity.getSipServerIP());
		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(myIdentity.getMyUserName());

		return headerFactory.createContactHeader(contactAddress);
	}

	/*
	 * Sends a SIP request
	 * 
	 * @param request The SIP request to be sent
	 * 
	 * @result response SIP response
	 */
	public synchronized Response send(Request request) throws Exception {
		Request r = (Request) request.clone();
		Tid = sipProvider.getNewClientTransaction(request);
		dialog = Tid.getDialog();

		this.response = null;
		Tid.sendRequest();
		wait(5000);// TODO: check this

		if (this.response == null) {
			throw new Exception("Response timeout");
		}

		if (response.getStatusCode() == Response.UNAUTHORIZED) {
			String nonce;
			String realm;

			String ha = response.getHeader("WWW-Authenticate").toString();
			Pattern p = Pattern.compile("nonce=\"(.*?)\"");
			Matcher m = p.matcher(ha);
			if (m.find()) {
				nonce = m.group(1);

				p = Pattern.compile("realm=\"(.*?)\"");
				m = p.matcher(ha);
				if (m.find()) {
					realm = m.group(1);
					String username = myIdentity.getMyUserName();
					String password = myIdentity.getMyPassword();
					String uri = myIdentity.getMySIPURI();

					String method = r.getMethod();
					String aResponse = Utils.digest(nonce, realm, username,
							password, uri, method);

					String authorization = "Digest username=\"" + username
							+ "\", realm=\"" + realm + "\", nonce=\"" + nonce
							+ "\", uri=\"" + uri + "\", response=\""
							+ aResponse + "\", algorithm=MD5";

					AuthorizationHeader ah = headerFactory
							.createAuthorizationHeader(authorization);
					r.addHeader(ah);

					Tid = sipProvider.getNewClientTransaction(r);
					dialog = Tid.getDialog();

					this.response = null;
					Tid.sendRequest();
					wait(5000);// TODO: check this

					if (response == null) {
						throw new Exception("Response timeout");
					}
				}
			} else {
				throw new Exception(
						"Error: No nonce provided for authentication..");
			}
		}
		return this.response;
	}

	/*
	 * Prepares a SIP REGISTER message
	 * 
	 * @return request request ready to be sent
	 */
	public Request register() throws Exception {
		SipURI requestURI = addressFactory.createSipURI(myIdentity
				.getMyUserName(), myIdentity.getSipServerIP());
		CallIdHeader callIdHeader = getNewCallIdHeader();
		CSeqHeader cSeqHeader = getCSeqHeader(Request.REGISTER);
		FromHeader fromHeader = (FromHeader) getHeader(myIdentity
				.getMyUserName(), myIdentity.getMySIPDomain(), true);
		ToHeader toHeader = (ToHeader) getHeader(myIdentity.getMyUserName(),
				myIdentity.getMySIPDomain(), false);
		List<ViaHeader> viaHeaders = getViaHeaders();
		MaxForwardsHeader maxForwards = getMaxForwardsHeader();

		Request request = messageFactory.createRequest(requestURI,
				Request.REGISTER, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);

		request.addHeader(contactHeader);
		request.addHeader(headerFactory.createHeader("Expires", "3600"));

		return request;
	}

	private MaxForwardsHeader getMaxForwardsHeader()
			throws InvalidArgumentException {
		return headerFactory.createMaxForwardsHeader(70);
	}

	private List<ViaHeader> getViaHeaders() throws ParseException,
			InvalidArgumentException {
		List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		String ipAddress = localUDPListeningPoint.getIPAddress();
		ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
				localUDPListeningPoint.getPort(), localUDPListeningPoint
						.getTransport(), null);

		viaHeaders.add(viaHeader);
		return viaHeaders;
	}

	private HeaderAddress getHeader(String sipName, String sipDomain,
			boolean isFrom) throws ParseException {
		SipURI address = addressFactory.createSipURI(sipName, sipDomain);
		Address nameAddress = addressFactory.createAddress(address);
		nameAddress.setDisplayName(sipName);
		if (isFrom == true) {
			return headerFactory.createFromHeader(nameAddress, null);
		} else {
			return headerFactory.createToHeader(nameAddress, null);
		}
	}

	private CSeqHeader getCSeqHeader(String register) throws ParseException,
			InvalidArgumentException {
		return headerFactory.createCSeqHeader(1L, Request.REGISTER);
	}

	private CallIdHeader getNewCallIdHeader() {
		return sipProvider.getNewCallId();
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		
	}

	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		this.response = response;
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		if (tid == null) {
			// RFC3261: MUST respond to every 2xx
			if (ackRequest != null && dialog != null) {
				try {
					dialog.sendAck(ackRequest);
				} catch (SipException se) {
					se.printStackTrace();
				}
			}
			return;
		}

		if (tid.getState() == TransactionState.COMPLETED
				|| tid.getState() == TransactionState.TERMINATED
				|| tid.getState() == TransactionState.TRYING) {
			notify();
		}

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					dialog.sendAck(ackRequest);
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider
								.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Handle this more gracefully
		// notify();
	}

	public void processIOException(IOExceptionEvent arg0) {
		// TODO Handle this more gracefully
		// notify();
	}

	public void processTimeout(TimeoutEvent arg0) {
		// TODO Handle this more gracefully
		// notify();
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Handle this more gracefully
		// notify();
	}

}
