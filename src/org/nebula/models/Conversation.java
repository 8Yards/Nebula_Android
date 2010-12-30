/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.main.NebulaApplication;

public class Conversation {
	private int id;
	private Date date;
	private List<String> callee;
	private String conversationName;
	private String callId = "";

	private ConversationThread thread;
	private MyIdentity myIdentity = NebulaApplication.getInstance()
			.getMyIdentity();

	public Conversation(JSONObject convObj, String conversationName)
			throws JSONException, ParseException {
		this.callee = new ArrayList<String>();
		this.id = convObj.getInt("id");
		this.conversationName = conversationName;
		this.setDate(convObj.getString("date"));

		JSONObject calleeElement = convObj.getJSONObject("callees");
		for (int i = 0; i < calleeElement.length(); i++) {
			JSONObject user = calleeElement.getJSONObject("" + i);
			callee.add(user.getString("username"));
		}
	}

	public Conversation(String conversationName) {
		this.conversationName = conversationName;
		this.date = new Date();
		this.callee = new ArrayList<String>();
	}

	public Conversation(String conversationName, String rcl) {
		this(conversationName);

		List<String> rclList = new ArrayList<String>();
		String[] splitArray = rcl.split(",");
		for (String split : splitArray) {
			rclList.add(split);
		}

		addAllCallee(rclList);
	}

	public Conversation(String conversationName, List<String> callee) {
		this(conversationName);
		addAllCallee(callee);
	}

	public void addAllCallee(List<String> callee) {
		for (String member : callee) {
			this.addCalle(member);
		}
	}

	public boolean addCalle(String member) {
		if (callee.contains(member)) {
			return false;
		}
		return callee.add(member);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getConversationName() {
		return conversationName;
	}

	public List<String> getCallee() {
		return callee;
	}

	public void setConversationName(String conversationName) {
		this.conversationName = conversationName;
	}

	public Date getDate() {
		return date;
	}

	public String getRcl() {
		return getRcl(true);
	}

	public String getRcl(boolean appendDomain) {
		StringBuilder res = new StringBuilder("");
		for (String member : callee) {
			if (!res.toString().equals("")) {
				res.append(",");
			}

			res.append(member);

			if (appendDomain) {
				res.append("@" + myIdentity.getMySIPDomain());
			}
		}
		return res.toString();
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDateToString() {
		Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return (String) format.format(date);
	}

	public void setDate(String dat) throws ParseException,
			java.text.ParseException {
		Format formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		this.date = (Date) formatter.parseObject(dat);
	}

	public ConversationThread getThread() {
		return thread;
	}

	public void setThread(ConversationThread thread) {
		this.thread = thread;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String toString() {
		return this.thread.getThreadName() + conversationName;
	}
}