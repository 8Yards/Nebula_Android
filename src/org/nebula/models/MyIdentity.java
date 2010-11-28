/*
 * author: prajwol kumar nakarmi, michel hognurand, saad aali
 */

package org.nebula.models;

import java.util.ArrayList;
import java.util.List;
import org.nebula.utils.Utils;

public class MyIdentity {
	private String myIP;
	private int mySIPPort;
	private int myRTPPort;
	private String myUserName;
	private String myPassword;
	private String mySIPDomain;

	private String sipServerIP;
	private Integer sipServerPort;
	private String sipServerName;
	
	private String restServerIP;

	private List<Group> myGroups = new ArrayList<Group>();

	public void loadConfiguration() throws Exception {
		myIP = Utils.getLocalIpAddress();
		mySIPPort = Utils.getNextRandomPort();
		myRTPPort = Utils.getNextRandomPort();

		// TODO: externalize this
		mySIPDomain = "192.16.124.211";
		sipServerIP = "192.16.124.211";
		sipServerPort = 5060;
		sipServerName = "Opensips";
		
		restServerIP = "http://192.16.124.211/REST";
	}

	public String getMySIPURI() {
		return "sip:" + myUserName + "@" + mySIPDomain;
	}

	public String getMyIP() {
		return myIP;
	}

	public void setMyIP(String myIP) {
		this.myIP = myIP;
	}

	public int getMySIPPort() {
		return mySIPPort;
	}

	public void setMySIPPort(int mySIPPort) {
		this.mySIPPort = mySIPPort;
	}

	public int getMyRTPPort() {
		return myRTPPort;
	}

	public void setMyRTPPort(int myRTPPort) {
		this.myRTPPort = myRTPPort;
	}

	public String getMyUserName() {
		return myUserName;
	}

	public void setMyUserName(String myUserName) {
		this.myUserName = myUserName;
	}

	public String getMyPassword() {
		return myPassword;
	}

	public void setMyPassword(String myPassword) {
		this.myPassword = myPassword;
	}

	public String getMySIPDomain() {
		return mySIPDomain;
	}

	public void setMySIPDomain(String mySIPDomain) {
		this.mySIPDomain = mySIPDomain;
	}

	public String getSipServerIP() {
		return sipServerIP;
	}

	public void setSipServerIP(String sipServerIP) {
		this.sipServerIP = sipServerIP;
	}

	public Integer getSipServerPort() {
		return sipServerPort;
	}

	public void setSipServerPort(Integer sipServerPort) {
		this.sipServerPort = sipServerPort;
	}

	public String getSipServerName() {
		return sipServerName;
	}

	public void setSipServerName(String sipServerName) {
		this.sipServerName = sipServerName;
	}

	public List<Group> getMyGroups() {
		return myGroups;
	}

	public void setMyGroups(List<Group> myGroups) {
		this.myGroups = myGroups;
	}

	public String getRestServerIP() {
		return restServerIP;
	}

	public void setRestServerIP(String restServerIP) {
		this.restServerIP = restServerIP;
	}

}