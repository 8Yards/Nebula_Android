package org.nebula;

import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.nebula.sipClient.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SIPTest extends Activity implements SIPInterface {
	private SIPClient sip;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Request request;
		Response response;
		
		Log.v("nebula", "Start!");
		SIPClient sip;
		try {
			sip = new SIPClient("130.229.143.63", 5054, "testSIP", "130.229.159.113", "testSIP", this);

			request = sip.register();
			response = sip.send(request);
			if(response.getStatusCode() == Response.UNAUTHORIZED)
				System.out.println("Authentication problem");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

	public void sipRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		Log.v("nebula", "\n\nRequest " + request.getMethod()
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
