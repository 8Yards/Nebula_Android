/*
 * author: michel hognurand, prajwol kumar nakarmi, nina mulkijanyan
 * 
 * version 1 - basic signalling
 * version 2 - extending Service
 */

package org.nebula.client.sip;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
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
import javax.sip.ServerTransaction;
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
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.nebula.main.NebulaApplication;
import org.nebula.main.NebulaEventHandler;
import org.nebula.models.MyIdentity;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.utils.SDPUtils;
import org.nebula.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import gov.nist.javax.sip.header.SIPHeader;
/**
 * Class for the SIP Client
 */
public class SIPClient implements SipListener {

	public static final String PRESENCE_ONLINE = "Online";
	public static final String PRESENCE_BUSY = "Busy";
	public static final String PRESENCE_AWAY = "Away";
	public static final String PRESENCE_OFFLINE = "Offline";
	private static final String PIDF_NS_VALUE = "urn:ietf:params:xml:ns:pidf";
	private static final String PRESENCE_ELEMENT = "presence";
	private static final String TUPLE_ELEMENT = "tuple";
	private static final String NOTE_ELEMENT = "note";
	private static final String ENTITY_ATTRIBUTE = "entity";
	public static final String NOTIFY_PRESENCE = "NOTIFY_PRESENCE_EVENT";
	public static final String NOTIFY_INVITE = "NOTIFY_INVITE_EVENT";
	public static final String THREAD_PARAMETER = "thread";
	public static final String CONVERSATION_PARAMETER = "conv";
	public static final String OLD_CONVERSATION_PARAMETER = "newvalue";

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;

	private static SipProvider sipProvider;
	private static SipStack sipStack;
	private NebulaEventHandler eventHandler = null;
	private MyIdentity myIdentity;

	private ClientTransaction Tid;
	private Dialog dialog;

	private Request ackRequest;// Save the created ACK request, to respond to
	// retransmitted 2xx
	private Response response;

	private ListeningPoint localUDPListeningPoint;
	private String transport = "udp";

	public SIPClient() throws Exception {
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
	}

