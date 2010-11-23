package org.nebula.restClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.userData.Group;
import org.nebula.userData.Groups;
import org.nebula.userData.Profile;
import org.nebula.userData.Profiles;

import android.util.Log;
import android.util.TypedValue;

public class RESTGroups extends Resource {

	public RESTGroups(RESTClient rc) {
		super(rc);
	}
	
	/**
	 * 
	 * @param group group information for the one to add + (contacts to add inside the group)
	 * @param ownerUserID userID of the creator of the group
	 * @return the response from the server (HTTPstatus + message)
	 */
	public Response addNewGroup(Group group) {
		//group insertion
		HashMap<String, Object> hM = new HashMap<String, Object>();
		hM.put("groupName", group.getGroupName());
		hM.put("status", group.getGroupStatus());
		//if something goes wrong return, without inserting the user into
		Response r = this.post("insertGroup", hM);
		if(r.getStatus()!=201)
			return r;
		//store the id of the brand new group for utilize it in inserting the user
		try {
			group.setId(Integer.parseInt(r.getResult().getString("id")));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return r;
		
	}
	public Response retrieveGroup(Group g) throws JSONException {
		HashMap<String, String> hM = new HashMap<String, String>();
		hM.put("id", "" + g.getId());
		Response r = this.get("retrieveGroup", hM);
		g.setGroupName("" + r.getResult().get("groupName"));
		g.setGroupStatus("" + r.getResult().get("status"));
		return r;
	} 
	
	public Response retrieveGroups() throws JSONException {
		Response r = this.get("retrieveGroups");

		Log.e("nebula", "" + r.getResult().getJSONObject("0").get("id"));
		for (int i=0; i < r.getResult().length();i++)
		{
			Group group = new Group();
			JSONObject jSonObj = r.getResult().getJSONObject("" + i);
			group.setGroupName(jSonObj.getString("groupName"));
			group.setId(jSonObj.getInt("id"));
			group.setGroupStatus(jSonObj.getString("status"));
			Groups.add(group);
		}
		return r;
	} 
	
public Response retrieveAllGroupsMembers() throws JSONException {
		
	Response r = this.get("retrieveAllGroupsMembers");
//	Log.e("nebula", ""+r.getStatus());
//	Log.e("nebula", ""+r.getResult());
//	Log.e("nebula", ""+r.getResult().getJSONObject("Group1"));
//	Log.e("nebula", ""+r.getResult().getJSONObject("Group1").getJSONObject(""+0));
	
for (Iterator iterator = r.getResult().keys(); iterator.hasNext();) {
	//Log.e("nebula", ""+iterator.next());
	String groupName = ""+iterator.next();
	JSONObject jsonObj = r.getResult().getJSONObject(groupName);
	
	
	//group.setId(jSonObj1.getInt("id"));
	Profiles profiles = new Profiles();
	JSONObject jsonObj1 = null;
	//the length of the array o
	int limit = jsonObj.length()/4;
	for (int i=0; i < jsonObj.length()/4;i++)
	{
		//retrieve all the group members
		jsonObj1 = jsonObj.getJSONObject("" + i);
		Log.e("nebula", "len:" + jsonObj.length());
		Profile prof = new Profile(jsonObj1);
		
		profiles.add(prof);
	}
	
	//retrive Group name, id and status
	Group group = new Group(jsonObj, groupName, profiles.returnProfiles());
	group.setContacts(profiles.returnProfiles());
	Groups.add(group);
	Log.e("nebula", "group Added");
	}
	return r;
}
	/**
	 * 
	 * @param user List
	 * @return
	 */
	public Response insertUsersIntoGroup(Group group){
		//for every user, perform the insert operation
		Response groupUser = null;
		ArrayList<Profile> user = group.getContacts();
		for (int i=0; i<user.size(); i++)
		{
			HashMap<String, Object> hMGroupUser = new HashMap<String, Object>();
			 	
			hMGroupUser.put("userID", user.get(i).getId());
			hMGroupUser.put("groupID", group.getId());
			groupUser = this.post("insertUserIntoGroup", hMGroupUser);
			Log.e("nebula",""+groupUser.getStatus());
//			if(groupUser.getStatus()!=201)
//				return groupUser;
		}
		
		return groupUser;
	}
}