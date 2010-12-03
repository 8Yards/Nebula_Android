/*
 * author - marco, michel
 * refactor - prajwol
 */
package org.nebula.client.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.models.Profile;

public class RESTProfileManager extends Resource {

	public RESTProfileManager() {
		super("RESTProfiles");
	}

	public Status register(Profile p) throws ClientProtocolException,
			IOException, JSONException {
		HashMap<String, Object> h = new HashMap<String, Object>();
		h.put("username", p.getUsername());
		h.put("password", p.getPassword());
		h.put("fullName", p.getFullName());
		h.put("phoneNumber", p.getPhoneNumber());
		h.put("address", p.getAddress());
		h.put("email_address", p.getEmail_address());

		Response r = this.post(h);
		if (r.getStatus() == 201) {
			p.setId(r.getResult().getInt("id"));
			return new Status(true, "Profile added successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
	public Status modify(Profile p) throws ClientProtocolException,
	IOException, JSONException {
		Map<String, Object> h = new HashMap<String, Object>();
		h.put("username", p.getUsername());
		if(p.getPassword()!="")
			h.put("password", p.getPassword());
		if(p.getFullName()!="")
			h.put("fullName", p.getFullName());
		if(p.getPhoneNumber()!="")
			h.put("phoneNumber", p.getPhoneNumber());
		if(p.getAddress()!="")
			h.put("address", p.getAddress());
		if(p.getEmail_address()!="")
			h.put("email_address", p.getEmail_address());
		
		Response r = this.put("modifyProfile", h);
		System.out.println(""+r.getResult());
		if (r.getStatus() == 201) {
			return new Status(true, "Profile modified successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
}