	private ContactHeader createContactHeader() throws ParseException {
		SipURI contactURI = addressFactory.createSipURI(myIdentity
				.getMyUserName(), myIdentity.getMyIP());

		// TODO:: well in production remove this
		// contactURI = addressFactory.createSipURI(myIdentity.getMyUserName(),
		// "130.229.137.196 ");

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
		wait(5000);// TODO: check this/ handle waiting times in timeout

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

	private Request createRequest(String toSIPUser, String toSIPDomain,
			String reqType, SipURI requestURI) throws ParseException,
			InvalidArgumentException, Exception {
		return createRequest(false, toSIPUser, toSIPDomain, reqType, requestURI);
	}

	private Request createRequest(boolean isSameDialog, String toSIPUser,
			String toSIPDomain, String reqType, SipURI requestURI)
			throws ParseException, InvalidArgumentException, Exception {
		CallIdHeader callIdHeader = getNewCallIdHeader();
		if (isSameDialog == true) {
			callIdHeader.setCallId(dialog.getCallId().getCallId());
		}

		CSeqHeader cSeqHeader = getCSeqHeader(reqType);
		FromHeader fromHeader = (FromHeader) getHeader(myIdentity
				.getMyUserName(), myIdentity.getMySIPDomain(), true);
		ToHeader toHeader = (ToHeader) getHeader(toSIPUser, toSIPDomain, false);
		List<ViaHeader> viaHeaders = getViaHeaders();
		MaxForwardsHeader maxForwards = getMaxForwardsHeader();

		Request request = messageFactory.createRequest(requestURI, reqType,
				callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
				maxForwards);
		request.addHeader(createContactHeader());

		return request;
	}

	/*
	 * contact - michel, prajwol Prepares a SIP REGISTER message
	 */
	// TODO:: test the refactor
	public Request register() throws Exception {
		return register(3600);
	}

	public Request register(int expires) throws Exception {
		Request registerReq = createRequest(myIdentity.getMyUserName(),
				myIdentity.getMySIPDomain(), Request.REGISTER, addressFactory
						.createSipURI(myIdentity.getMyUserName(), myIdentity
								.getMySIPDomain()));
		registerReq.setExpires(headerFactory.createExpiresHeader(expires));

		return registerReq;
	}

	/*
	 * contact - nina, prajwol
	 */
	// TODO:: test the refactor
	public Request publish(String status, int expires) throws Exception {
		Request publishReq = createRequest(myIdentity.getMyUserName(),
				myIdentity.getMySIPDomain(), Request.PUBLISH, addressFactory
						.createSipURI(myIdentity.getMyUserName(), myIdentity
								.getMySIPDomain()));

		byte[] doc = getPidfPresenceStatus(status);

		publishReq.setContent(doc, headerFactory.createContentTypeHeader(
				"application", "pidf+xml"));
		publishReq.setHeader(headerFactory.createExpiresHeader(expires));
		publishReq.setHeader(headerFactory.createEventHeader("presence"));

		// replace the previous publish messages
		if (myIdentity.getSipETag().length() > 0) {
			publishReq.addHeader(headerFactory
					.createSIPIfMatchHeader(myIdentity.getSipETag()));
		}

		return publishReq;
	}

	/*
	 * contact - nina
	 */
	public Request subscribe(String toSIPUser, String toSIPDomain)
			throws Exception {
		Request subscribeReq = createRequest(toSIPUser, toSIPDomain,
				Request.SUBSCRIBE, addressFactory.createSipURI(toSIPUser,
						toSIPDomain));
		subscribeReq.setExpires(headerFactory.createExpiresHeader(3600));
		subscribeReq.addHeader(headerFactory.createEventHeader("presence"));
		subscribeReq.addHeader(headerFactory.createAcceptHeader("application",
				"pidf+xml"));
		subscribeReq.addHeader(headerFactory
				.createSubscriptionStateHeader("active"));

		return subscribeReq;
	}

	public Request invite(List<String> toSIPUsers) throws ParseException,
			InvalidArgumentException, Exception {
		return invite(toSIPUsers, myIdentity.getMySIPDomain());
	}

	// contact - prajwol
	public Request invite(List<String> toSIPUsers, String toSIPDomain)
			throws ParseException, InvalidArgumentException, Exception {
		// do you know that we always invite mcu :):) --myIdentity.getMcuName()

		// TODO:: change this to MCU >.<
		Request inviteReq = createRequest(myIdentity.getMcuName(), myIdentity
				.getMySIPDomain(), Request.INVITE, addressFactory.createSipURI(
				myIdentity.getMcuName(), myIdentity.getMySIPDomain()));
		inviteReq.setExpires(headerFactory.createExpiresHeader(3600));

		// TODO:: do actual XML
		StringBuilder rclList = new StringBuilder();
		for (int i = 0; i < toSIPUsers.size(); i++) {
			if (i > 0) {
				rclList.append(",");
			}
			rclList.append(toSIPUsers.get(i) + "@"
					+ myIdentity.getMySIPDomain());
		}

		// TODO:: add the MIME in elegant way
		String myMIMEContent = "--8Yards" + "\r\n"
				+ "Content-type: application/sdp" + "\r\n" + "" + "\r\n"
				+ SDPUtils.getMySDP() + "\r\n" + "--8Yards" + "\r\n"
				+ "Content-type: application/resource-lists+xml" + "\r\n"
				+ rclList.toString() + "\r\n" + "--8Yards--";
		Log.v("nebula", "sipClient: " + myMIMEContent);

		inviteReq
				.setContent(myMIMEContent.getBytes(), headerFactory
						.createContentTypeHeader("multipart",
								"mixed; boundary=8Yards"));

		ConversationThread thread = myIdentity.createThread();
		Conversation conversation = thread.addConversation(rclList.toString());

		// add thread and conversation parameters
		ToHeader toHeader = (ToHeader) inviteReq.getHeader(SIPHeader.TO);
		toHeader.setParameter(THREAD_PARAMETER, thread.getId());
		toHeader.setParameter(CONVERSATION_PARAMETER, conversation.getId());
		inviteReq.setHeader(toHeader);

		return inviteReq;
	}

	public Request bye() throws ParseException, InvalidArgumentException,
			Exception {
		// return Tid.getDialog().createRequest(Request.BYE) ;
		return dialog.createRequest(Request.BYE);
	}

	/*
	 * contact prajwol, nina we only support these statuses - Online, Busy
	 */
	public byte[] getPidfPresenceStatus(String status) throws ParseException {
		// TODO:: producing XML through API - nina
		// well, i am lazy and this seems faster in processing - prajwol :P
		String res = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
				+ "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" "
				+ "entity=\""
				+ myIdentity.getMySIPURI()
				+ "\">"
				+ "<dm:person id=\"p9206\">";

		// TODO:: check and produce XML well :S
		if (status.equals(PRESENCE_ONLINE)) {
			res += "<rpid:activities/>";
		} else {
			res += "<rpid:activities>" + "<rpid:busy/>" + "</rpid:activities>";
		}

		res += "</dm:person>" + "<tuple id=\"t6222\">" + "<status>"
				+ "<basic>open</basic>" + "</status>" + "<contact>"
				+ createContactHeader().getAddress().getURI().toString()
				+ "</contact>" + "<note>" + status + "</note>" + "</tuple>"
				+ "</presence>";

		return res.getBytes();
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
			return headerFactory.createFromHeader(nameAddress, String
					.valueOf(new Random().nextLong()));
		} else {
			return headerFactory.createToHeader(nameAddress, null);
		}
	}

