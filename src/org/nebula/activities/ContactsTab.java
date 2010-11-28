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
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.Profile;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;

public class ContactsTab extends ExpandableListActivity {

	private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	private SimpleExpandableListAdapter expListAdapter;
	private static final int SHOW_SUB_ACTIVITY_GoToGROUP = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tab);

		expListAdapter = new SimpleExpandableListAdapter(this, groupData,
				R.layout.group_row, new String[] { "groupName" },
				new int[] { R.id.tvGroupName }, childData,
				R.layout.contact_row, new String[] { "userName" },
				new int[] { R.id.tvContactName });
		
		setListAdapter(expListAdapter);
		reloadContactList();
	}

	/*private void reloadContactList() {
		// TextView presence = (TextView) findViewById(R.id.presence);
		Spinner spinner = (Spinner) findViewById(R.id.sStatus);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.status_prompt,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		List<Group> myGroups = NebulaApplication.getInstance().getMyIdentity()
				.getMyGroups();

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

		SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
				this, groupData, R.layout.group_row,
				new String[] { "groupName" }, new int[] { R.id.tvGroupName },
				childData, R.layout.contact_row, new String[] { "userName" },
				new int[] { R.id.tvContactName });

		/*
		 * SimpleExpandableListAdapter expListAdapter = new
		 * SimpleExpandableListAdapter( this, groupData, R.layout.mighty_row,
		 * new String[] { "groupName" }, new int[] { R.id.tvdisplayText },
		 * childData, R.layout.mighty_row, new String[] { "userName" }, new
		 * int[] { R.id.tvdisplayText });
		 */

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
			// Intent intent = new Intent(ContactsTab.this, Addcontacts.class);
			// startActivity(intent);
			break;
		case R.id.iAddGroup:
			Intent intent = new Intent(ContactsTab.this, AddGroup.class);
			startActivityForResult(intent, SHOW_SUB_ACTIVITY_GoToGROUP);
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
			// this.finish();
			break;
		}
		return true;
	}

	/*
	 * public void doToggleCheckBox(View v) { new
	 * AlertDialog.Builder(this).setMessage(
	 * v.getParent()..getClass().getName()).show(); }
	 * 
	 * @Override public boolean onChildClick(ExpandableListView parent, View v,
	 * int groupPosition, int childPosition, long id) {
	 * super.onChildClick(parent, v, groupPosition, childPosition, id);
	 * 
	 * v.setSelected(selected)
	 * 
	 * new AlertDialog.Builder(this).setMessage(v.getClass().getName()).show();
	 * 
	 * return true; }
	 */

}
