package org.nebula.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile {
	private int id;
	private String username;
	private String domain;
	private String fullName;
	private String status;
	private String phoneNumber;
	private String sipURI;
	private String address;
	private String password;
	private String email_address;

	public Profile() {
		id = 0;
		username = "";
		domain = "";
		fullName = "";
		status = "";
		phoneNumber = "";
		sipURI = "";
		address = "";
		password = "";
		email_address = "";
	}

	public Profile(JSONObject jsonObject) throws JSONException {
		this.address = jsonObject.getString("address");
		this.domain = jsonObject.getString("domain");
		this.email_address = jsonObject.getString("email_address");
		this.fullName = jsonObject.getString("fullName");
		this.phoneNumber = jsonObject.getString("phoneNumber");
		this.status = jsonObject.getString("status");
		this.username = jsonObject.getString("username");

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getSipURI() {
		return sipURI;
	}

	public void setSipURI(String sipURI) {
		this.sipURI = sipURI;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}
}
