package org.nebula.userData;

import java.util.ArrayList;

public class Group {
	private int id;
	private String groupName;
	private String groupStatus;
	private ArrayList<Profile> contacts;
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
