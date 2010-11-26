/*
 * author: marco
 * debugging, refactoring: prajwol, marco
 */
 
package org.nebula.client.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.models.Group;
import org.nebula.models.Profile;

public class RESTGroupManager extends Resource {

	public RESTGroupManager() {
		super("RESTGroups");
	}

	public List<Group> retrieveAllGroupsMembers() throws JSONException,
			ClientProtocolException, IOException {
		Response r = this.get("retrieveAllGroupsMembers");
		List<Group> myGroups = new ArrayList<Group>();
		//scan the data for the name of the groups
		for (Iterator iterator = r.getResult().keys(); iterator.hasNext();) {
			String groupName = "" + iterator.next();
			//retrieve the JSON object and instantiates a new Group with it
			JSONObject groupObj = r.getResult().getJSONObject(groupName);
			myGroups.add(new Group(groupObj, groupName));
		}
		return myGroups;
	}
	
	/**
	*
	* @param group group information for the one to add + (contacts to add inside the group)
	* @return the response from the server (HTTPstatus + message)
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	*/
	public Status addNewGroup(Group group) throws ClientProtocolException, IOException, JSONException {
		//group insertion
		HashMap<String, Object> hM = new HashMap<String, Object>();
		hM.put("groupName", group.getGroupName());
		hM.put("status", group.getGroupStatus());
		
		Response r = this.post("insertGroup", hM);
		//201 = HTTP status code for user inserted
		if (r.getStatus() == 201) {
			//store the id of the brand new group for utilize it in inserting the user
			group.setId(Integer.parseInt(r.getResult().getString("id")));
			return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	/**
	 * @return status of the performed operation
	 */
	public Status retrieveGroup(Group g) throws JSONException, ClientProtocolException, IOException {
		HashMap<String, String> hM = new HashMap<String, String>();
		hM.put("id", "" + g.getId());
		Response r = this.get("retrieveGroup", hM);
		g.setGroupName("" + r.getResult().get("groupName"));
		g.setGroupStatus("" + r.getResult().get("status"));
		// HTTP valid return status
		if ((r.getStatus() >= 200) && (r.getStatus() < 300)) {
			return new Status(true, "Group retrieved successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	} 
	
	
	public List<Group> retrieveGroups() throws JSONException, ClientProtocolException, IOException {
		Response r = this.get("retrieveGroups");
		List<Group> myGroups = new ArrayList<Group>();
		
		// scan through the array and return the fetched group
		for (int i=0; i < r.getResult().length();i++)
		{
			Group group = new Group();
			JSONObject jSonObj = r.getResult().getJSONObject("" + i);
			group.setGroupName(jSonObj.getString("groupName"));
			group.setId(jSonObj.getInt("id"));
			group.setGroupStatus(jSonObj.getString("status"));
			myGroups.add(group);
		}
		return myGroups;
	} 
	
	/**
	 * Insert the Profiles that belongs to group object into it in the DB
	 * @param group
	 * @return
	 */
	public Status insertUsersIntoGroup(Group group) throws ClientProtocolException, IOException, JSONException{
		//for every user, perform the insert operation
		Response r = null;
		List<Profile> user = group.getContacts();
		for (int i=0; i<user.size(); i++)
		{
			HashMap<String, Object> hMGroupUser = new HashMap<String, Object>();
			hMGroupUser.put("userID", user.get(i).getId());
			hMGroupUser.put("groupID", group.getId());
			r = this.post("insertUserIntoGroup", hMGroupUser);
		}
		//201 = HTTP status for insertion performed correctly
		if (r.getStatus() == 201) {return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
	/**
	 * Insert a specific user into a group in DB
	 */
	public Status insertUserIntoGroup(Group group, String username) throws ClientProtocolException, IOException, JSONException {
		HashMap<String, Object> hMGroupUser = new HashMap<String, Object>();
		hMGroupUser.put("groupID", group.getId());
		hMGroupUser.put("username", username);
		Response r = this.post("insertUserIntoGroup", hMGroupUser);
		//201 = HTTP status for insertion performed correctly
		if (r.getStatus() == 201) {return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
	/**
	 * @param contactUsername user supposed to be added as contact
	 * @return status of the performed operation
	 */
	public Status addContact(String contactUsername) throws ClientProtocolException, IOException, JSONException {
		HashMap<String, Object> hMGroupUser = new HashMap<String, Object>();
		hMGroupUser.put("username", contactUsername);
		hMGroupUser.put("nickname", contactUsername);
		Response r = this.post("addContact", hMGroupUser);
		//201 = HTTP status for insertion performed correctly
		if (r.getStatus() == 201) {return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}

	/**
	 * @param contactUsername user supposed to be added as contact
	 * @param contactNickName nick of the user supposed to be added as contact
	 * @return status of the performed operation
	 */
	public Status addContact(String contactUsername, String contactNickName) throws ClientProtocolException, IOException, JSONException {
		HashMap<String, Object> hMGroupUser = new HashMap<String, Object>();
		hMGroupUser.put("username", contactUsername);
		hMGroupUser.put("nickname", contactNickName);
		Response r = this.post("addContact", hMGroupUser);
		//201 = HTTP status for insertion performed correctly
		if (r.getStatus() == 201) {
			return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
}