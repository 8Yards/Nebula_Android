/*
 * author - michel, sujan
 * refactor - michel, prajwol, sujan
 */
package org.nebula.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nebula.R;
import org.nebula.client.sip.NebulaSIPConstants;
import org.nebula.client.sip.SIPHandler;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsTabExpandableListAdapter extends BaseExpandableListAdapter {
	private Map<String, Integer> iconHolder;
	private List<GroupRow> groups;
	private List<List<ContactRow>> contacts;

	private LayoutInflater inflater;

	public ContactsTabExpandableListAdapter(Context context,
			List<GroupRow> groups, List<List<ContactRow>> contacts) {
		this.groups = groups;
		this.contacts = contacts;
		this.inflater = LayoutInflater.from(context);

		this.iconHolder = new HashMap<String, Integer>();
		this.iconHolder.put(NebulaSIPConstants.PRESENCE_ONLINE, R.drawable.green);
		this.iconHolder.put(NebulaSIPConstants.PRESENCE_BUSY, R.drawable.red);
		this.iconHolder.put(NebulaSIPConstants.PRESENCE_OFFLINE, R.drawable.white);
		this.iconHolder.put(NebulaSIPConstants.PRESENCE_AWAY, R.drawable.orange);
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.group_row, parent, false);
		}

		if (groupPosition >= contacts.size()) {
			return v;
		}

		GroupRow currentGroup = (GroupRow) groups.get(groupPosition);

		TextView groupName = (TextView) v.findViewById(R.id.tvGroupName);
		groupName.setText(currentGroup.getName());

		CheckBox cb = (CheckBox) v.findViewById(R.id.cbCheckGroup);
		cb.setChecked(currentGroup.isChecked());

		return v;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = inflater.inflate(R.layout.contact_row, parent, false);
		}

		if (groupPosition >= contacts.size()
				|| childPosition >= contacts.get(groupPosition).size()) {
			return v;
		}

		ContactRow currentRow = contacts.get(groupPosition).get(childPosition);

		ImageView presence = (ImageView) v.findViewById(R.id.ivPresence);
		presence.setImageResource(iconHolder.get(currentRow.getStatus()));

		CheckedTextView userName = (CheckedTextView) v
				.findViewById(R.id.ctvContactName);
		userName.setText(currentRow.getUserName());
		userName.setChecked(currentRow.isChecked());
		userName.setBackgroundColor(currentRow.isChecked() ? Color.DKGRAY
				: Color.TRANSPARENT);

		return v;
	}

	public int getChildrenCount(int groupPosition) {
		return contacts.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	public int getGroupCount() {
		return groups.size();
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

	public void setGroups(List<GroupRow> groups) {
		this.groups = groups;
	}

	public void setContacts(List<List<ContactRow>> contacts) {
		this.contacts = contacts;
	}

	public List<GroupRow> getGroups() {
		return groups;
	}

	public List<List<ContactRow>> getContacts() {
		return contacts;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return contacts.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return (long) (groupPosition * 1024 + childPosition);
	}
}
