/*
 * author: saad
 * rearchitecture and refactor: prajwol, saad
 */

package org.nebula.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nebula.R;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.Profile;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class ContactsTab extends ExpandableListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tab);

		reloadContactList();
	}

	private void reloadContactList() {
		List<Group> myGroups = NebulaApplication.getInstance().getMyIdentity().getMyGroups();

		List<HashMap<String, String>> groups = new ArrayList<HashMap<String, String>>();
		HashMap<String, ArrayList<HashMap<String, String>>> groupMembers = new HashMap<String, ArrayList<HashMap<String, String>>>();
		List<ArrayList<HashMap<String, String>>> profiles = new ArrayList<ArrayList<HashMap<String, String>>>();

		for (Group individualGroup : myGroups) {
			HashMap<String, String> t = new HashMap<String, String>();
			t.put("groupName", individualGroup.getGroupName());
			groups.add(t);

			ArrayList<HashMap<String, String>> tt = new ArrayList<HashMap<String, String>>();

			for (Profile profile_ : individualGroup.getContacts()) {
				HashMap<String, String> t1 = new HashMap<String, String>();
				t1.put("username", profile_.getUsername());
				tt.add(t1);
			}
			groupMembers.put(individualGroup.getGroupName(), tt);
			profiles.add(groupMembers.get(individualGroup.getGroupName()));

		}

		SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
				this, groups, R.layout.group_row, new String[] { "groupName" },
				new int[] { R.id.tvGroupName }, profiles,
				R.layout.contact_row, new String[] { "username" },
				new int[] { R.id.tvContactName });
		
		setListAdapter(expListAdapter);
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
			//Intent intent = new Intent(ContactsTab.this, Addcontacts.class);
			 //startActivity(intent);
			break;
		case R.id.iAddGroup:
			// Intent intent = new Intent(ContactsTab.this, Addgroup.class);
			// startActivity(intent);
			break;
		case R.id.iEdit:
			// Intent intent = new Intent(ContactsTab.this, Editcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iSignout:
			// this.finish();
			break;
		}
		return true;
	}

	public void onContentChanged() {
		super.onContentChanged();
		// TODO: what here?
	}

	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		CheckBox cb = (CheckBox) v.findViewById(R.id.cbCheckContact);
		if (cb != null) {
			cb.toggle();
		}
		// TODO: whats the purpose?
		return false;
	}

}
