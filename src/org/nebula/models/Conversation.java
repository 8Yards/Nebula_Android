/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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
	}
	
	public String getRcl() {
		return rcl;
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
	
	public void setRcsl(String rcl) {
		this.rcl = rcl ;
	}

}

