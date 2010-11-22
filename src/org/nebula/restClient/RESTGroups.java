package org.nebula.restClient;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.userData.Group;
import org.nebula.userData.Groups;
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
//		JSONArray jsonArray = r.getResult().getJSONArray("0");
//		int size = jsonArray.length();
//		Log.e("nebula","" + size);
//	    ArrayList<JSONObject> arrays = new ArrayList<JSONObject>();
//	    for (int i = 0; i < size; i++) {
//	        JSONObject jsonObj = jsonArray.getJSONObject(i);
//	        Log.e("nebula","" + jsonObj);
//	            //Blah blah blah...
//	            arrays.add(jsonObj);
//	    }
//	    Log.e("nebula","after while");
//	    JSONObject[] jsons = new JSONObject[arrays.size()];
//	    arrays.toArray(jsons);
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