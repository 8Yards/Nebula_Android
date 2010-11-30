package org.nebula.utils;

import java.net.SocketException;
import java.text.ParseException;

import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import jlibrtp.Participant;

import org.nebula.rtpClient.RTPClient;
import org.nebula.sipClient.SDPUtils;
import org.nebula.sipClient.SIPClient;
import org.nebula.sipClient.SIPInterface;
import org.nebula.test.R;
import org.nebula.utils.Config;

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
import android.widget.EditText;
import android.widget.LinearLayout;

public class Test extends Activity implements SIPInterface {
	private Test thisClass = this;
	
	private Intent intentRecord;
	private Intent intentPlay;
	protected ServiceSender serviceSenderBinder;
	protected ServiceReceiver serviceReceiverBinder;
	
	private SIPClient sip = null;
	private RTPClient rtpClient;
	
	private String myName = Config.getUsername();
	private String myPassword = Config.getPassword();
	private String myAddress;
	private int myPort = Config.getLocalSIPPort();
	
	private String destAddressRTP;
	private int destPortRTP;
	
	private EditText etUsername;
	private EditText etPassword;
	private EditText etCallee;
	private LinearLayout rlRegister;
	private LinearLayout rlCallee;
	
	private Status status;
	private enum Status {
		AVAILABLE, UNAVAILABLE
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			myAddress = Config.getLocalIpAddress();
			status = Status.AVAILABLE;
			
			intentRecord = new Intent(this, ServiceSender.class);
			intentPlay = new Intent(this, ServiceReceiver.class);
			
			Button btnRegister = (Button)findViewById(R.id.btnRegister);
			Button btnCall = (Button)findViewById(R.id.btnCall);
			Button btnHangup = (Button)findViewById(R.id.btnHangup);
			etUsername = (EditText)findViewById(R.id.etUsername);
			etPassword = (EditText)findViewById(R.id.etPassword);
			etCallee = (EditText)findViewById(R.id.etCallee);
			rlRegister = (LinearLayout)findViewById(R.id.rlRegister);
			rlCallee = (LinearLayout)findViewById(R.id.rlCallee);
			Button btnExit = (Button)findViewById(R.id.btnExit);
			
			btnExit.setOnClickListener(new View.OnClickListener() {		
				public void onClick(View v) {
					System.exit(-1);
				}
			});
			
