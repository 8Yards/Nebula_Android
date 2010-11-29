/*
 * author: saad,sharique
 * rearchitecture and refactor: prajwol, saad
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nebula.R;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.Profile;

import android.app.ExpandableListActivity;
import android.content.Intent;
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

public class ContactsTab extends ExpandableListActivity implements
		OnItemSelectedListener {

	private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	private SimpleExpandableListAdapter expListAdapter;
	private ArrayAdapter<CharSequence> adapter;
	private Spinner spinner;

	private static final int SHOW_SUB_ACTIVITY_GOTOGROUP = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tab);

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

	public void reloadContactList() {
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
		switch (item.getItemId()) {
		case R.id.iInstantTalk:
			break;
		case R.id.iAddContact:
			// Intent intent = new Intent(ContactsTab.this,
			// Addcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iAddGroup:
			Intent intent = new Intent(ContactsTab.this, AddGroup.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_GOTOGROUP);
			break;
		case R.id.iEdit:
			// Intent intent = new Intent(ContactsTab.this,
			// Editcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iDelete:
			// Intent intent = new Intent(ContactsTab.this,
			// Editcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iSignout:
			// this.finish();
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
}
