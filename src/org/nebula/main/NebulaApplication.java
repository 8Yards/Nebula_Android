/*
 * author: prajwol kumar nakarmi
 */
package org.nebula.main;

import java.util.List;

import javax.sip.RequestEvent;

import org.json.JSONException;
import org.nebula.client.rest.RESTClient;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.sip.SIPInterface;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.services.SIPClient;

import android.app.Application;
import android.util.Log;

public class NebulaApplication extends Application implements SIPInterface {
	private static NebulaApplication singleton;

	private MyIdentity myIdentity = null;
	private List<Group> myGroups = null;
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
		RESTGroupManager rG = new RESTGroupManager(new RESTClient(
				myIdentity.getMySIPName(), myIdentity.getMyPassword()));

		try {
			myGroups = rG.retrieveAllGroupsMembers();
			Log.v("nebula", "Groups found: " + myGroups.size() + " Profiles found in first group: " + myGroups.get(0).getContacts().size());
		} catch (JSONException e) {
			Log.e("nebula", "Groups cannot be fetched: " + e.getMessage());
			myGroups = null;
		}
	}

	public MyIdentity getMyIdentity() {
		return myIdentity;
	}

	public void setMyIdentity(MyIdentity myIdentity) {
		this.myIdentity = myIdentity;
	}

	public List<Group> getMyGroups() {
		return myGroups;
	}

	public void setMyGroups(List<Group> myGroups) {
		this.myGroups = myGroups;
	}

	public SIPClient getMySIPClient() {
		return mySIPClient;
	}

	public void setMySIPClient(SIPClient mySIPClient) {
		this.mySIPClient = mySIPClient;
	}
}
