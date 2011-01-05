/*
 * author - michel, saad
 */

package org.nebula.ui;

import java.util.ArrayList;
import java.util.List;

import org.nebula.models.Conversation;

public class ConversationRow implements Comparable<ConversationRow> {
	private boolean isChecked;
	private List<ContactRow> members;
	private Conversation conversation;

	public ConversationRow(Conversation conv) {
		this.conversation = conv;
		
		members=new ArrayList<ContactRow>();
		for (String callee : conv.getCallee()) {
			members.add(new ContactRow(callee));
		}
	}

	public int compareTo(ConversationRow other) {
		if(other==null)
			return 1;
		return -conversation.getDate().compareTo(other.getConversation().getDate());
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public List<ContactRow> getMembers() {
		return members;
	}

	public void setMembers(List<ContactRow> members) {
		this.members = members;
	}
}
