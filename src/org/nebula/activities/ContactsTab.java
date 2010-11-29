/*
 * author: saad
 * rearchitecture and refactor: prajwol, saad
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nebula.R;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ContactsTab extends ExpandableListActivity implements
		OnItemSelectedListener {

	private PresenceReceiver presenceReceiver = null;

	private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	private SimpleExpandableListAdapter expListAdapter;
	private ArrayAdapter<CharSequence> adapter;
	private Spinner spinner;
	private TextView presence;
	public MyIdentity myIdentity;

	private static final int SHOW_SUB_ACTIVITY_ADDGROUP = 1;
	private static final int SHOW_SUB_ACTIVITY_ADDCONTACT = 2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tab);

		presence = (TextView) findViewById(R.id.tvPresence);
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		presence.setText(myIdentity.getMyUserName().toString());

		spinner = (Spinner) findViewById(R.id.sStatus);
		adapter = ArrayAdapter.createFromResource(this, R.array.status,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		expListAdapter = new SimpleExpandableListAdapter(this, groupData,
				R.layout.group_row, new String[] { "groupName" },
				new int[] { R.id.tvGroupName }, childData,
				R.layout.contact_row, new String[] { "userName" },
				new int[] { R.id.tvContactName });

		setListAdapter(expListAdapter);
		reloadContactList();
	}

	@Override
	protected void onResume() {
		if (presenceReceiver == null) {
			presenceReceiver = new PresenceReceiver();
			registerReceiver(presenceReceiver, new IntentFilter(
					SIPClient.NOTIFY_PRESENCE));
		}
		super.onResume();
	}

	public void reloadContactList() {
		NebulaApplication.getInstance().reloadMyGroups();
		List<Group> myGroups = NebulaApplication.getInstance().getMyIdentity()
				.getMyGroups();

		groupData.clear();
		childData.clear();

		for (Group individualGroup : myGroups) {
			Map<String, String> curGroupMap = new HashMap<String, String>();
			groupData.add(curGroupMap);
			curGroupMap.put("groupName", individualGroup.getGroupName());

			List<Map<String, String>> children = new ArrayList<Map<String, String>>();
			for (Profile individualProfile : individualGroup.getContacts()) {
				Map<String, String> curChildMap = new HashMap<String, String>();
				children.add(curChildMap);
				curChildMap.put("userName", individualProfile.getUsername());
			}
			childData.add(children);
		}
		expListAdapter.notifyDataSetChanged();
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
			// Intent intent = new Intent(ContactsTab.this, Editcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iDelete:
			// Intent intent = new Intent(ContactsTab.this, Editcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iSignout:
			finish();

			myIdentity = NebulaApplication.getInstance().getMyIdentity();
			myIdentity.setMyUserName(null);
			intent = new Intent(ContactsTab.this, Main.class);
			startActivity(intent);
			break;
		}
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
				Log.v("nebula", "contacts_tab:" + " reloading contact list");
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
			}
			break;
		case SHOW_SUB_ACTIVITY_ADDCONTACT:
			if (resultCode == AddContact.ADDCONTACT_SUCCESSFUL) {
				Log.v("nebula", "contacts_tab:" + " reloading contact list");
				reloadContactList();
			} else {
				// TODO:: recheck if this is good way
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
			// TODO:: its crazy but it works :P CHANGE IT
			String sipURI = params[1].toString().split("sip:")[1].split("@")[0];
			String status = "(" + params[2].toString() + ")";
			updateStatus(sipURI, status);

			Log.v("nebula", "main:" + "params_len=" + params.length);
			for (int i = 0; i < params.length; i++) {
				Log.v("nebula", "main:" + "param-" + params[i]);
			}
		}
	}

	public void alert(String message) {
		new AlertDialog.Builder(this).setMessage(message).show();
	}

	public void updateStatus(String username, String status) {
		for (List<Map<String, String>> childList : childData) {
			for (Map<String, String> map : childList) {
				// TODO:: its crazy but it works :P CHANGE IT
				if (map.get("userName").split("\\(")[0].equals(username)) {
					// TODO:: check if put updates or not
					map.put("userName", username + status);
				}
			}
		}
		expListAdapter.notifyDataSetChanged();
	}
}
