/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.util.Date;

/*
 * created by nina
 */
public class Conversation {
	private String id ;
	private Date date;
	private String rcl ;
		
	public Conversation(String id, String rcl) {
		this.id = id ;
		this.rcl = rcl ;
		this.date = new Date();
	}
	
	public String getId() {
		return id ;
	}
	
	public void setId(String id) {
		this.id = id ;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getRcl() {
		return rcl;
	}
	
	public void setRcl(String rcl) {
		this.rcl = rcl ;
	}
}

