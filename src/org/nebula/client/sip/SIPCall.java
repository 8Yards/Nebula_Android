package org.nebula.client.sip;

import static org.nebula.client.sip.NebulaSIPConstants.CONVERSATION_PARAMETER;
import static org.nebula.client.sip.NebulaSIPConstants.NOTIFY_INVITE;
import static org.nebula.client.sip.NebulaSIPConstants.NOTIFY_PRESENCE;
import static org.nebula.client.sip.NebulaSIPConstants.THREAD_PARAMETER;
import gov.nist.javax.sip.header.SIPHeader;

import java.io.IOException;
import java.text.ParseException;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionState;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;
import org.nebula.utils.SIPUtils;
import org.xml.sax.SAXException;

import android.util.Log;

public class SIPCall {
	private MyIdentity myIdentity = NebulaApplication.getInstance()
			.getMyIdentity();
	private Dialog dialog = null;
	private Response lastResponse = null;
	private int registerTryCount = 0;

	private long seqCount = 1L;

	private SIPHandler mySipHandler = NebulaApplication.getInstance()
			.getMySIPHandler();

	public void handleRequestEvent(RequestEvent requestReceivedEvent,
			ServerTransaction st) throws Exception {
		Request request = requestReceivedEvent.getRequest();
		String method = request.getMethod();

		if (method.equals(Request.NOTIFY)) {
			processNotify(request, st);
		} else if (method.equals(Request.INVITE)) {
			processInvite(request, st);
		}
	}

	public synchronized void handleResponseEvent(
			ResponseEvent responseReceivedEvent, ClientTransaction ct) {
		lastResponse = (Response) responseReceivedEvent.getResponse();

		if (ct.getState() == TransactionState.COMPLETED
				|| ct.getState() == TransactionState.TERMINATED
				|| ct.getState() == TransactionState.TRYING) {
			Log.v("nebula-sip", "handleResponseEvent: " + ct.getState());
			notify();
		}

		CSeqHeader cseq = (CSeqHeader) lastResponse.getHeader(CSeqHeader.NAME);
		if (lastResponse.getStatusCode() == Response.OK) {

			if (cseq.getMethod().equals(Request.INVITE)) {
				try {
					dialog.sendAck(dialog.createAck(cseq.getSeqNumber()));
				} catch (SipException e) {
					// dont do anything here
				} catch (InvalidArgumentException e) {
					// dont do anything here
				}
			} else if (cseq.getMethod().equals(Request.PUBLISH)) {
				SIPETagHeader sipETag = (SIPETagHeader) lastResponse
						.getHeader("SIP-ETag");
				myIdentity.setSipETag(sipETag.getETag());
			}
		}
	}

	public synchronized Response sendRequest(Request request) throws Exception {
		Request r = (Request) request.clone();
		ClientTransaction ct = mySipHandler.getSipProvider()
				.getNewClientTransaction(request);
		dialog = ct.getDialog();

		if (dialog != null) {
			mySipHandler.addCall(this);

			if (r.getMethod() == Request.SUBSCRIBE) {
				mySipHandler.getKeyToCall().put(
						NebulaSIPConstants.SUBSCRIBE_NOTIFY_CALLID,
						dialog.getCallId().getCallId());
			}
		}

		Log.v("nebula-sip", "sendRequest: call size: "
				+ mySipHandler.getCallCount());
		Log.v("nebula-sip", "sendRequest: key size: "
				+ mySipHandler.getKeyToCall().size());

		lastResponse = null;
		ct.sendRequest();
		wait(10000);

		if (lastResponse == null) {
			throw new Exception("No response");
		} else if (lastResponse.getStatusCode() == Response.UNAUTHORIZED
				&& registerTryCount == 0) {
			String ha = lastResponse.getHeader("WWW-Authenticate").toString();

			AuthorizationHeader ah = mySipHandler.createAuthorizationHeader(ha,
					r.getMethod());
			r.addHeader(ah);
			ct.terminate();
			registerTryCount++;
			return sendRequest(r);
		}

		Response resp = (Response) lastResponse.clone();
		lastResponse = null;
		return resp;
	}

	private void processNotify(Request request,
			ServerTransaction serverTransaction) throws ParseException,
			SipException, InvalidArgumentException,
			ParserConfigurationException, FactoryConfigurationError,
			SAXException, IOException {

		Response okResponse = mySipHandler.getMessageFactory().createResponse(
				200, request);
		serverTransaction.sendResponse(okResponse);

		EventHeader eventHeader = (EventHeader) request
				.getHeader(EventHeader.NAME);

		// TODO:: handle other event types and extract sanity checks from here
		if (!eventHeader.getEventType().equalsIgnoreCase("presence")) {
			return;
		}

		if (request.getHeader("Subscription-State") != null
				&& !((SubscriptionStateHeader) request
						.getHeader("Subscription-State")).getState()
						.equalsIgnoreCase(SubscriptionStateHeader.ACTIVE)) {
			return;
		}

		if (request.getContentLength().getContentLength() == 0) {
			return;
		}
		// --till here

		String inXML = new String(request.getRawContent()).trim();
		String[] userStatus = SIPUtils.parsePresenceXML(inXML);

		if (mySipHandler.getEventHandler() != null) {
			mySipHandler.getEventHandler().processEvent(NOTIFY_PRESENCE,
					userStatus[0], userStatus[1]);
		}
	}

	private void processInvite(Request request,
			ServerTransaction serverTransaction) throws Exception {

		Response response = mySipHandler.getMessageFactory().createResponse(
				Response.OK, request);
		response.addHeader(mySipHandler.createContactHeader());

		String requestContent = new String(request.getRawContent());
		String requestSDP = SIPUtils.getSDP(requestContent);
		String requestRCL = SIPUtils.getRCL(requestContent);

		// add new thread and conversation
		ToHeader toHeader = (ToHeader) request.getHeader(SIPHeader.TO);
		String threadId = toHeader.getParameter(THREAD_PARAMETER);
		String convId = toHeader.getParameter(CONVERSATION_PARAMETER);

		if (!myIdentity.existsThread(threadId)) {
			// TODO:: add the MIME in elegant way
			String myMIMEContent = "--8Yards" + "\r\n"
					+ "Content-type: application/sdp" + "\r\n" + "" + "\r\n"
					+ SIPUtils.getMySDP() + "\r\n" + "--8Yards" + "\r\n"
					+ "Content-type: application/resource-lists+xml" + "\r\n"
					+ requestRCL + "\r\n" + "--8Yards--";

			response.setContent(myMIMEContent.getBytes(), mySipHandler
					.getHeaderFactory().createContentTypeHeader("multipart",
							"mixed; boundary=8Yards"));
		}

		mySipHandler.getKeyToCall().put(threadId + convId,
				dialog.getCallId().getCallId());

		serverTransaction.sendResponse(response);

		if (mySipHandler.getEventHandler() != null) {
			mySipHandler.getEventHandler().processEvent(NOTIFY_INVITE,
					SIPUtils.retrieveIP(requestSDP),
					SIPUtils.retrievePort(requestSDP), threadId, convId,
					requestRCL);
		}
	}

	public Dialog getDialog() {
		return dialog;
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public long getSeqCount() {
		return seqCount++;
	}
}
