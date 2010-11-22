package org.nebula.restClient;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.nebula.userData.Group;
import org.nebula.userData.Profile;

import android.util.Log;

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