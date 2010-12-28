/*
 * author - michel, saad
 */
package org.nebula.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nebula.R;
import org.nebula.client.sip.SIPClient;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationTabExpandableListAdapter extends
		BaseExpandableListAdapter {
	private Map<String, Integer> iconHolder;
	private List<ConversationRow> conversations;
	private List<List<ContactRow>> members;

	private LayoutInflater inflater;

	public ConversationTabExpandableListAdapter(Context context,
			List<ConversationRow> conversations) {
		this.conversations = conversations;
		this.inflater = LayoutInflater.from(context);

		this.iconHolder = new HashMap<String, Integer>();
		this.iconHolder.put(SIPClient.PRESENCE_ONLINE, R.drawable.green);
		this.iconHolder.put(SIPClient.PRESENCE_BUSY, R.drawable.red);
		this.iconHolder.put(SIPClient.PRESENCE_OFFLINE, R.drawable.white);
		this.iconHolder.put(SIPClient.PRESENCE_AWAY, R.drawable.orange);
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.conversation_row, parent, false);
		}

		ConversationRow currentConversation = (ConversationRow) conversations
				.get(groupPosition);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
		String conversationName = dateFormat.format(currentConversation
				.getConversation().getDate());

		Conversation conversation = currentConversation.getConversation();
		// v.setTag(conversation);

		TextView threadName = (TextView) v
				.findViewById(R.id.tvConversationName);
		threadName.setText(conversationName);
		// threadName.setTag(conversation);
		// threadName.setOnLongClickListener(ConversationTab.getInstance());

		if (conversation.equals(NebulaApplication.getInstance().getMyIdentity()
				.getCurrentConversation())) {
			threadName.setBackgroundColor(Color.rgb(0x35, 0x6A, 0xA0));
		} else {
			threadName.setBackgroundColor(Color.TRANSPARENT);
		}

		Button b = (Button) v.findViewById(R.id.bInviteMembers);
		b.setTag(currentConversation.getConversation());
		return v;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.member_row, parent, false);
		}

		ContactRow currentRow = conversations.get(groupPosition).getMembers()
				.get(childPosition);

		ImageView presence = (ImageView) v.findViewById(R.id.ivPresence);
		//presence.setImageResource(iconHolder.get(currentRow.getStatus()));
		try {
			presence.setImageResource(iconHolder.get(NebulaApplication
					.getInstance().getMyIdentity().getContactByName(
							currentRow.getUserName()).getStatus()));
		} catch (Exception e) {
			presence.setImageResource(iconHolder.get(SIPClient.PRESENCE_OFFLINE));
			//Log.v("nebula", e.getMessage());
		}

		TextView userName = (TextView) v.findViewById(R.id.tvMemberName);
		userName.setText(currentRow.getUserName());

		return v;
	}

	public int getChildrenCount(int groupPosition) {
		return conversations.get(groupPosition).getMembers().size();
	}

	public Object getGroup(int groupPosition) {
		return conversations.get(groupPosition);
	}

	public int getGroupCount() {
		return conversations.size();
	}

	public long getGroupId(int groupPosition) {
		return (long) (groupPosition * 1024); // To be consistent with
		// getChildId
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void onGroupCollapsed(int groupPosition) {
	}

	public void onGroupExpanded(int groupPosition) {
	}

	public void setConversations(List<ConversationRow> conversations) {
		this.conversations = conversations;
	}

	public void setMembers(List<List<ContactRow>> members) {
		this.members = members;
	}

	public List<ConversationRow> getConversations() {
		return conversations;
	}

	public List<List<ContactRow>> getMembers() {
		return members;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return members.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return (long) (groupPosition * 1024 + childPosition);
	}
}
