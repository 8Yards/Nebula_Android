package org.nebula.sipClient;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private Request request;
	private ContactHeader contactHeader;
	private ClientTransaction Tid;
	private Dialog dialog;
    private Request ackRequest;// Save the created ACK request, to respond to retransmitted 2xx
    private String sipServerIP = "192.16.124.211";
    private Integer sipServerPort = 5060;

	private String sipServerName = "Server";
    private String myIP;
    private Integer myPort;
    private String mySIPName;
    private String mySIPDomain;
    private ListeningPoint localUDPListeningPoint;
	private String transport = "udp";
	private String myPassword;
	private Response response;
	private SIPInterface sipint;
	
	/*
	 * Constructor
	 * @param	fromIPAddress	IP address of the client
	 * @param	fromPort		port used by of the client
	 * @param	fromName		sip name of the client
	 * @param	fromDomain		sip domain of the client
	 */
    public SIPClient(String fromIPAddress, Integer fromPort, String fromName, 
    		String fromDomain, String password, SIPInterface sipint) throws Exception {
    	this.sipint = sipint;
    	this.myIP = fromIPAddress;
    	this.myPort = fromPort;
    	this.mySIPName = fromName;
    	this.mySIPDomain = fromDomain;
    	this.myPassword = password;
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
	
	public static String digest(String nonce, String realm, String username, String password, 
			String uri, String method) {
		String response;
		String ha1;
		String ha2;
		
		ha1 = md5(username +":"+ realm +":"+ password);
		ha2 = md5(method +":"+ uri);
		
		response = md5(ha1 +":"+ nonce +":"+ ha2);
		
		return response;
	}
	
	public static String md5(String plaintext) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		m.reset();
		m.update(plaintext.getBytes());
		BigInteger bigInt = new BigInteger(1, m.digest());
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 )
			hashtext = "0"+hashtext;
		return hashtext;
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

    	//Log.v("nebula", request.toString());
    	
		// Create the client transaction.
    	Request r = (Request)request.clone();
		Tid = sipProvider.getNewClientTransaction(request);
	
		//System.out.println(request);

		dialog = Tid.getDialog();
		
	    // send the request out.
	    Tid.sendRequest();
    	Log.v("nebula", "send a request");
		/*if(request.getMethod().equals("REGISTER")) {
	    	Log.v("nebula", "wait for register");
		}*/
    	wait();
    	Log.v("nebula", "done waiting");

		if (response.getStatusCode() == Response.UNAUTHORIZED) {
	    	Log.v("nebula", "unauthorized?");
			String nonce;
			String realm;
			
			System.out.println("Send authentication..");
			String ha = response.getHeader("WWW-Authenticate").toString();
			Pattern p = Pattern.compile("nonce=\"(.*?)\"");
			Matcher m = p.matcher(ha);
			if(m.find()) {
				nonce = m.group(1);
				
				p = Pattern.compile("realm=\"(.*?)\"");
				m = p.matcher(ha);
				if(m.find()) {
					realm = m.group(1);
					String username = this.mySIPName;
					String password = this.myPassword ;
					String uri = "sip:"+this.mySIPName+"@"+this.mySIPDomain;
					String method = r.getMethod();
					
					String aResponse = digest(nonce, realm, username, password, uri, method);
					
					String authorization = "Digest username=\""+username+"\", realm=\""+realm+"\", nonce=\""+nonce+"\", uri=\""+uri+"\", response=\""+aResponse+"\", algorithm=MD5";
					
					
					//r.setTransaction(null);
					
					try {
						AuthorizationHeader ah = headerFactory.createAuthorizationHeader(authorization);
						r.addHeader(ah);
					} catch (ParseException e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
					try {
						//System.out.println("-"+r);
						Tid = sipProvider.getNewClientTransaction(r);

						dialog = Tid.getDialog();
						
					    // send the request out.
					    Tid.sendRequest();
						wait();
						
						//wait();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
				}
			}
			else
				System.out.println("Error: No nonce provided for authentication..");
		}
		
	    return this.response;
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
		ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
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
		ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
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
	    Header extensionHeader = headerFactory.createHeader("Expires", "3600");
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
		Log.v("nebula", "Got a request");

		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
		Log.v("nebula", "Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
		//Log.v("nebula", response);
		
		sipint.processRequest(requestReceivedEvent);
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
		//Log.v("nebula", "Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		this.response = response;
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		//Log.v("nebula", "Response received : Status Code = "
		//		+ response.getStatusCode() + " " + cseq);
		//Log.v("nebula", response);
		Log.v("nebula", tid.getState().toString());
		if(tid.getState() == TransactionState.COMPLETED || 
				tid.getState() == TransactionState.TERMINATED) {
			notify();
		}
			
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
			
		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {if (response.getStatusCode() == Response.OK) {
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
		notify();
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
	

    /**
	 * @return the sipProvider
	 */
	public static SipProvider getSipProvider() {
		return sipProvider;
	}

	/**
	 * @param sipProvider the sipProvider to set
	 */
	public static void setSipProvider(SipProvider sipProvider) {
		SIPClient.sipProvider = sipProvider;
	}

	/**
	 * @return the addressFactory
	 */
	public static AddressFactory getAddressFactory() {
		return addressFactory;
	}

	/**
	 * @param addressFactory the addressFactory to set
	 */
	public static void setAddressFactory(AddressFactory addressFactory) {
		SIPClient.addressFactory = addressFactory;
	}

	/**
	 * @return the messageFactory
	 */
	public static MessageFactory getMessageFactory() {
		return messageFactory;
	}

	/**
	 * @param messageFactory the messageFactory to set
	 */
	public static void setMessageFactory(MessageFactory messageFactory) {
		SIPClient.messageFactory = messageFactory;
	}

	/**
	 * @return the headerFactory
	 */
	public static HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	/**
	 * @param headerFactory the headerFactory to set
	 */
	public static void setHeaderFactory(HeaderFactory headerFactory) {
		SIPClient.headerFactory = headerFactory;
	}

	/**
	 * @return the sipStack
	 */
	public static SipStack getSipStack() {
		return sipStack;
	}

	/**
	 * @param sipStack the sipStack to set
	 */
	public static void setSipStack(SipStack sipStack) {
		SIPClient.sipStack = sipStack;
	}

	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
	}

	/**
	 * @return the contactHeader
	 */
	public ContactHeader getContactHeader() {
		return contactHeader;
	}

	/**
	 * @param contactHeader the contactHeader to set
	 */
	public void setContactHeader(ContactHeader contactHeader) {
		this.contactHeader = contactHeader;
	}

	/**
	 * @return the tid
	 */
	public ClientTransaction getTid() {
		return Tid;
	}

	/**
	 * @param tid the tid to set
	 */
	public void setTid(ClientTransaction tid) {
		Tid = tid;
	}

	/**
	 * @return the dialog
	 */
	public Dialog getDialog() {
		return dialog;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * @return the ackRequest
	 */
	public Request getAckRequest() {
		return ackRequest;
	}

	/**
	 * @param ackRequest the ackRequest to set
	 */
	public void setAckRequest(Request ackRequest) {
		this.ackRequest = ackRequest;
	}

	/**
	 * @return the sipServerIP
	 */
	public String getSipServerIP() {
		return sipServerIP;
	}

	/**
	 * @param sipServerIP the sipServerIP to set
	 */
	public void setSipServerIP(String sipServerIP) {
		this.sipServerIP = sipServerIP;
	}

	/**
	 * @return the sipServerPort
	 */
	public Integer getSipServerPort() {
		return sipServerPort;
	}

	/**
	 * @param sipServerPort the sipServerPort to set
	 */
	public void setSipServerPort(Integer sipServerPort) {
		this.sipServerPort = sipServerPort;
	}

	/**
	 * @return the sipServerName
	 */
	public String getSipServerName() {
		return sipServerName;
	}

	/**
	 * @param sipServerName the sipServerName to set
	 */
	public void setSipServerName(String sipServerName) {
		this.sipServerName = sipServerName;
	}

	/**
	 * @return the myIP
	 */
	public String getMyIP() {
		return myIP;
	}

	/**
	 * @param myIP the myIP to set
	 */
	public void setMyIP(String myIP) {
		this.myIP = myIP;
	}

	/**
	 * @return the myPort
	 */
	public Integer getMyPort() {
		return myPort;
	}

	/**
	 * @param myPort the myPort to set
	 */
	public void setMyPort(Integer myPort) {
		this.myPort = myPort;
	}

	/**
	 * @return the mySIPName
	 */
	public String getMySIPName() {
		return mySIPName;
	}

	/**
	 * @param mySIPName the mySIPName to set
	 */
	public void setMySIPName(String mySIPName) {
		this.mySIPName = mySIPName;
	}

	/**
	 * @return the mySIPDomain
	 */
	public String getMySIPDomain() {
		return mySIPDomain;
	}

	/**
	 * @param mySIPDomain the mySIPDomain to set
	 */
	public void setMySIPDomain(String mySIPDomain) {
		this.mySIPDomain = mySIPDomain;
	}

	/**
	 * @return the localUDPListeningPoint
	 */
	public ListeningPoint getLocalUDPListeningPoint() {
		return localUDPListeningPoint;
	}

	/**
	 * @param localUDPListeningPoint the localUDPListeningPoint to set
	 */
	public void setLocalUDPListeningPoint(ListeningPoint localUDPListeningPoint) {
		this.localUDPListeningPoint = localUDPListeningPoint;
	}

	/**
	 * @return the transport
	 */
	public String getTransport() {
		return transport;
	}

	/**
	 * @param transport the transport to set
	 */
	public void setTransport(String transport) {
		this.transport = transport;
	}

	/**
	 * @return the myPassword
	 */
	public String getMyPassword() {
		return myPassword;
	}

	/**
	 * @param myPassword the myPassword to set
	 */
	public void setMyPassword(String myPassword) {
		this.myPassword = myPassword;
	}

	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * @return the sipint
	 */
	public SIPInterface getSipint() {
		return sipint;
	}

	/**
	 * @param sipint the sipint to set
	 */
	public void setSipint(SIPInterface sipint) {
		this.sipint = sipint;
	}
}
