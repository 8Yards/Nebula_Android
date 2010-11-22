package org.nebula.restClient;

public class RESTClient {
	private final String serverIP = "http://192.16.124.211/REST";
	private String username;
	private String password;
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public RESTClient() {
		this.username = "";
		this.password = "";
	}
	
	public RESTClient(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getServerIP() {
		return serverIP;
	}

}
