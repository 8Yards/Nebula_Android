/*
 * author: michel hognurand, prajwol kumar nakarmi, nina mulkijanyan
 */

package org.nebula.client.sip;

import static org.nebula.client.sip.NebulaSIPConstants.REGISTER_SIPEXCEPTION;
import gov.nist.javax.sip.header.SIPHeader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
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
import javax.sip.header.ReferToHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.nebula.main.NebulaApplication;
import org.nebula.main.NebulaEventHandler;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.MyIdentity;
import org.nebula.models.Status;
import org.nebula.utils.SIPUtils;
import org.nebula.utils.Utils;

import android.util.Log;

public class SIPHandler implements SipListener {
	private MyIdentity myIdentity = NebulaApplication.getInstance()
			.getMyIdentity();
	private NebulaEventHandler eventHandler;

	private SipStack sipStack;
	private SipProvider sipProvider;
	private AddressFactory addressFactory;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;

	private final String transport = "udp";
	private ListeningPoint localUDPListeningPoint;

	private SIPCall lastCall;
	private Map<String, SIPCall> myCalls;
	private Map<String, String> keyToCall;

	public SIPHandler(NebulaEventHandler eventHandler) throws Exception {
		this.eventHandler = eventHandler;

		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "NebulaSIPHandler");

		SipFactory sipFactory = SipFactory.getInstance();
		// sipFactory.setPathName("gov.nist");

		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

		sipStack = sipFactory.createSipStack(properties);
		localUDPListeningPoint = sipStack.createListeningPoint(myIdentity
				.getMyIP(), myIdentity.getMySIPPort(), transport);

		sipProvider = sipStack.createSipProvider(localUDPListeningPoint);
		sipProvider.addSipListener(this);

		myCalls = new HashMap<String, SIPCall>();
		keyToCall = new HashMap<String, String>();
		keyToCall.put(NebulaSIPConstants.SUBSCRIBE_NOTIFY_CALLID, "fake");
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		ServerTransaction st = requestReceivedEvent.getServerTransaction();
		if (st == null) {
			// doesn't belong to any existing dialog
			try {
				st = sipProvider.getNewServerTransaction(requestReceivedEvent
						.getRequest());
			} catch (TransactionAlreadyExistsException e) {
				// retransmitted request. so we don't process this
			} catch (TransactionUnavailableException e) {
				// cannot have transaction. so we don't process this
			}
		}

		SIPCall call = getCallByDialog(st.getDialog());
		if (call != null) {
			try {
				call.handleRequestEvent(requestReceivedEvent, st);
			} catch (Exception e) {
				removeCallById(call.getDialog().getCallId().getCallId());

				Log.e("nebula-sip", "SIPHandler.processRequest: "
						+ e.getMessage());
			}
		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		ClientTransaction ct = responseReceivedEvent.getClientTransaction();
		if (ct == null) {
			// this is a stray response, we just ignore it
			return;
		}

		SIPCall call = getCallByDialog(ct.getDialog());
		if (call != null) {
			call.handleResponseEvent(responseReceivedEvent, ct);
		}
	}

	public void processDialogTerminated(DialogTerminatedEvent event) {
		// removeCallById(event.getDialog().getCallId().getCallId());
		Log.v("nebula-sip", "dialogTerminate 1");
	}

	public void processIOException(IOExceptionEvent arg0) {
	}

	public void processTimeout(TimeoutEvent arg0) {
		// NOTE:: I commented this. Please verify
		// notify
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
	}