	private CSeqHeader getCSeqHeader(String reqType) throws ParseException,
			InvalidArgumentException {
		return headerFactory.createCSeqHeader(1L, reqType);
	}

	private CallIdHeader getNewCallIdHeader() {
		return sipProvider.getNewCallId();
	}

	// contact - nina, prajwol
	public void processRequest(RequestEvent requestReceivedEvent) {
		Log.v("nebula", "sipclient:"
				+ requestReceivedEvent.getRequest().getMethod());

		try {
			Request request = requestReceivedEvent.getRequest();
			ServerTransaction serverTransaction = requestReceivedEvent
					.getServerTransaction();
			if (serverTransaction == null) {
				serverTransaction = sipProvider
						.getNewServerTransaction(request);
			}

			String method = request.getMethod();
			if (method.equals(Request.NOTIFY)) {
				processNotify(request, serverTransaction);

			} else if (method.equals(Request.INVITE)) {
				processInvite(request, serverTransaction);
			} else if (!method.equals(Request.ACK)) {
				throw new Exception("I don't know how to handle: " + method);
			}
		} catch (Exception e) {
			// we have been lazy here and yeah. lazy :|
			Log.e("nebula", "sipclient:" + e.getMessage());
		}
	}

	// contact - nina, prajwol
	private void processNotify(Request request,
			ServerTransaction serverTransaction) throws ParseException,
			SipException, InvalidArgumentException,
			ParserConfigurationException, FactoryConfigurationError,
			SAXException, IOException {

		// send 200 OK -) not to get the retrying packets??
		Response okResponse = messageFactory.createResponse(200, request);
		serverTransaction.sendResponse(okResponse);

		EventHeader eventHeader = (EventHeader) request
				.getHeader(EventHeader.NAME);

		// TODO:: handle other event types and extract sanity checks from here
		if (!eventHeader.getEventType().equalsIgnoreCase("presence")) {
			return;
		}

		// we dont handle inactive subscriptions
		// prajwol- using equals with whole header to check active is not
		// sufficient since it can contains other parameters. i changed to
		// comparing case insesitive check of
		// get state of header:)

		if (request.getHeader("Subscription-State") != null
				&& !((SubscriptionStateHeader) request
						.getHeader("Subscription-State")).getState()
						.equalsIgnoreCase(SubscriptionStateHeader.ACTIVE)) {
			return;
		}

		if (request.getContentLength().equals(0)) {
			return;
		}
		// --till here

		String inXML = new String(request.getRawContent()).trim();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		// TODO:: purpose of each instance ---- assinged to NINA
		Document doc = builder.parse(new InputSource(new StringReader(inXML)));

		// get presence element
		NodeList presList = doc.getElementsByTagNameNS(PIDF_NS_VALUE,
				PRESENCE_ELEMENT);

		if (presList.getLength() == 0) {
			presList = doc.getElementsByTagName(PRESENCE_ELEMENT);
			if (presList.getLength() == 0) {
				return;
			}
		}

		// we only use the first presence list =)
		Node presNode = presList.item(0);
		Element presence = (Element) presNode;
		NodeList tupleList = presence.getElementsByTagName(TUPLE_ELEMENT);

		for (int i = 0; i < tupleList.getLength(); i++) {
			Node tupleNode = tupleList.item(i);

			if (tupleNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element tuple = (Element) tupleNode;
			NodeList noteList = tuple.getElementsByTagName(NOTE_ELEMENT);

			if (noteList.getLength() > 0) {
				if (eventHandler != null) {
					eventHandler.processEvent(NOTIFY_PRESENCE, presence
							.getAttribute(ENTITY_ATTRIBUTE), noteList.item(0)
							.getFirstChild().getNodeValue());
				}
			}
		}
	}

	private void processInvite(Request request,
			ServerTransaction serverTransaction) throws Exception {

		if (myIdentity.getMyStatus().equals("Online")) {
			Response response = messageFactory.createResponse(Response.OK,
					request);
			response.addHeader(createContactHeader());

			String requestContent = new String(request.getRawContent());
			String requestSDP = SDPUtils.getSDP(requestContent);
			String requestRCL = SDPUtils.getRCL(requestContent);
			String mySDP = SDPUtils.getMySDP();

			// TODO:: add the MIME in elegant way
			String myMIMEContent = "--8Yards" + "\r\n"
					+ "Content-type: application/sdp" + "\r\n" + "" + "\r\n"
					+ mySDP + "\r\n" + "--8Yards" + "\r\n"
					+ "Content-type: application/resource-lists+xml" + "\r\n"
					+ requestRCL + "\r\n" + "--8Yards--";

			response.setContent(myMIMEContent.getBytes(), headerFactory
					.createContentTypeHeader("multipart",
							"mixed; boundary=8Yards"));

			serverTransaction.sendResponse(response);

			// add new thread and conversation
			ToHeader toHeader = (ToHeader) request.getHeader(SIPHeader.TO);
			String threadId = toHeader.getParameter(THREAD_PARAMETER);
			String convId = toHeader.getParameter(CONVERSATION_PARAMETER);
			ConversationThread thread;

			if (myIdentity.existsThread(threadId)) {
				thread = myIdentity.getThreadById(threadId);
			} else {
				thread = myIdentity.createThread(threadId);
			}
			thread.addConversation(convId, requestRCL);

			if (eventHandler != null) {
				// TODO Good SDP Parsing
				eventHandler.processEvent(NOTIFY_INVITE, SDPUtils
						.retrieveIP(requestSDP), SDPUtils
						.retrievePort(requestSDP));
			}
		} else {
			// TODO:: common handle this
			throw new Exception("I am busy :P");
		}
	}

	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		this.response = response;
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		// RFC3261: MUST respond to every 2xx
		if (tid != null && tid.getDialog() != null) {
			if (ackRequest != null) {
				try {
					tid.getDialog().sendAck(ackRequest);
				} catch (SipException se) {
					Log.e("nebula", se.getMessage());
				}
			}
		} else {
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
					Log.e("nebula", "sipClient: " + "sending ACK");
					ackRequest = tid.getDialog().createAck(cseq.getSeqNumber());
					tid.getDialog().sendAck(ackRequest);
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (tid.getDialog().getState() == DialogState.CONFIRMED) {
						Request byeRequest = tid.getDialog().createRequest(
								Request.BYE);
						ClientTransaction ct = sipProvider
								.getNewClientTransaction(byeRequest);
						tid.getDialog().sendRequest(ct);
					}
				} else if (cseq.getMethod().equals(Request.REGISTER)) {
					tid.getDialog().notify();
					// tid.notify() ;
				} else if (cseq.getMethod().equals(Request.PUBLISH)) {
					SIPETagHeader sipETag = (SIPETagHeader) response
							.getHeader("SIP-ETag");
					myIdentity.setSipETag(sipETag.getETag());
				}
			}
		} catch (Exception ex) {
			Log.e("nebula", "sipClient: " + ex.getMessage());
		}
	}

	/*
	 * contact nina
	 */
	public Request refer(String referSIPUser, String referSIPDomain,
			String threadId, String oldConversationId) throws Exception {
		// Create the request.
		Request request = createRequest(true, myIdentity.getMcuName(),
				myIdentity.getMySIPDomain(), Request.REFER, addressFactory
						.createSipURI(myIdentity.getMcuName(), myIdentity
								.getMySIPDomain()));

		SipURI referSIP = addressFactory.createSipURI(referSIPUser,
				referSIPDomain);
		Address referAddress = addressFactory.createAddress(referSIP);
		ReferToHeader referToHeader = headerFactory
				.createReferToHeader(referAddress);
		request.addHeader(referToHeader);

		// update old conversation and add new one
		ConversationThread currentThread = myIdentity.getThreadById(threadId);

		String newRcl = currentThread.getConversation(oldConversationId)
				.getRcl()
				+ "," + referSIP.toString();

		Conversation newConversation = currentThread.addConversation(newRcl);

		// append thread and conversation parameters
		ToHeader toHeader = (ToHeader) request.getHeader(SIPHeader.TO);
		toHeader.setParameter(THREAD_PARAMETER, threadId);
		toHeader.setParameter(CONVERSATION_PARAMETER, newConversation.getId());
		toHeader.setParameter(OLD_CONVERSATION_PARAMETER, oldConversationId);
		request.setHeader(toHeader);

		return request;
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
		Log.v("nebula", "sip_client: " + "timeout occured");
		notify();
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Handle this more gracefully
		// notify();
	}

	public NebulaEventHandler getEventHandler() {
		return eventHandler;
	}

	public void setEventHandler(NebulaEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

}