			btnRegister.setOnClickListener(new View.OnClickListener() {		
				public void onClick(View v) {
						try {
							myName = etUsername.getText().toString();
							myPassword = etPassword.getText().toString();
							if( sip == null )
								sip = new SIPClient(myAddress, myPort, myName, myAddress, myPassword, thisClass);
							if(register()) {
								Log.d("nebula", "register success");
								rlCallee.setVisibility(View.VISIBLE);
								rlRegister.setVisibility(View.INVISIBLE);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			
			btnCall.setOnClickListener(new View.OnClickListener() {		
				public void onClick(View v) {
						try {
							Log.i("nebula", etCallee.getText().toString());
							invite(etCallee.getText().toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			
			btnHangup.setOnClickListener(new View.OnClickListener() {		
				public void onClick(View v) {
						try {
							if(sip != null)
								sip.bye();
						} catch (Exception e) {
							e.printStackTrace();
						}
						finally {
							serviceSenderBinder.stopRecording();
							status = Status.AVAILABLE;
						}
					}
				});
		} catch (Exception e) {
			Log.v("nebula", "Error");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/*
	 * Test SIP authentication
	 * @return	true	if authentication successes
	 */
	private boolean register() throws Exception {
		Response r = sip.register();
		if( r != null && r.getStatusCode() == 200 )
			return true;
		return false;
	}
	
	/*
	 * Send an invitation
	 */
	private void invite(String callee) throws Exception {
		Log.i("nebula", "sending invite");
		Response response = sip.invite(callee);
		if(response == null)
			throw new Exception("Invite failed");
		if(response.getStatusCode() != 200 || response.getRawContent() == null) {
			Log.i("nebula", "could not invite");
		}
		
		//"Parse" SDP
		// TODO Good Parsing
		String sdp = new String(response.getRawContent());
		Log.v("nebula", sdp);
		destAddressRTP = SDPUtils.retrieveIP(sdp);
		destPortRTP = SDPUtils.retrievePort(sdp);
		Log.v("nebula", destAddressRTP);
		Log.v("nebula", Integer.toString(destPortRTP));
		status = Status.UNAVAILABLE;
		
		establishRTP();
	}
	
	/*
	 * Establish an RTP channel
	 */
	private void establishRTP() {
		//Connect to Service Sender
		ServiceConnection connection = new ServiceConnection() {
		    public void onServiceDisconnected(ComponentName name) { 
		        Log.v("nebula", "Sender Disconnected!"); 
		        serviceSenderBinder = null;
		    }

			public void onServiceConnected(ComponentName name, IBinder binder) {
		        Log.v("nebula", "Sender Connected!");
		        serviceSenderBinder = ((SenderBinder) binder).getService();
		        serviceSenderReady();
			}
		};
		    
		startService(intentRecord);
		bindService(intentRecord, connection, Context.BIND_AUTO_CREATE);
	}

	/*
	 * Called when service Sender is ready
	 */
	public void serviceSenderReady() {
		//Connect to Server Receiver
		ServiceConnection connection = new ServiceConnection() {
		    public void onServiceDisconnected(ComponentName name) { 
		        Log.v("nebula", "Receiver Disconnected!"); 
		        serviceReceiverBinder = null;
		    }

			public void onServiceConnected(ComponentName name, IBinder binder) {
		        Log.v("nebula", "Receiver Connected!");
		        serviceReceiverBinder = ((ReceiverBinder) binder).getService();
		        serviceReceiverReady();
			}
		};

		startService(intentPlay);
		bindService(intentPlay, connection, Context.BIND_AUTO_CREATE);
	}
	
	/*
	 * Called when service Receiver is ready
	 */
	public void serviceReceiverReady() {
		try {
			//Instantiate RTP Client
			rtpClient = new RTPClient(Config.getLocalRTPPort(), Config.getLocalRTPPort()+1, null, false);
			serviceSenderBinder.setRTPClient(rtpClient);
			Log.d("nebula", "-"+destAddressRTP+"-");
			Participant p = new Participant(destAddressRTP.trim(), destPortRTP, destPortRTP+1);
			int participants = serviceSenderBinder.numberOfReceivers();
			serviceSenderBinder.addParticipant(p);
			//Need to wait for addParticipant..
			while( participants == serviceSenderBinder.numberOfReceivers() ) {}
			
			rtpClient.RTPSessionRegister(serviceReceiverBinder, null, null);
			
			//Start Recording and Playing RTP
			serviceSenderBinder.startRecording();
			serviceReceiverBinder.startPlaying();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nebula.sipClient.SIPInterface#processBye(javax.sip.message.Request, javax.sip.ServerTransaction)
	 */
	public void processBye(Request request, ServerTransaction serverTransactionId) {
		Log.i("nebula", "process bye");
		try {
			if (serverTransactionId == null) {
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = SIPClient.getMessageFactory().createResponse(200, request);
			serverTransactionId.sendResponse(response);
			/*System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());*/
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		} finally {
			serviceSenderBinder.stopRecording();
			status = Status.AVAILABLE;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nebula.sipClient.SIPInterface#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestReceivedEvent) {
		Log.i("nebula", "Got a request");
		
		Log.i("nebula", "\nRequest " + requestReceivedEvent.getRequest().getMethod());
		
		try {
			Request request = requestReceivedEvent.getRequest();
			ServerTransaction serverTransactionId = requestReceivedEvent
					.getServerTransaction();
			if (serverTransactionId == null)
					serverTransactionId = SIPClient.getSipProvider().getNewServerTransaction(request);
			
			if (request.getMethod().equals(Request.BYE)) {
				this.processBye(request, serverTransactionId);
				serviceSenderBinder.stopRecording();
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

	/*
	 * Process an INVITE request
	 * @param	request	The INVITE request
	 * @param	st		transaction id
	 */
	private void processInvite(Request request, ServerTransaction st) throws SipException, InvalidArgumentException, ParseException {
		//Send "trying" to server while waiting for decision
		Log.v("nebula", "process invite");
		st.sendResponse( SIPClient.getMessageFactory().createResponse(Response.RINGING, request) );
		// TODO Give the choice
		if(status == Status.AVAILABLE)
			processInviteOK(request, st);
		else
			processInviteBusy(request, st);
			
		// TODO processInviteCancel(request, st);
		// TODO processBusyHere(request, st);
	}

	/*
	 * Process invite if busy
	 */
	private void processInviteBusy(Request request, ServerTransaction st) {
		Response response;
		try {
			response = SIPClient.getMessageFactory().createResponse(Response.BUSY_HERE, request);
			st.sendResponse(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Reply OK to INVITE
	 * @param	request	The INVITE request
	 * @param	st		transaction id
	 */
	private void processInviteOK(Request request, ServerTransaction st) {
		try {
			Thread.sleep(2000);
			status = Status.UNAVAILABLE;
			//Send OK
			HeaderFactory headerFactory = SIPClient.getHeaderFactory();
			Response response = SIPClient.getMessageFactory().createResponse(Response.OK, request);
			Address address = SIPClient.getAddressFactory().createAddress(myName+" <sip:"
					+ myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			
			String sendingSDP = "v=0\r\n"
				+"o="+Config.getUsername()+" 122456 654221 IN IP4 "+Config.getLocalIpAddress()+"\r\n"
				+"s=A conversation\r\n"
				+"c=IN IP4 "+Config.getLocalIpAddress()+"\r\n"
				+"t=0 0\r\n"
				+"m=audio "+Config.getLocalRTPPort()+" RTP/AVP 8\r\n"
				+"a=rtpmap:8 PCMA/8000/1";
			
			byte[] content = sendingSDP.getBytes();
			
			if (content != null) {
			    ContentTypeHeader contentTypeHeader =
				headerFactory.createContentTypeHeader("application", "sdp");
			    response.setContent(content, contentTypeHeader);
			}
			
			Log.i("nebula", "sending response OK");
			st.sendResponse(response);
			//Log.v("nebula", (String)response.getContent());
			
			//"Parse" SDP
			// TODO Good Parsing
			String receivedSDP = new String(request.getRawContent());
			Log.v("nebula", receivedSDP);
			destAddressRTP = SDPUtils.retrieveIP(receivedSDP);
			destPortRTP = SDPUtils.retrievePort(receivedSDP);
			Log.v("nebula", destAddressRTP);
			Log.v("nebula", Integer.toString(destPortRTP));
			
			establishRTP();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}