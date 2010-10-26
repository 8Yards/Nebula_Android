package org.nebula.sipClient;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Class for the SIP Client
 * @author Nebula
 */
public class SIPClient implements SipListener {
	private static SipProvider sipProvider;
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private ContactHeader contactHeader;
	private ClientTransaction Tid;
	private Dialog dialog;
    private Request ackRequest;// Save the created ACK request, to respond to retransmitted 2xx
    private String sipServerIP = "130.229.159.97";//"130.229.144.30";//
    private Integer sipServerPort = 5060;//5061;//
    private String sipServerName = "Server";
    private String myIP;
    private Integer myPort;
    private String mySIPName;
    private String mySIPDomain;
    private ListeningPoint localUDPListeningPoint;
	private String transport = "udp";
	
	/*
	 * simply to test the SIP Client
	 */
	public static void main(String args[]) throws Exception {
		Request request;
		Response response;
		
		System.out.println("Start!");
		SIPClient sip = new SIPClient("130.229.152.58", 5075, "michel", "130.229.159.97");

		request = sip.register();

		response = sip.send(request);
		
		request = sip.invite("nina", "130.229.159.97");

		response = sip.send(request);
		
		sip.bye();
		
		/*request = sip.refer("nina", "130.229.159.97", "sdfdsf", "baba.com");

		response = sip.send(request);*/
		
		System.out.println(response);
		System.out.println("End!");
	}
	
	/*
	 * Constructor
	 * @param	fromIPAddress	IP address of the client
	 * @param	fromPort		port used by of the client
	 * @param	fromName		sip name of the client
	 * @param	fromDomain		sip domain of the client
	 */
    public SIPClient(String fromIPAddress, Integer fromPort, String fromName, String fromDomain) throws Exception {
    	this.myIP = fromIPAddress;
    	this.myPort = fromPort;
    	this.mySIPName = fromName;
    	this.mySIPDomain = fromDomain;
    	SipFactory sipFactory = null;
	    sipStack = null;
	    sipFactory = SipFactory.getInstance();
	    sipFactory.setPathName("gov.nist");
	    Properties properties = new Properties();
	    properties.setProperty("javax.sip.STACK_NAME", "SipClient");
	    // Create SipStack object
	    sipStack = sipFactory.createSipStack(properties);
	    
	    headerFactory = sipFactory.createHeaderFactory();
	    addressFactory = sipFactory.createAddressFactory();
	    messageFactory = sipFactory.createMessageFactory();

	    localUDPListeningPoint = sipStack.createListeningPoint(this.myIP, this.myPort, "udp");
	
	    sipProvider = sipStack.createSipProvider(localUDPListeningPoint);
	    System.out.println("\n jain sip stack started on " + fromIPAddress + ":" + fromPort + "/" + ListeningPoint.UDP);
	    sipProvider.addSipListener(this);
    }

	/*
	 * Sends a SIP request
	 * @param	request		The SIP request to be sent
	 * @result	response	SIP response
	 */
    public synchronized Response send(Request request) throws Exception {
	    // Create the client transaction.
	    //ClientTransaction registerTid = 
	    //	sipProvider.getNewClientTransaction(request);

		// Create the client transaction.
		Tid = sipProvider.getNewClientTransaction(request);
	
		System.out.println(request);

		dialog = Tid.getDialog();
		
	    // send the request out.
	    Tid.sendRequest();
		wait();

	    return null;
	    //return this.response();
    }

	/*
	 * Prepares a SIP REFER message
	 * @param	toUser				name of the SIP recipient
	 * @param	toUserDomain		domain of the SIP recipient
	 * @param	referSIPID			name of the resource
	 * @param	referSIPDomain		domain of the resource
	 * @return	request				request ready to be sent
	 */
    public Request refer(String toUser, String toUserDomain, String referSIPID, String referSIPDomain) throws Exception {
    	// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(this.mySIPName,
				this.mySIPDomain);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(this.mySIPName);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");

