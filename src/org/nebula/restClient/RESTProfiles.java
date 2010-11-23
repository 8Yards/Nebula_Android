package org.nebula.restClient;

import java.util.HashMap;

import org.json.JSONException;
import org.nebula.userData.Profile;
import org.nebula.userData.Profiles;

import android.util.Log;

public class RESTProfiles extends Resource {

	public RESTProfiles(RESTClient rc) {
		super(rc);
	}

	/*
	 * A function to test the HTTP POST request
	 */
	public void testpost() {
		this.post(this.data);
	}

	public Response register(Profile p) {
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("username", p.getUsername());
		h.put("password", p.getPassword());
		h.put("fullName", p.getFullName());
		h.put("phoneNumber", p.getPhoneNumber());
		h.put("address", p.getAddress());
		h.put("email_address", p.getEmail_address());
		Response r = this.post(h);
		//if(r.getStatus()>=300)
			//return r;
		
		
		try {
			//Log.v("nebula",(String)r.getResult().get("id"));
			p.setId((Integer) r.getResult().get("id"));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	public Response retrieveAllMyGroups() {
		return this.get("retrieveGroups");
	}

}