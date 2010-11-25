/*
 * author: marco
 * debugging, refactoring: prajwol, marco
 */

package org.nebula.client.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.models.Group;

public class RESTGroupManager extends Resource {

	public RESTGroupManager() {
		super("RESTGroups");
	}

	public List<Group> retrieveAllGroupsMembers() throws JSONException,
			ClientProtocolException, IOException {
		Response r = this.get("retrieveAllGroupsMembers");
		List<Group> myGroups = new ArrayList<Group>();

		for (Iterator iterator = r.getResult().keys(); iterator.hasNext();) {
			String groupName = "" + iterator.next();
			JSONObject groupObj = r.getResult().getJSONObject(groupName);

			myGroups.add(new Group(groupObj, groupName));
		}

		return myGroups;
	}

}