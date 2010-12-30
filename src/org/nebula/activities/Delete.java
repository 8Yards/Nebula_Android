/*
 * author: saad ali
 * refactored by: saad, prajwol
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.List;

import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.Profile;
import org.nebula.models.Status;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Delete extends Activity implements OnItemSelectedListener,
		DialogInterface.OnClickListener,
		DialogInterface.OnMultiChoiceClickListener {

	public static final int DELETEGROUP_SUCCESSFUL = 0;
	public static final int DELETEGROUP_FAILURE = 1;

	public static final int DELETECONTACT_SUCCESSFUL = 2;
	public static final int DELETECONTACT_FAILURE = 3;

	private RESTGroupManager groupManager;
	private List<Group> myGroups;

	private String[] itemNames;
	private Integer[] itemIds;
	private boolean[] selectedItems;

	private EditText removeGroup;
	private EditText removeContact;

	private AbsoluteLayout group;
	private AbsoluteLayout contact;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete);

		removeGroup = (EditText) findViewById(R.id.etSelectGroups);
		removeContact = (EditText) findViewById(R.id.etSelectContact);
		group = (AbsoluteLayout) findViewById(R.id.abGroup);
		contact = (AbsoluteLayout) findViewById(R.id.abContact);

		myGroups = NebulaApplication.getInstance().getMyIdentity()
				.getMyGroups();
		groupManager = new RESTGroupManager();
	}

	public void doShowGroup(View v) {
		contact.setVisibility(View.INVISIBLE);
		group.setVisibility(View.VISIBLE);
	}

	public void doShowContact(View v) {
		contact.setVisibility(View.VISIBLE);
		group.setVisibility(View.INVISIBLE);
	}

	public void doSelectGroups(View v) {
		if (myGroups.isEmpty()) {
			Toast.makeText(this.getApplicationContext(), "No group found",
					Toast.LENGTH_LONG).show();
			return;
		}

		// to remove the "ungrouped" from showing up
		List<String> groupNamesList = new ArrayList<String>();
		List<Integer> groupIdsList = new ArrayList<Integer>();
		for (int i = 0; i < myGroups.size(); i++) {
			if (myGroups.get(i).getGroupName().equals("ungrouped") == false) {
				groupNamesList.add(myGroups.get(i).getGroupName());
				groupIdsList.add(myGroups.get(i).getId());
			}
		}

		itemNames = new String[groupNamesList.size()];
		itemIds = new Integer[groupIdsList.size()];
		selectedItems = new boolean[groupNamesList.size()];
		groupNamesList.toArray(itemNames);
		groupIdsList.toArray(itemIds);

		new AlertDialog.Builder(this).setTitle("Select Groups")
				.setMultiChoiceItems(itemNames, selectedItems, this)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						EditText text = (EditText) findViewById(R.id.etSelectGroups);
						String selectedNames = "";
						for (int i = 0; i < itemNames.length; i++) {
							if (selectedItems[i] == true) {
								selectedNames = selectedNames + " ["
										+ itemNames[i] + "] ";
							}
						}
						text.setText(selectedNames);
					}
				}).show();
	}

	public void doDeleteGroup(View v) {
		if (removeGroup.length() == 0) {
			Toast.makeText(getApplication(), "Please select Group Name",
					Toast.LENGTH_LONG).show();
			return;
		}

		Status status = new Status(false, "Uninitialized status");
		if (selectedItems != null) {
			for (int i = 0; i < selectedItems.length; i++) {
				try {
					if (selectedItems[i]) {
						status = groupManager
								.deleteGroup(itemIds[i].intValue());

						if (!status.isSuccess()) {
							throw new Exception(status.getMessage());
						}
					}
				} catch (Exception e) {
					Toast.makeText(this.getApplicationContext(),
							e.getMessage(), Toast.LENGTH_LONG).show();
					setResult(DELETEGROUP_FAILURE);
					return;
				}
			}
			Toast.makeText(this.getApplicationContext(), "Group Deleted",
					Toast.LENGTH_LONG).show();
			setResult(DELETEGROUP_SUCCESSFUL);
			finish();
		}
	}

	public void doSelectContacts(View v) {
		if (myGroups.isEmpty()) {
			Toast.makeText(this.getApplicationContext(), "No contact found",
					Toast.LENGTH_LONG).show();
			return;
		}
		List<String> addedProfiles = new ArrayList<String>();
		List<String> contactNamesList = new ArrayList<String>();
		List<Integer> contactIdsList = new ArrayList<Integer>();
		for (Group group : myGroups) {
			for (Profile profile : group.getContacts()) {
				if (!profile.getUsername().equals("null")&& !addedProfiles.contains(profile.getUsername())) {
					contactNamesList.add(profile.getUsername());
					contactIdsList.add(profile.getId());
					addedProfiles.add(profile.getUsername());
				}
			}
		}

		itemNames = new String[contactNamesList.size()];
		itemIds = new Integer[contactIdsList.size()];
		selectedItems = new boolean[contactNamesList.size()];
		contactNamesList.toArray(itemNames);
		contactIdsList.toArray(itemIds);

		new AlertDialog.Builder(this).setTitle("Select Contacts")
				.setMultiChoiceItems(itemNames, selectedItems, this)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						EditText text = (EditText) findViewById(R.id.etSelectContact);
						String selectedNames = "";
						for (int i = 0; i < itemNames.length; i++) {
							if (selectedItems[i] == true) {
								selectedNames = selectedNames + " ["
										+ itemNames[i] + "] ";
							}
						}
						text.setText(selectedNames);
					}
				}).show();
	}

	public void doDeleteContact(View v) {
		if (removeContact.length() == 0) {
			Toast.makeText(getApplication(), "Please select Contact Name",
					Toast.LENGTH_LONG).show();
			return;
		}

		Status status = new Status(false, "Uninitialized status");
		if (selectedItems != null) {
			for (int i = 0; i < selectedItems.length; i++) {
				try {
					if (selectedItems[i]) {
						status = groupManager.deleteContact(itemIds[i]
								.intValue());

						if (!status.isSuccess()) {
							throw new Exception(status.getMessage());
						}
					}
				} catch (Exception e) {
					Toast.makeText(this.getApplicationContext(),
							e.getMessage(), Toast.LENGTH_LONG).show();
					setResult(DELETECONTACT_FAILURE);
					return;
				}
			}
			Toast.makeText(this.getApplicationContext(), "Contact Deleted",
					Toast.LENGTH_LONG).show();
			setResult(DELETECONTACT_SUCCESSFUL);
			finish();
		}
	}

	public void doBackGroups(View v) {
		setResult(DELETEGROUP_FAILURE);
		finish();
	}

	public void doBackContact(View v) {
		setResult(DELETECONTACT_FAILURE);
		finish();
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
	}
}