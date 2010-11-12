package org.nebula;

import java.text.ParseException;

import javax.sip.Dialog;
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
	private Dialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Request request;
		Response response;
		
		Log.v("nebula", "Start!");
		SIPClient sip;
		try {
			Log.v("nebula", "1");
			sip = new SIPClient("130.229.143.176", 5054, "michel", "192.16.124.217", "123", this);

			Log.v("nebula", "2");
			request = sip.register();
			Log.v("nebula", "3");
			response = sip.send(request);
			Log.v("nebula", "4");
			if(response.getStatusCode() == Response.UNAUTHORIZED) {
				Log.v("nebula", "unauthorized");				
				System.out.println("Authentication problem");
			}
			Log.v("nebula", "5");
			
			/*request = sip.invite("michel", "192.16.124.217");
			response = sip.send(request);*/
			
		} catch (Exception e) {
			Log.v("nebula", "6");
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
		else if (request.getMethod().equals(Request.INVITE)) {
			try {
				/*Log.v("nebula", "send 100");
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(100, request) );
				Log.v("nebula", "send 101");
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(101, request) );*/
				Log.v("nebula", "send 180");
				dialog = serverTransactionId.getDialog();
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(180, request) );
				Log.v("nebula", "send 200");
				serverTransactionId.sendResponse( SIPClient.getMessageFactory().createResponse(200, request) );

		        /*long lserverTransactionId = requestReceivedEvent.getTransactionId();
				sip.getSipProvider().sendResponse(lserverTransactionId, Response.RINGING);
				sip.getSipProvider().se
                Thread.sleep(500);
                sip.getSipProvider().sendResponse(lserverTransactionId, Response.OK);*/
				Log.v("nebula", "done");
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
