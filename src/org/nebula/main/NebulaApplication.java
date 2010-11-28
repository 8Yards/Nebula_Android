/*
 * author: prajwol kumar nakarmi
 */
package org.nebula.main;

import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NebulaApplication extends Application implements
		NebulaEventHandler {
	private static NebulaApplication singleton;

	private MyIdentity myIdentity = null;
	private SIPClient mySIPClient = null;

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
		} catch (Exception e) {
			// TODO Handle gracefully
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void processEvent(Object... params) {
		Log.v("nebula", "nebulaApp:" + params[0].toString() + "("
				+ params.length + ")");
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
