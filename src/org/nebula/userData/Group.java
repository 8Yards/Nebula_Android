package org.nebula.userData;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Group {
	private int id;
	private String groupName;
	private String groupStatus;
	private ArrayList<Profile> contacts;
	private int groupID;
	
	public Group(){
		this.groupName = "";
		this.groupID = 0;
		this.groupStatus =  "";
		this.contacts = null;
	}
	
	public Group(JSONObject jsonObject, String groupName,ArrayList<Profile> profiles) throws JSONException{
		this.groupName = groupName;
		this.groupID = jsonObject.getInt("id");
		this.groupStatus =  jsonObject.getString("status");
		this.contacts = profiles;
	}
	
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	/**
	 * @return the groupStatus
	 */
	
	public String getGroupStatus() {
		return groupStatus;
	}
	/**
	 * @param groupStatus the groupStatus to set
	 */
	public void setGroupStatus(String groupStatus) {
		this.groupStatus = groupStatus;
	}
	/**
	 * @return the contacts
	 */
	public ArrayList<Profile> getContacts() {
		return contacts;
	}
	/**
	 * @param contacts the contacts to set
	 */
	public void setContacts(ArrayList<Profile> contacts) {
		this.contacts = contacts;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
}
