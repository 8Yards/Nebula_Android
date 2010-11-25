/*
 * author: prajwol kumar nakarmi
 */
package org.nebula.main;

import javax.sip.RequestEvent;

import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.sip.SIPInterface;
import org.nebula.models.MyIdentity;
import org.nebula.services.SIPClient;

import android.app.Application;

public class NebulaApplication extends Application implements SIPInterface {
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
			mySIPClient = new SIPClient(this);
		} catch (Exception e) {
			// TODO Handle gracefully
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		// TODO: implement this
	}

	public void reloadMyGroups() {
		RESTGroupManager rG = new RESTGroupManager();

		try {
			myIdentity.setMyGroups(rG.retrieveAllGroupsMembers());
		} catch (Exception e) {
			// do nothing here since the old group is preserved
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
