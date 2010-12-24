/*
 * author: prajwol kumar nakarmi
 */
package org.nebula.main;

import java.net.DatagramSocket;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

import org.nebula.client.localdb.NebulaLocalDB;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rtp.RTPReceiver;
import org.nebula.client.rtp.RTPSender;
import org.nebula.client.rtp.RTPSender.SenderBinder;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.models.ConversationThread;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;

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

	private NebulaLocalDB myLocalDB = null;
	private MyIdentity myIdentity = null;
	private SIPClient mySIPClient = null;
	private RTPSession myRTPClient = null;
	private RTPSender myRTPSender = null;
	private RTPReceiver myRTPReceiver = null;

	public static NebulaApplication getInstance() {
		return singleton;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		singleton = this;

		myIdentity = new MyIdentity();
		myLocalDB = new NebulaLocalDB(this);

		try {
			myIdentity.loadConfiguration();
			mySIPClient = new SIPClient();
			mySIPClient.setEventHandler(this);

			bindService(new Intent(NebulaApplication.this, RTPSender.class),
					senderConnection, Context.BIND_AUTO_CREATE);
			Log.v("nebula", "nebulaAPP: " + "sender service started");
			startService(new Intent(NebulaApplication.this, RTPSender.class));

			// Instantiate RTP Client
			myRTPClient = new RTPSession(new DatagramSocket(myIdentity
					.getMyRTPPort()), new DatagramSocket(myIdentity
					.getMyRTPPort() + 1));
			myRTPClient.payloadType(8);

			myRTPReceiver = new RTPReceiver(myRTPClient); // TODO:: do we need
			// this??
		} catch (Exception e) {
			// TODO Handle gracefully
			Log.e("nebula", "nebulaApp: " + e.getMessage());
			System.exit(-1);
		}
	}

	public void processEvent(Object... params) {
		String eventName = params[0].toString();
		if (eventName.equals(SIPClient.NOTIFY_PRESENCE)) {
			sendBroadcast(new Intent(params[0].toString()).putExtra("params",
					params));
		} else if (eventName.equals(SIPClient.NOTIFY_INVITE)) {
			Log.v("nebula", "nebulaApp:" + eventName + "(" + params.length + ")");
			String threadId = (String) params[3];
			String convId = (String) params[4];
			String requestRCL = (String) params[5];

			ConversationThread thread;
			boolean shouldEstablishRTP = false;

			if (myIdentity.existsThread(threadId)) {
				thread = myIdentity.getThreadById(threadId);

				if (!thread.existsConversation(convId)) {
					thread.addConversation(convId, requestRCL);
					shouldEstablishRTP = false;
				} else {
					shouldEstablishRTP = true;
				}
			} else {
				thread = myIdentity.createThread(threadId);
				thread.addConversation(convId, requestRCL);
				shouldEstablishRTP = true;
			}

			if (shouldEstablishRTP == true) {
				establishRTP(params[1].toString(), Integer.valueOf(
						params[2].toString()).intValue());
			}

			sendBroadcast(new Intent(params[0].toString()));			
		}
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

	public void reloadConversation() {
		RESTConversationManager rC = new RESTConversationManager();

		try {
			myIdentity.setMyThreads(rC.retrieveAll());
		} catch (Exception e) {
			// do nothing here since the old conversation is preserved
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

	public void establishRTP(String destAddressRTP, int destPortRTP) {
		myRTPSender.setRtpSender(myRTPClient);

		Log.v("nebula", "nebulaApp: desc ip/prt = " + destAddressRTP + "/"
				+ destPortRTP);
		Participant p = new Participant(destAddressRTP, destPortRTP,
				destPortRTP + 1);
		int participants = myRTPSender.numberOfReceivers();
		myRTPClient.addParticipant(p);

		// TODO:: Need to wait for addParticipant.. check if this is
		// necessary
		while (participants == myRTPSender.numberOfReceivers()) {
		}

		myRTPClient.RTPSessionRegister(myRTPReceiver, null, null);

		// Start Recording and Playing RTP
		myRTPSender.startRecordingAndSending();
		myRTPReceiver.startPlaying();
	}

	private ServiceConnection senderConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			myRTPSender = ((SenderBinder) binder).getService();
			Log.v("nebula", "Sender Connected!");
		}

		public void onServiceDisconnected(ComponentName name) {
			myRTPSender = null;
			Log.v("nebula", "Sender Disconnected!");
		}
	};

	public NebulaLocalDB getMyLocalDB() {
		return myLocalDB;
	}
	
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
