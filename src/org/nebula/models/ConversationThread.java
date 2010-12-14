/*
 * author nina mulkijanyan
 */

package org.nebula.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.nebula.main.NebulaApplication;

public class ConversationThread {
	private String id;
	private int seqNo = 0;
	private List<Conversation> myConversations = new ArrayList<Conversation>();

	private Calendar calendar = Calendar.getInstance();
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyyMMddhhmmss");

	public ConversationThread() {
		this.id = createNewThreadId();
	}

	public ConversationThread(String id) {
		this.id = id;
	}

	public Conversation addConversation(String newConversationId, String rcl) {
		Conversation newConversation = new Conversation(newConversationId, rcl);
		myConversations.add(newConversation);
		return newConversation;
	}

	public Conversation addConversation(String rcl) {
		Conversation newConversation = new Conversation(
				createNewConversationId(), rcl);
		myConversations.add(newConversation);
		return newConversation;
	}

	private String createNewConversationId() {
		return NebulaApplication.getInstance().getMyIdentity().getMyUserName()
				+ seqNo++;
	}

	public String createNewThreadId() {
		return NebulaApplication.getInstance().getMyIdentity().getMyUserName()
				+ simpleDateFormat.format(calendar.getTime());
	}

	public Conversation getConversation(String id) throws Exception {
		for (Conversation curConversation : myConversations) {
			if (curConversation.getId().equals(id)) {
				return curConversation;
			}
		}
		return null;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public String getId() {
		return id;
	}

	public List<Conversation> getMyConversations() {
		return myConversations;
	}
}
