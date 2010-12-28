/*
 * author: saad, michel, sujan, sharique
 * rearchitecture and refactor: prajwol, saad, michel, sujan
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
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
import org.nebula.utils.NebulaTask;
import org.nebula.utils.Utils;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
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

		ProgressDialog pd = ProgressDialog.show(this, "",
				"Loading. Please wait...", true);

		NebulaApplication.getInstance().reloadMyGroups();
		reloadContactList();

		pd.cancel();
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
		Collections.sort(groups);
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

			final ConversationThread thread = myIdentity.createThread();
			final Conversation conversation = thread.addConversation(members);
			try {
				new NebulaTask() {
					protected Long doInBackground(Object... params) {
						Conversation c = (Conversation) params[0];

						RESTConversationManager conversationManager = new RESTConversationManager();

						org.nebula.client.rest.Status status;
						try {
							status = conversationManager.addNewConversation(c);

							if (!status.isSuccess()) {
								runOnUiThread(new Runnable() {
									public void run() {
										notifyUser("The conversation could not be saved on remote");
									}
								});
							}
						} catch (Exception e) {
							runOnUiThread(new Runnable() {
								public void run() {
									notifyUser("The conversation could not be saved on remote");
								}
							});
						}

						return null;
					}
				}.execute(conversation);

				new NebulaTask() {
					protected Long doInBackground(Object... params) {
						ConversationThread thread = (ConversationThread) params[0];
						Conversation conversation = (Conversation) params[1];
						SIPManager.doCall(thread, conversation);

						return null;
					}
				}.execute(thread, conversation);

				((Main) getParent()).setTabByTag(Main.CONVERSATION_TAB);
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

	private void notifyUser(String msg) {
		Utils.notifyUser(this, msg);
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
		new NebulaTask() {
			protected Long doInBackground(Object... params) {
				AdapterView<?> parent = (AdapterView<?>) params[0];
				int pos = (Integer) params[1];
				String myStatus = parent.getItemAtPosition(pos).toString();
				myIdentity
						.setMyStatus(myStatus);

				runOnUiThread(new Runnable() {
					public void run() {
						expListAdapter.notifyDataSetChanged();
					}
				});
				
				int status = SIPManager.doPublish(myStatus);
				Log
				.v(
						"nebula",
						"contacts_tab: "
								+ (status == SIPManager.PUBLISH_SUCCESSFUL ? "publish success"
										: "publish failure"));
				
				return null;
			}
		}.execute(parent, pos);
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
				//TODO should not REST reload
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
			}
			break;
		case SHOW_SUB_ACTIVITY_ADDCONTACT:
			if (resultCode == AddContact.ADDCONTACT_SUCCESS) {
				//TODO should not REST reload
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
			}
			break;
		case SHOW_SUB_ACTIVITY_DELETE:
			if (resultCode == Delete.DELETEGROUP_SUCCESSFUL
					|| resultCode == Delete.DELETECONTACT_SUCCESSFUL) {
				//TODO should not REST reload
				NebulaApplication.getInstance().reloadMyGroups();
				reloadContactList();
			} else {
			}
			break;
		case SHOW_SUB_ACTIVITY_EDIT:
			if (resultCode == Edit.EDITGROUP_SUCCESSFUL
					|| resultCode == Edit.EDITCONTACT_SUCCESSFUL) {
				//TODO should not REST reload
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
			new NebulaTask() {
				protected Long doInBackground(Object... intents) {
					Intent intent = (Intent) intents[0];
					Object[] params = (Object[]) intent.getExtras().get(
							"params");
					String sipURI = params[1].toString().split("sip:")[1]
							.split("@")[0];
					String status = params[2].toString();
					updateStatus(sipURI, status);

					return null;
				}
			}.execute(intent);
		}
	}

	public class InviteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			((Main) getParent()).setTabByTag(Main.CONVERSATION_TAB);
		}
	}

	public void updateStatus(String username, String status) {
		/*
		 * for (List<ContactRow> contactList : contacts) { for (ContactRow
		 * contact : contactList) { if (contact.getUserName().equals(username))
		 * { contact.setStatus(status); } } }
		 */

		try {
			NebulaApplication.getInstance().getMyIdentity().getContactByName(
					username).setStatus(status);
		} catch (Exception e) {
			Log.w("nebula", "ContactsTab: contact not found");
		}
		// Log.v("presence-update", username + "-" + status);

		runOnUiThread(new Runnable() {
			public void run() {
				expListAdapter.notifyDataSetChanged();
			}
		});
	}
}
