package org.nebula.sipClient.test;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.nebula.sipClient.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SIPTest extends Activity implements SIPInterface {
	private SIPClient sip;
	private String myName = "Michel";
	private String myAddress = "130.229.157.39";
	private int myPort = 5060;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v("nebula", "Start!");
		try {
			Log.v("nebula", "1");
			sip = new SIPClient(myAddress, myPort, myName, myAddress, "123", this);

			Log.v("nebula", "2");
			/*request = sip.register();
			Log.v("nebula", "3");
			response = sip.send(request);
			Log.v("nebula", "4");
			if(response.getStatusCode() == Response.UNAUTHORIZED) {
				Log.v("nebula", "unauthorized");				
				System.out.println("Authentication problem");
			}*/
			Log.v("nebula", "5");
			
			/*request = sip.invite("michel", "192.16.124.217");
			response = sip.send(request);*/
			
		} catch (Exception e) {
			Log.v("nebula", "6");
			e.printStackTrace();
		}
		
		Log.v("nebula", "waiting..");
		
		boolean wait = true;
		while(wait){}
		
/*
		request = sip.invite("nina", "130.229.159.97");

		response = sip.send(request);
		
		sip.bye();*/
		
		/*request = sip.refer("nina", "130.229.159.97", "sdfdsf", "baba.com");

		response = sip.send(request);*/
		
		//System.out.println(response);
		System.out.println("End!");
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		try {
			Request request = requestReceivedEvent.getRequest();
			ServerTransaction serverTransactionId = requestReceivedEvent
					.getServerTransaction();
			if (serverTransactionId == null)
					serverTransactionId = SIPClient.getSipProvider().getNewServerTransaction(request);
	
			Log.v("nebula", "\n\nRequest " + request.getMethod()
					+ " received at " + SIPClient.getSipStack().getStackName()
					+ " with server transaction id " + serverTransactionId);
			
			if (request.getMethod().equals(Request.BYE))
				sip.processBye(request, serverTransactionId);
			else if (request.getMethod().equals(Request.INVITE))
				processInvite(request, serverTransactionId);
			else
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(202,request) );
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
	}

	private void processInvite(Request request, ServerTransaction st) {
		/*Log.v("nebula", "send 100");
		serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(100, request) );
		Log.v("nebula", "send 101");
		serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(101, request) );*/
		Log.v("nebula", "send 180");
		//dialog = serverTransactionId.getDialog();
		
		try {
			st.sendResponse( SIPClient.getMessageFactory().createResponse(Response.RINGING, request) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		processInviteOK(request, st);
		//processInviteCancel(request, st);
		//processBusyHere(request, st);
		
		Log.v("nebula", "done");
	}

	private void processInviteOK(Request request, ServerTransaction st) {
		try {
			Thread.sleep(1000);
			Log.v("nebula", "send 200");
			HeaderFactory headerFactory = SIPClient.getHeaderFactory();
			Response response = SIPClient.getMessageFactory().createResponse(Response.OK, request);
			Address address = SIPClient.getAddressFactory().createAddress(myName+" <sip:"
					+ myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			
			byte[] content = request.getRawContent();
			/*String content = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                        + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                        + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";*/
			if (content != null) {
			    ContentTypeHeader contentTypeHeader =
				headerFactory.createContentTypeHeader("application", "sdp");
			    System.out.println("response = " + response);
			    response.setContent(content, contentTypeHeader);
			}
			
			st.sendResponse( response );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
