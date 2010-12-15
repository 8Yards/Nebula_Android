/*
 * author: marco
 * debugging and refactor: prajwol
 */
package org.nebula.models;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

//TODO: consistency in naming - contacts or profiles????

public class Group {
	private int id;
	private String groupName;
	private String groupStatus;
	private List<Profile> contacts = new ArrayList<Profile>();

	public Group() {
		this.groupName = "";
		this.id = 0;
		this.groupStatus = "";
		this.contacts = new ArrayList<Profile>();
	}
	
	public Group(int id, String groupName,  String groupStatus) {
		this();
		this.id = id;
		this.groupName = groupName;
		this.groupStatus = groupStatus;
		this.contacts = new ArrayList<Profile>();
	}
	
	public Group(JSONObject groupObj, String groupName) throws JSONException {
		this.groupName = groupName;
		this.id = groupObj.getInt("id");
		this.groupStatus = groupObj.getString("status");

		List<Profile> profiles = new ArrayList<Profile>();
		JSONObject profileObj = null;

		int profilesCount = groupObj.length() - 2; // :P there are two keys more
		for (int i = 0; i < profilesCount; i++) {
			profileObj = groupObj.getJSONObject("" + i);
			Profile prof = new Profile(profileObj);
			profiles.add(prof);
		}

		this.contacts = profiles;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(String groupStatus) {
		this.groupStatus = groupStatus;
	}

	public List<Profile> getContacts() {
		return contacts;
	}

	public void setContacts(List<Profile> contacts) {
		this.contacts = contacts;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public String toString(){
		return this.groupName;
	}
}