		// create To Header
		SipURI toAddress = addressFactory
				.createSipURI(toUser, toUserDomain);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toUser);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);

		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toUser, sipServerIP+":"+sipServerPort);

		// Create ViaHeaders
		ArrayList viaHeaders = new ArrayList();
		//String ipAddress = udpListeningPoint.getIPAddress();
		//ViaHeader viaHeader = headerFactory.createViaHeader(sipServerIP, sipServerPort,
		ViaHeader viaHeader = headerFactory.createViaHeader(myIP, myPort,
				transport , null);

		// add via headers
		viaHeaders.add(viaHeader);

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				Request.REFER);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);

		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.REFER, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		
		// Create contact headers
		SipURI contactUrl = addressFactory.createSipURI(mySIPName, mySIPDomain);
		contactUrl.setPort(localUDPListeningPoint.getPort());
		contactUrl.setLrParam();

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(mySIPName, sipServerIP);
		contactURI.setPort(sipProvider.getListeningPoint(transport)
				.getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(mySIPName);

		contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		// You can add extension headers of your own making
		// to the outgoing SIP request.
		// Add the extension header.
		SipURI referSIP = addressFactory.createSipURI(referSIPID,
				referSIPDomain);
		Address referAddress = addressFactory.createAddress(referSIP);
		ReferToHeader referToHeader = headerFactory.createReferToHeader(referAddress);
		request.addHeader(referToHeader);
		
		return request;
    }

	/*
	 * Prepares a SIP INVITE message
	 * @param	toUser				name of the SIP recipient
	 * @param	toUserDomain		domain of the SIP recipient
	 * @return	request				request ready to be sent
	 */
    public Request invite(String toUser, String toUserDomain) throws Exception {
    	// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(this.mySIPName,
				this.mySIPDomain);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(this.mySIPName);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");

		// create To Header
		SipURI toAddress = addressFactory
				.createSipURI(toUser, toUserDomain);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toUser);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);

		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toUser, sipServerIP+":"+sipServerPort);

		// Create ViaHeaders
		ArrayList viaHeaders = new ArrayList();
		//String ipAddress = udpListeningPoint.getIPAddress();
		//ViaHeader viaHeader = headerFactory.createViaHeader(sipServerIP, sipServerPort,
		ViaHeader viaHeader = headerFactory.createViaHeader(myIP, myPort,
				transport , null);

		// add via headers
		viaHeaders.add(viaHeader);

		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory
				.createContentTypeHeader("application", "sdp");

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				Request.INVITE);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);

		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		
		// Create contact headers
		SipURI contactUrl = addressFactory.createSipURI(mySIPName, mySIPDomain);
		contactUrl.setPort(localUDPListeningPoint.getPort());
		contactUrl.setLrParam();

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(mySIPName, sipServerIP);
		contactURI.setPort(sipProvider.getListeningPoint(transport)
				.getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(mySIPName);

		contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		// You can add extension headers of your own making
		// to the outgoing SIP request.
		// Add the extension header.
		Header extensionHeader = headerFactory.createHeader("My-Header",
				"my header value");
		request.addHeader(extensionHeader);

		String sdpData = "v=0\r\n"
				+ "o=4855 13760799956958020 13760799956958020"
				+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
				+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
				+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
				+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
				+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
		byte[] contents = sdpData.getBytes();

		request.setContent(contents, contentTypeHeader);
		// You can add as many extension headers as you
		// want.

		extensionHeader = headerFactory.createHeader("My-Other-Header",
				"my new header value ");
		request.addHeader(extensionHeader);

		Header callInfoHeader = headerFactory.createHeader("Call-Info",
				"<http://www.antd.nist.gov>");
		request.addHeader(callInfoHeader);

		//System.out.println(request);
		
		return request;
    }

	/*
	 * Prepares a SIP REGISTER message
	 * @return	request				request ready to be sent
	 */
    public Request register() throws Exception {
    	// create >From Header
	    SipURI fromAddress = addressFactory.createSipURI(mySIPName, 
	    		sipServerIP);
	
	    Address fromNameAddress = addressFactory.createAddress(fromAddress);
	    fromNameAddress.setDisplayName(sipServerName);
	    FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, 
	    		null);
	
	    // create To Header
	    SipURI toAddress = addressFactory.createSipURI(mySIPName, sipServerIP);
	    Address toNameAddress = addressFactory.createAddress(toAddress);
	    toNameAddress.setDisplayName(sipServerName);
	    ToHeader toHeader = headerFactory.createToHeader(toNameAddress,null);
	
	    // create Request URI
	    SipURI requestURI = addressFactory.createSipURI(mySIPName, sipServerIP);
	
	    // Create ViaHeaders
	
	    List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
	    String ipAddress = localUDPListeningPoint.getIPAddress();
	    ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress, 
	    		localUDPListeningPoint.getPort(), 
	    		localUDPListeningPoint.getTransport(), null);
	
	    // add via headers
	    viaHeaders.add(viaHeader);
	
	    // Create a new CallId header
	    CallIdHeader callIdHeader = sipProvider.getNewCallId();
	
	    // Create a new Cseq header
	    CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(701L, 
	    		Request.REGISTER);
	
	    // Create a new MaxForwardsHeader
	    MaxForwardsHeader maxForwards = 
	    	headerFactory.createMaxForwardsHeader(70);
	
	    // Create the request.
	    Request request = messageFactory.createRequest(requestURI, 
	    		Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, 
	    		toHeader, viaHeaders, maxForwards);
	    // Create contact headers
	
	    // Create the contact name address.
	    SipURI contactURI = addressFactory.createSipURI(mySIPName, myIP);
	    contactURI.setPort(sipProvider.getListeningPoint(
	    		localUDPListeningPoint.getTransport()).getPort());
	
	    Address contactAddress = addressFactory.createAddress(contactURI);
	
	    contactHeader = headerFactory.createContactHeader(contactAddress);
	    request.addHeader(contactHeader);
	
	    // You can add extension headers of your own making
	    // to the outgoing SIP request.
	    // Add the extension header.
	    Header extensionHeader = headerFactory.createHeader("Expires", "360000");
	    request.addHeader(extensionHeader);
	    
	    return request;
    }

	/*
	 * Sends a SIP BYE message
	 */
    public void bye() throws Exception {
    	Request byeRequest = this.dialog.createRequest(Request.BYE);
    	ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
		dialog.sendRequest(ct);
    }

    /*
     * Handles the bye task
     */
	class ByeTask  extends TimerTask {
		Dialog dialog;
		public ByeTask(Dialog dialog)  {
			this.dialog = dialog;
		}
		public void run () {
			try {
			   Request byeRequest = this.dialog.createRequest(Request.BYE);
			   ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
			   dialog.sendRequest(ct);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	/*
	 * Sends a SIP CANCEL message 
	 */
	public void sendCancel() {
		try {
			System.out.println("Sending cancel");
			Request cancelRequest = Tid.createCancel();
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
		else {
			try {
				serverTransactionId.sendResponse( messageFactory.createResponse(202,request) );
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * Processes a BYE request
	 * @param	request					The request
	 * @param	serverTransactionId		to identity the transaction
	 */
	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			if (serverTransactionId == null) {
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		System.out.println("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
		System.out.println(response);
			
		if (tid == null) {
			// RFC3261: MUST respond to every 2xx
			if (ackRequest!=null && dialog!=null) {
			   System.out.println("re-sending ACK");
			   try {
			      dialog.sendAck(ackRequest);
			   } catch (SipException se) {
			      se.printStackTrace(); 
			   }
			}			
			return;
		}
		
		if(tid.getState() == TransactionState.COMPLETED)
			notify();
			
		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					System.out.println("Dialog after 200 OK  " + dialog);
					System.out.println("Dialog State after 200 OK  " + dialog.getState());
					ackRequest = dialog.createAck( ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber() );
					System.out.println("Sending ACK");
					dialog.sendAck(ackRequest);
					
					// JvB: test REFER, reported bug in tag handling
					//dialog.sendRequest(  sipProvider.getNewClientTransaction( dialog.createRequest("REFER") )); 
					
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the
						// dialog.
						System.out
								.println("Sending BYE -- cancel went in too late !!");
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

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		System.out.println("Transaction Time out");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
	 */
	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for "
				+ exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());

	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
	 */
	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
	 */
	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");

	}
}
