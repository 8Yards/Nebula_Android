package org.nebula.test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import jlibrtp.Participant;

import org.nebula.sipClient.SIPClient;
import org.nebula.sipClient.SIPInterface;
import org.nebula.test.R;
import org.nebula.utils.Utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Test extends Activity implements SIPInterface {    
	Intent intentRecord;
	Intent intentPlay;
	private SIPClient sip;
	private String myName = "michel";
	private String myPassword = "michel";
	private String myAddress;
	private int myPort = 5062;
	protected ServiceSender serviceBinder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		intentRecord = new Intent(this, ServiceSender.class);
		intentPlay = new Intent(this, ServiceReceiver.class);
		
		Log.v("nebula", "Start!");
		try {
			myAddress = Utils.getLocalIpAddress();
			Log.v("nebula", myAddress);
			sip = new SIPClient(myAddress, myPort, myName, myAddress, myPassword, this);
			Request request = sip.invite("sujan", "192.16.124.211");
			Response response = sip.send(request);
			//Log.v("nebula", response.toString());
			if(response.getStatusCode() != 200 || response.getRawContent() == null) {
				Log.v("nebula", response.toString());
				Log.v("nebula", "Finish..");
				System.exit(-1);
			}
			String sdp = new String(response.getRawContent());
			Log.v("nebula", sdp);
			String ipAddress = SDPUtils.getIPAddress(sdp);
			int sendPortRTP = 6030;
			int sendPortRTCP = 6031;
			int receivePortRTP = 6032;
			int receivePortRTCP = 6033;
			
			intentRecord.putExtra("portRTP", sendPortRTP);
			intentRecord.putExtra("portRTCP", sendPortRTCP);
			intentRecord.putExtra("portRTCP", sendPortRTCP);
			intentPlay.putExtra("portRTP", receivePortRTP);
			intentPlay.putExtra("portRTCP", receivePortRTCP);
			
			ServiceConnection connection = new ServiceConnection() {
			    public void onServiceDisconnected(ComponentName name) { 
			        Log.v("nebula", "Disconnected!"); 
			        serviceBinder = null;
			    }

				public void onServiceConnected(ComponentName name, IBinder binder) {
			        Log.v("nebula", "Connected!"); 
			        serviceBinder = ((SenderBinder) binder).getService();
			        serviceReady();
				}  
			};
			    
			bindService(intentRecord, connection, Context.BIND_AUTO_CREATE);
			
			//startService(intentRecord);
			//startService(intentPlay);
		} catch (Exception e) {
			Log.v("nebula", "Error");
			e.printStackTrace();
		}
	}
	
	public void serviceReady() {
		//ServiceSender service = ((BackgroundServiceBinder)service).getService(); 
		Participant p = new Participant("130.229.131.172", 7078, 7079);
		serviceBinder.addParticipant(p);
		serviceBinder.startRecording();
		
		Log.v("nebula", "App finished.");
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
			
			if (request.getMethod().equals(Request.BYE)) {
				sip.processBye(request, serverTransactionId);
				serviceBinder.stopRecording();
			}
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
			/*
			 * http://en.wikipedia.org/wiki/Session_Description_Protocol
			 * Some examples
			 * String content = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                        + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                        + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";*/
			/*
			 * Captured from Linphone
			 * String content = "v=0\r\n"
					+ "o=prajwol 123456 654321 IN IP4 130.229.159.113\r\n"
					+ "s=A conversation\r\n"
					+ "c=IN IP4 130.229.159.113\r\n"
					+ "t=0 0\r\n"
					+ "m=audio 7078 RTP/AVP 0 101\r\n"
					+ "a=rtpmap:0 PCMU/8000/1\r\n"
					+ "a=rtpmap:101 telephone-event/8000/1\r\n"
					+ "m=video 0 RTP/AVP 0\r\n";*/
			
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