	public Status sendRegister(int timeout) {
		try {
			lastCall = new SIPCall();
			Response resp = lastCall.sendRequest(createRegisterRequest(
					lastCall, timeout));
			if (resp.getStatusCode() == Response.OK) {
				return new Status(true, "Register Success");
			} else {
				throw new Exception("Register Failure");
			}
		} catch (TimeoutException e) {
			return new Status(false, "" + REGISTER_SIPEXCEPTION);
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendRegister: "
					+ e.getMessage());
		} finally {
			lastCall = null;
		}
	}

	public Status sendRegister() {
		return sendRegister(3600);
	}

	public Status sendInvite(ConversationThread thread,
			Conversation conversation) {
		lastCall = new SIPCall();
		try {
			Response resp = lastCall.sendRequest(createInviteRequest(lastCall,
					conversation.getRcl(), thread.getThreadName(), conversation
							.getConversationName()));
			if (resp.getStatusCode() == Response.OK) {
				keyToCall.put(conversation.toString(), lastCall.getDialog()
						.getCallId().getCallId());
				return new Status(true, new String(resp.getRawContent()));
			} else {
				throw new Exception("Invite Failure");
			}
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendInvite: " + e.getMessage());
		} finally {
			lastCall = null;
		}
	}

	public Status sendRefer(String referSIPUser, String referSIPDomain,
			String threadId, Conversation oldConversation,
			Conversation newConversation) {
		try {
			String callId = keyToCall.get(oldConversation.toString());

			lastCall = myCalls.get(callId);
			Response resp = lastCall.sendRequest(createReferRequest(lastCall,
					referSIPUser, referSIPDomain, threadId, oldConversation
							.getConversationName(), newConversation
							.getConversationName()));
			if (resp.getStatusCode() == Response.OK) {
				keyToCall.remove(oldConversation.toString());
				keyToCall.put(newConversation.toString(), lastCall.getDialog()
						.getCallId().getCallId());
				return new Status(true, new String(resp.getRawContent()));
			} else {
				throw new Exception("Refer Failure");
			}
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendRefer: " + e.getMessage());
		} finally {
			lastCall = null;
		}
	}

	public Status sendSubscribe(String toSIPUser, String toSIPDomain) {
		try {
			lastCall = myCalls.get(keyToCall
					.get(NebulaSIPConstants.SUBSCRIBE_NOTIFY_CALLID));
			if (lastCall == null) {
				lastCall = new SIPCall();
			}

			Response resp = lastCall.sendRequest(createSubscribeRequest(
					lastCall, toSIPUser, toSIPDomain));
			if (resp.getStatusCode() == Response.OK) {
				return new Status(true, "Subscribe Success");
			} else {
				throw new Exception("Subscribe Failure");
			}
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendSubscribe: "
					+ e.getMessage());
		} finally {
			lastCall = null;
		}
	}

	public Status sendPublish(String status, int expires) {
		try {
			lastCall = new SIPCall();

			Response resp = lastCall.sendRequest(createPublishRequest(lastCall,
					status, expires));
			if (resp.getStatusCode() == Response.OK) {
				return new Status(true, "Publish Success");
			} else {
				throw new Exception("Publish Failure");
			}
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendRefer: " + e.getMessage());
		} finally {
			lastCall = null;
		}
	}

	public Status sendByeToAll() {
		try {
			List<String> sendByeTo = new ArrayList<String>();
			for (Iterator<SIPCall> iter = myCalls.values().iterator(); iter
					.hasNext();) {
				String callId = iter.next().getDialog().getCallId().getCallId();
				if (!callId.equals(keyToCall
						.get(NebulaSIPConstants.SUBSCRIBE_NOTIFY_CALLID))) {
					sendByeTo.add(callId);
				}
			}

			for (String byeTo : sendByeTo) {
				sendBye(byeTo);
			}
			return new Status(true, "ByeToAll success");
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendByToAll: "
					+ e.getMessage());
		}
	}

	private Status sendBye(String callId) {
		try {
			SIPCall call = myCalls.get(callId);
			call.sendBye();
			myCalls.remove(callId);

			return new Status(true, "Bye Success");
		} catch (Exception e) {
			return new Status(false, "SIPHandler.sendBye: " + e.getMessage());
		}
	}

	public ContactHeader createContactHeader() throws ParseException {
		SipURI contactURI = addressFactory.createSipURI(myIdentity
				.getMyUserName(), myIdentity.getMyIP());

		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(myIdentity.getMyUserName());

		return headerFactory.createContactHeader(contactAddress);
	}

	public Request createRegisterRequest(SIPCall call) throws Exception {
		return createRegisterRequest(call, 3600);
	}

	public Request createRegisterRequest(SIPCall call, int expires)
			throws Exception {
		Request registerReq = createRequest(call, myIdentity.getMyUserName(),
				myIdentity.getMySIPDomain(), Request.REGISTER, addressFactory
						.createSipURI(myIdentity.getMyUserName(), myIdentity
								.getMySIPDomain()));
		registerReq.setExpires(headerFactory.createExpiresHeader(expires));

		return registerReq;
	}

	public Request createInviteRequest(SIPCall call, String rclList,
			String threadId, String conversationId) throws ParseException,
			InvalidArgumentException, Exception {
		Request inviteReq = createRequest(call, myIdentity.getMcuName(),
				myIdentity.getMySIPDomain(), Request.INVITE, addressFactory
						.createSipURI(myIdentity.getMcuName(), myIdentity
								.getMySIPDomain()));
		inviteReq.setExpires(headerFactory.createExpiresHeader(3600));

		// TODO:: add the MIME in elegant way
		String myMIMEContent = "--8Yards" + "\r\n"
				+ "Content-type: application/sdp" + "\r\n" + "" + "\r\n"
				+ SIPUtils.getMySDP() + "\r\n" + "--8Yards" + "\r\n"
				+ "Content-type: application/resource-lists+xml" + "\r\n"
				+ rclList.toString() + "\r\n" + "--8Yards--";

		inviteReq
				.setContent(myMIMEContent.getBytes(), headerFactory
						.createContentTypeHeader("multipart",
								"mixed; boundary=8Yards"));

		// add thread and conversation parameters
		ToHeader toHeader = (ToHeader) inviteReq.getHeader(SIPHeader.TO);
		toHeader.setParameter(NebulaSIPConstants.THREAD_PARAMETER, threadId);
		toHeader.setParameter(NebulaSIPConstants.CONVERSATION_PARAMETER,
				conversationId);
		inviteReq.setHeader(toHeader);

		return inviteReq;
	}

	public Request createReferRequest(SIPCall call, String referSIPUser,
			String referSIPDomain, String threadId, String oldConversationName,
			String newConversationName) throws ParseException,
			InvalidArgumentException, Exception {

		if (call.getDialog() == null) {
			throw new Exception("No existing dialog found");
		}

		Request request = createRequest(call, myIdentity.getMcuName(),
				myIdentity.getMySIPDomain(), Request.REFER, addressFactory
						.createSipURI(myIdentity.getMcuName(), myIdentity
								.getMySIPDomain()));
		SipURI referSIP = addressFactory.createSipURI(referSIPUser,
				referSIPDomain);
		Address referAddress = addressFactory.createAddress(referSIP);
		ReferToHeader referToHeader = headerFactory
				.createReferToHeader(referAddress);
		request.addHeader(referToHeader);

		// append thread and conversation parameters
		ToHeader toHeader = (ToHeader) request.getHeader(SIPHeader.TO);
		toHeader.setParameter(NebulaSIPConstants.THREAD_PARAMETER, threadId);
		toHeader.setParameter(NebulaSIPConstants.CONVERSATION_PARAMETER,
				newConversationName);
		toHeader.setParameter(NebulaSIPConstants.OLD_CONVERSATION_PARAMETER,
				oldConversationName);
		request.setHeader(toHeader);

		return request;
	}

	private Request createSubscribeRequest(SIPCall call, String toSIPUser,
			String toSIPDomain) throws ParseException,
			InvalidArgumentException, Exception {
		Request subscribeReq = createRequest(call, toSIPUser, toSIPDomain,
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

	private Request createPublishRequest(SIPCall call, String status,
			int expires) throws ParseException, InvalidArgumentException,
			Exception {
		Request publishReq = createRequest(call, myIdentity.getMyUserName(),
				myIdentity.getMySIPDomain(), Request.PUBLISH, addressFactory
						.createSipURI(myIdentity.getMyUserName(), myIdentity
								.getMySIPDomain()));

		byte[] doc = SIPUtils.getPidfPresenceStatus(status, myIdentity
				.getMySIPURI(), createContactHeader().getAddress().getURI()
				.toString());

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

	private Request createRequest(SIPCall call, String toSIPUser,
			String toSIPDomain, String reqType, SipURI requestURI)
			throws ParseException, InvalidArgumentException, Exception {

		CallIdHeader callIdHeader = null;
		String fromTag = null;
		String toTag = null;
		if (call.getDialog() == null) {
			callIdHeader = sipProvider.getNewCallId();
			fromTag = String.valueOf(new Random().nextLong());
		} else {
			callIdHeader = call.getDialog().getCallId();
			fromTag = call.getDialog().getLocalTag();
		}
		CSeqHeader cSeqHeader = getCSeqHeader(call.getSeqCount(), reqType);

		FromHeader fromHeader = (FromHeader) getHeader(myIdentity
				.getMyUserName(), myIdentity.getMySIPDomain(), true, fromTag);
		ToHeader toHeader = (ToHeader) getHeader(toSIPUser, toSIPDomain, false,
				toTag);
		List<ViaHeader> viaHeaders = getViaHeaders();
		MaxForwardsHeader maxForwards = getMaxForwardsHeader();

		Request request = messageFactory.createRequest(requestURI, reqType,
				callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
				maxForwards);
		request.addHeader(createContactHeader());

		return request;
	}

	public AuthorizationHeader createAuthorizationHeader(String ha,
			String method) throws ParseException {
		String nonce;
		String realm;

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

				String aResponse = Utils.digest(nonce, realm, username,
						password, uri, method);

				String authorization = "Digest username=\"" + username
						+ "\", realm=\"" + realm + "\", nonce=\"" + nonce
						+ "\", uri=\"" + uri + "\", response=\"" + aResponse
						+ "\", algorithm=MD5";

				return headerFactory.createAuthorizationHeader(authorization);
			} else {
				throw new ParseException(
						"No realm provided for authentication..", -1);
			}

		} else {
			throw new ParseException("No nonce provided for authentication..",
					-1);
		}
	}

	private CSeqHeader getCSeqHeader(long seqCount, String reqType)
			throws ParseException, InvalidArgumentException {
		return headerFactory.createCSeqHeader(seqCount, reqType);
	}

	private HeaderAddress getHeader(String sipName, String sipDomain,
			boolean isFrom, String tag) throws ParseException {
		SipURI address = addressFactory.createSipURI(sipName, sipDomain);
		Address nameAddress = addressFactory.createAddress(address);
		nameAddress.setDisplayName(sipName);
		if (isFrom == true) {
			return headerFactory.createFromHeader(nameAddress, tag);
		} else {
			return headerFactory.createToHeader(nameAddress, tag);
		}
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

	private MaxForwardsHeader getMaxForwardsHeader()
			throws InvalidArgumentException {
		return headerFactory.createMaxForwardsHeader(70);
	}

	public int getCallCount() {
		return myCalls.size();
	}

	private SIPCall getCallByDialog(Dialog dialog) {
		if (dialog == null) {
			return lastCall;
		}

		String id = dialog.getCallId().getCallId();
		SIPCall call = myCalls.get(id);
		if (call == null) {
			call = new SIPCall();
			call.setDialog(dialog);
			addCall(call);
		}
		return call;
	}

	public void addCall(SIPCall call) {
		myCalls.put(call.getDialog().getCallId().getCallId(), call);
	}

	public void removeCallById(String id) {
		myCalls.remove(id);
	}

	public Map<String, String> getKeyToCall() {
		return keyToCall;
	}

	public SipProvider getSipProvider() {
		return sipProvider;
	}

	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	public NebulaEventHandler getEventHandler() {
		return eventHandler;
	}
}