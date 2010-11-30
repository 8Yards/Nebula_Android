/*
 * author: prajwol kumar nakarmi
 */
package org.nebula.main;

import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;
import org.nebula.services.ServiceReceiver;
import org.nebula.services.ServiceSender;
import org.nebula.services.ServiceReceiver.ReceiverBinder;
import org.nebula.services.ServiceSender.SenderBinder;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class NebulaApplication extends Application implements
		NebulaEventHandler {
	private static NebulaApplication singleton;

	private MyIdentity myIdentity = null;
	private SIPClient mySIPClient = null;
	private RTPSession myRTPClient = null;
	private ServiceSender serviceSenderBinder = null;
	private ServiceReceiver serviceReceiverBinder = null;

	public static NebulaApplication getInstance() {
		return singleton;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		singleton = this;

		myIdentity = new MyIdentity();

		try {
			myIdentity.loadConfiguration();
			mySIPClient = new SIPClient();
			mySIPClient.setEventHandler(this);

			bindService(
					new Intent(NebulaApplication.this, ServiceSender.class),
					senderConnection, Context.BIND_AUTO_CREATE);
			bindService(new Intent(NebulaApplication.this,
					ServiceReceiver.class), receiverConnection,
					Context.BIND_AUTO_CREATE);
			
			startService(new Intent(NebulaApplication.this, ServiceSender.class));

		} catch (Exception e) {
			// TODO Handle gracefully
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void processEvent(Object... params) {
		Log.v("nebula", "nebulaApp:" + params[0].toString() + "("
				+ params.length + ")");
		--handle invite--
		sendBroadcast(new Intent(params[0].toString()).putExtra("params",
				params));
	}

	public void reloadMyGroups() {
		RESTGroupManager rG = new RESTGroupManager();

		try {
			myIdentity.setMyGroups(rG.retrieveAllGroupsMembers());
			renewMyPresenceSubscriptions();
		} catch (Exception e) {
			// do nothing here since the old group is preserved
			Log.e("nebula", e.getMessage());
		}
	}

	private void renewMyPresenceSubscriptions() {
		for (Group individualGroup : myIdentity.getMyGroups()) {
			for (Profile individualProfile : individualGroup.getContacts()) {
				SIPManager.doSubscribe(individualProfile.getUsername(),
						myIdentity.getMySIPDomain());
			}
		}
	}

	private void establishRTP() {
		

		try {
			// Instantiate RTP Client
			myRTPClient = new RTPSession(new DatagramSocket(myIdentity
					.getMyRTPPort()), new DatagramSocket(myIdentity
					.getMyRTPPort() + 1));

			serviceSenderBinder.setRtpSender(myRTPClient);

			Participant p = new Participant(destAddressRTP, destPortRTP,
					destPortRTP + 1);
			int participants = serviceSenderBinder.numberOfReceivers();
			myRTPClient.addParticipant(p);
			// TODO:: Need to wait for addParticipant.. check if this is
			// necessary
			while (participants == serviceSenderBinder.numberOfReceivers()) {
			}

			myRTPClient.RTPSessionRegister(serviceReceiverBinder, null, null);

			// Start Recording and Playing RTP
			serviceSenderBinder.startRecording();
			serviceReceiverBinder.startPlaying();
		} catch (SocketException e) {
			Log.e("nebula", "nebulaApp: " + e.getMessage());
		}
	}

	private ServiceConnection senderConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			serviceSenderBinder = ((SenderBinder) binder).getService();
			Log.v("nebula", "Sender Connected!");
		}

		public void onServiceDisconnected(ComponentName name) {
			serviceSenderBinder = null;
			Log.v("nebula", "Sender Disconnected!");
		}
	};

	private ServiceConnection receiverConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			serviceReceiverBinder = ((ReceiverBinder) binder).getService();
			Log.v("nebula", "Receiver Connected!");
		}

		public void onServiceDisconnected(ComponentName name) {
			serviceReceiverBinder = null;
			Log.v("nebula", "Receiver Disconnected!");
		}
	};

	public MyIdentity getMyIdentity() {
		return myIdentity;
	}

	public void setMyIdentity(MyIdentity myIdentity) {
		this.myIdentity = myIdentity;
	}

	public SIPClient getMySIPClient() {
		return mySIPClient;
	}

	public void setMySIPClient(SIPClient mySIPClient) {
		this.mySIPClient = mySIPClient;
	}
}
