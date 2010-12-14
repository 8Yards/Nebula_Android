/*
 * author - prajwol, sujan
 */

package org.nebula.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nebula.R;
import org.nebula.client.sip.SIPClient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class ContactExpandableListAdapter extends SimpleExpandableListAdapter {
	private Map<String, Integer> iconHolder;

	public ContactExpandableListAdapter(Context context,
			List<Map<String, String>> groupData, int groupLayout,
			String[] groupFrom, int[] groupTo,
			List<List<Map<String, ContactRow>>> childData, int childLayout,
			String[] childFrom, int[] childTo) {
		super(context, groupData, groupLayout, groupFrom, groupTo, childData,
				childLayout, childFrom, childTo);

		// TODO:: check how to directly use the resources
		iconHolder = new HashMap<String, Integer>();
		iconHolder.put(SIPClient.PRESENCE_ONLINE, R.drawable.green);
		iconHolder.put(SIPClient.PRESENCE_BUSY, R.drawable.red);
		iconHolder.put(SIPClient.PRESENCE_OFFLINE, R.drawable.white);
		iconHolder.put(SIPClient.PRESENCE_AWAY, R.drawable.orange);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = newChildView(isLastChild, parent);
		}

		bindView(
				v,
				(Map<String, ContactRow>) getChild(groupPosition, childPosition));

		return v;
	}

	private void bindView(View view, Map<String, ContactRow> data) {
		ContactRow currentRow = data.get("userName");

		ImageView presence = (ImageView) view.findViewById(R.id.ivPresence);
		presence.setImageResource(iconHolder.get(currentRow.getStatus()));

		TextView userName = (TextView) view.findViewById(R.id.tvContactName);
		userName.setText(currentRow.getUserName());
	}
}
