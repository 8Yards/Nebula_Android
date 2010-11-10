package org.nebula.sipClient;

import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SIPTest implements SIPInterface {
	private SIPClient sip;
	
	
	/*
	 * simply to test the SIP Client
	 */
	public void main(String args[]) throws Exception {
		Request request;
		Response response;
		
		System.out.println("Start!");
		SIPClient sip = new SIPClient("130.229.159.113", 5075, "michel", "130.229.159.113", "michel", this);

		request = sip.register();
		response = sip.send(request);
		if(response.getStatusCode() == Response.UNAUTHORIZED)
			System.out.println("Authentication problem");
		
		
		
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

	public void sipRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + SIPClient.getSipStack().getStackName()
				+ " with server transaction id " + serverTransactionId);
		
		if (request.getMethod().equals(Request.BYE))
			sip.processBye(request, serverTransactionId);
		else {
			try {
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(202,request) );
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
}
