/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.main.NebulaApplication;

public class ConversationThread {
	private String threadName;	
	private List<Conversation> myConversations = new ArrayList<Conversation>();

	

	public ConversationThread() {
		this.threadName = NebulaApplication.getInstance().getMyIdentity().createNewThreadId();
	}

	public ConversationThread(String threadName) {
		this.threadName = threadName;
	}

	public ConversationThread(JSONObject keyObj, String key) throws JSONException, ParseException {
		this.threadName = key;
		this.myConversations = new ArrayList<Conversation>();

		for (Iterator iterator = keyObj.keys(); iterator.hasNext();) {
			String actualConv = "" + iterator.next();
			JSONObject conversationObj = keyObj.getJSONObject(actualConv);			
			addConversation(new Conversation(conversationObj, actualConv));
		}
	}

	public Conversation addConversation(String newConversationId, String rcl) {
		Conversation newConversation = new Conversation(newConversationId, rcl);
		newConversation.setThread(this);
		myConversations.add(newConversation);
		return newConversation;
	}
	
	public Conversation addConversation(List<String> members) {
		return addConversation(new Conversation(NebulaApplication.getInstance().getMyIdentity().createNewConversationId(),
				members));
	}

	public Conversation addConversation(Conversation conv) {
		conv.setThread(this);
		myConversations.add(conv);
		return conv;
	}

	public Conversation getConversationByName(String name) throws Exception {
		for (Conversation curConversation : myConversations) {
			if (curConversation.getConversationName().equals(name)) {
				return curConversation;
			}
		}
		return null;
	}

	public boolean existsConversation(String conversationName) {
		for (Conversation conv : myConversations) {
			if (conv.getConversationName().equals(conversationName)) {
				return true;
			}
		}
		return false;
	}

	public String getThreadName() {
		return threadName;
	}

	public List<Conversation> getMyConversations() {
		return myConversations;
	}
}