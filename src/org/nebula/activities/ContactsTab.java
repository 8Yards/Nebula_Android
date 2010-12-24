/*
 * author: saad, michel, sujan, sharique
 * rearchitecture and refactor: prajwol, saad, michel, sujan
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.List;

import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.client.rest.Status;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;
import org.nebula.ui.ContactRow;
import org.nebula.ui.ContactsTabExpandableListAdapter;
import org.nebula.ui.GroupRow;

import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ContactsTab extends ExpandableListActivity implements
		OnItemSelectedListener {
	private static final int SHOW_SUB_ACTIVITY_ADDGROUP = 1;
	private static final int SHOW_SUB_ACTIVITY_ADDCONTACT = 2;
	private static final int SHOW_SUB_ACTIVITY_DELETE = 3;
	private static final int SHOW_SUB_ACTIVITY_EDIT = 4;

	private List<GroupRow> groups = new ArrayList<GroupRow>();
	private List<List<ContactRow>> contacts = new ArrayList<List<ContactRow>>();
	private ContactsTabExpandableListAdapter expListAdapter = null;

	private ArrayAdapter<CharSequence> adapter;
	private Spinner spinner;
	private TextView presence;

	private PresenceReceiver presenceReceiver = null;
	private InviteReceiver refreshReceiver = null;

	public static List<String> callee;

	public MyIdentity myIdentity;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tab);

		presence = (TextView) findViewById(R.id.tvPresence);
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		presence.setText(myIdentity.getMyUserName().toString());

		spinner = (Spinner) findViewById(R.id.sStatus);
		adapter = ArrayAdapter.createFromResource(this, R.array.status,
				android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		expListAdapter = new ContactsTabExpandableListAdapter(this, groups,
				contacts);
		setListAdapter(expListAdapter);

		NebulaApplication.getInstance().reloadMyGroups();
		reloadContactList();
	}

	@Override
	protected void onResume() {
		if (presenceReceiver == null) {
			presenceReceiver = new PresenceReceiver();
			registerReceiver(presenceReceiver, new IntentFilter(
					SIPClient.NOTIFY_PRESENCE));
		}

		if (refreshReceiver == null) {
			refreshReceiver = new InviteReceiver();
			registerReceiver(refreshReceiver, new IntentFilter(
					SIPClient.NOTIFY_INVITE));
		}
		super.onResume();
	}

	private void reloadContactList() {
		Log.v("nebula", "contacts_tab:" + " reloading contact list");

		List<Group> myGroups = myIdentity.getMyGroups();
		groups.clear();
		contacts.clear();

		for (Group individualGroup : myGroups) {
			groups.add(new GroupRow(individualGroup.getGroupName()));

			List<ContactRow> children = new ArrayList<ContactRow>();
			for (Profile individualProfile : individualGroup.getContacts()) {
				if (!individualProfile.getUsername().equals("null")) {
					children
							.add(new ContactRow(individualProfile.getUsername()));
				}
			}
			contacts.add(children);
		}
		expListAdapter.notifyDataSetChanged();
	}

	public void onGroupClick(View v) {
		CheckBox cb = (CheckBox) v;
		LinearLayout groupRow = (LinearLayout) v.getParent();
		String groupName = (String) ((TextView) groupRow
				.findViewById(R.id.tvGroupName)).getText();

		for (int i = 0; i < groups.size(); i++) {
			GroupRow g = groups.get(i);
			if (g.getName().equals(groupName)) {
				g.setChecked(cb.isChecked());
				for (ContactRow individualContact : contacts.get(i)) {
					individualContact.setChecked(cb.isChecked());
				}
				expListAdapter.notifyDataSetChanged();

				return;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_contacts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.iInstantTalk:
			List<String> members = retrieveCheckedContacts();
			if (members.size() == 0) {
				Toast.makeText(this.getApplicationContext(),
						"No contact was selected", Toast.LENGTH_LONG).show();
				break;
			}
			members.add(myIdentity.getMyUserName());

			ConversationThread thread = myIdentity.createThread();
			Conversation conversation = thread.addConversation(members);

			RESTConversationManager conversationManager = new RESTConversationManager();
			try {
				Status status = conversationManager
						.addNewConversation(conversation);
				if (!status.isSuccess()) {
					throw new Exception(status.getMessage());
				} else {
					SIPManager.doCall(thread, conversation);
					((Main) getParent()).setTabByTag(Main.CONVERSATION_TAB);
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.iAddContact:
			intent = new Intent(ContactsTab.this, AddContact.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_ADDCONTACT);
			break;
		case R.id.iAddGroup:
			intent = new Intent(ContactsTab.this, AddGroup.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_ADDGROUP);
			break;
		case R.id.iEdit:
			intent = new Intent(ContactsTab.this, Edit.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_EDIT);
			break;
		case R.id.iDelete:
			intent = new Intent(ContactsTab.this, Delete.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_DELETE);
			break;
		case R.id.iSignout:
			SIPManager.doLogout();
			myIdentity.setMyUserName(null);
			finish();
			System.exit(-1);
			break;
		}
		return true;
	}

	public List<String> retrieveCheckedContacts() {
		List<String> results = new ArrayList<String>();
		for (List<ContactRow> list : expListAdapter.getContacts()) {
			for (ContactRow lc : list) {
				if (lc.isChecked()) {
					results.add(lc.getUserName());
				}
			}
		}
		return results;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		super.onChildClick(parent, v, groupPosition, childPosition, id);

		CheckedTextView userName = (CheckedTextView) v
				.findViewById(R.id.ctvContactName);
		userName.toggle();
		userName.setBackgroundColor(userName.isChecked() ? Color.DKGRAY
				: Color.TRANSPARENT);

		contacts.get(groupPosition).get(childPosition).setChecked(
				userName.isChecked());

		return true;
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		int status = SIPManager.doPublish(parent.getItemAtPosition(pos)
				.toString());
		Log.v("nebula", "contacts_tab: "
				+ (status == SIPManager.PUBLISH_SUCCESSFUL ? "publish success"
						: "publish failure"));
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing here :P
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SHOW_SUB_ACTIVITY_ADDGROUP:
			if (resultCode == AddGroup.ADDGROUP_SUCCESSFULL) {
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
			}
			break;
		case SHOW_SUB_ACTIVITY_ADDCONTACT:
			if (resultCode == AddContact.ADDCONTACT_SUCCESS) {
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
			}
			break;
		case SHOW_SUB_ACTIVITY_DELETE:
			if (resultCode == Delete.DELETEGROUP_SUCCESSFUL
					|| resultCode == Delete.DELETECONTACT_SUCCESSFUL) {
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
			}
			break;
		case SHOW_SUB_ACTIVITY_EDIT:
			if (resultCode == Edit.EDITGROUP_SUCCESSFUL
					|| resultCode == Edit.EDITCONTACT_SUCCESSFUL) {
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
			}
			break;
		default:
			break;
		}
	}

	public class PresenceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] params = (Object[]) intent.getExtras().get("params");
			String sipURI = params[1].toString().split("sip:")[1].split("@")[0];
			String status = params[2].toString();
			updateStatus(sipURI, status);
		}
	}

	public class InviteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			((Main) getParent()).setTabByTag(Main.CONVERSATION_TAB);
		}
	}

	public void updateStatus(String username, String status) {
		for (List<ContactRow> contactList : contacts) {
			for (ContactRow contact : contactList) {
				if (contact.getUserName().equals(username)) {
					contact.setStatus(status);
				}
			}
		}
		// Log.v("presence-update", username + "-" + status);
		expListAdapter.notifyDataSetChanged();
	}
}
