/*
 * author: saad ali, prajwol , sharique
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.List;

import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rest.Status;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import org.nebula.models.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class Edit extends Activity implements OnItemSelectedListener,
		DialogInterface.OnClickListener,
		DialogInterface.OnMultiChoiceClickListener {

	public static final int EDITGROUP_SUCCESSFUL = 0;
	public static final int EDITGROUP_FAILURE = 1;

	public static final int EDITCONTACT_SUCCESSFUL = 2;
	public static final int EDITCONTACT_FAILURE = 3;

	private static final int EDIT_CANCELLED = 4;

	private RESTGroupManager groupManager;
	private List<Group> myGroups;

	private ArrayAdapter<Object> adapter;

	private AbsoluteLayout group;
	private RadioButton radioGroup;
	private Spinner spinnerGroup;
	private EditText renameGroup;
	private EditText selectContact;

	private AbsoluteLayout contact;
	private RadioButton radioContact;
	private Spinner spinnerContact;
	private EditText renameContact;

	private int selectedItemId = -1; // this group is under change
	private boolean isLinkedItemsChanged = false;
	private String[] linkedItemNames = null;
	private Integer[] linkedItemIds = null; // group'll have these contact ids
	private boolean[] selectedLinkedItems = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);

		radioGroup = (RadioButton) findViewById(R.id.rbEditGroup);
		radioContact = (RadioButton) findViewById(R.id.rbEditContact);
		selectContact = (EditText) findViewById(R.id.etSelectContact);
		renameGroup = (EditText) findViewById(R.id.etRenameGroup);
		renameContact = (EditText) findViewById(R.id.etRenameContact);
		group = (AbsoluteLayout) findViewById(R.id.abGroup);
		contact = (AbsoluteLayout) findViewById(R.id.abContact);

		adapter = new ArrayAdapter<Object>(this,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerGroup = (Spinner) findViewById(R.id.sSelectGroup);
		spinnerContact = (Spinner) findViewById(R.id.sSelectContact);

		spinnerGroup.setOnItemSelectedListener(this);
		spinnerContact.setOnItemSelectedListener(this);

		myGroups = NebulaApplication.getInstance().getMyIdentity()
				.getMyGroups();
		groupManager = new RESTGroupManager();
	}

	public void doShowGroup(View v) {
		contact.setVisibility(View.INVISIBLE);
		group.setVisibility(View.VISIBLE);

		adapter.clear();
		for (int i = 0; i < myGroups.size(); i++) {
			if (myGroups.get(i).getGroupName().equals("ungrouped") == false) {
				adapter.add(myGroups.get(i));
			}
		}
		if (spinnerGroup.getAdapter() == null) {
			spinnerGroup.setAdapter(adapter);
		}
	}

	public void doShowContact(View v) {
		contact.setVisibility(View.VISIBLE);
		group.setVisibility(View.INVISIBLE);

		adapter.clear();
		List<String> addedProfiles = new ArrayList<String>();
		for (Group group : myGroups) {
			for (Profile profile : group.getContacts()) {
				if (!profile.getUsername().equals("null")
						&& !addedProfiles.contains(profile.getUsername())) {
					adapter.add(profile);
					addedProfiles.add(profile.getUsername());
				}
			}
		}

		if (spinnerContact.getAdapter() == null) {
			spinnerContact.setAdapter(adapter);
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Object selectedItem = parent.getItemAtPosition(pos);

		isLinkedItemsChanged = false;
		if (radioGroup.isChecked()) {
			renameGroup.setText(selectedItem.toString());
			selectContact
					.setText(retrieveContactsInGroup((Group) selectedItem));
			selectedItemId = ((Group) selectedItem).getId();
		} else if (radioContact.isChecked()) {
			renameContact.setText(selectedItem.toString());
			selectedItemId = ((Profile) selectedItem).getId();
		}
	}

	public String retrieveContactsInGroup(Group selectedGroup) {
		// get all contacts
		List<String> contactsList = new ArrayList<String>();
		List<Integer> contactsIdList = new ArrayList<Integer>();
		for (Group group : myGroups) {
			for (Profile profile : group.getContacts()) {
				if (!profile.getUsername().equals("null")
						&& !contactsList.contains(profile.getUsername())) {
					contactsList.add(profile.getUsername());
					contactsIdList.add(profile.getId());
				}
			}
		}

		linkedItemNames = new String[contactsList.size()];
		linkedItemIds = new Integer[contactsList.size()];
		selectedLinkedItems = new boolean[contactsList.size()];
		contactsList.toArray(linkedItemNames);
		contactsIdList.toArray(linkedItemIds);
		// prepare parameters for dialog

		// set checked the existing contacts
		StringBuilder contactsInGroup = new StringBuilder();
		for (Profile profile : selectedGroup.getContacts()) {
			contactsInGroup.append(" [" + profile.getUsername() + "] ");
			int index = contactsList.indexOf(profile.getUsername());
			if (index >= 0) {
				selectedLinkedItems[index] = true;
			}
		}

		return contactsInGroup.toString();
	}

	public void doModifyContact(View v) {
		// do this only if the contacts have not been modified
		if (!isLinkedItemsChanged) {
			isLinkedItemsChanged = true;
			retrieveContactsInGroup((Group) spinnerGroup.getSelectedItem());
		}

		new AlertDialog.Builder(this)
				.setTitle("Add Contacts")
				.setMultiChoiceItems(linkedItemNames, selectedLinkedItems, this)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						EditText text = (EditText) findViewById(R.id.etSelectContact);
						StringBuilder selectedContactNames = new StringBuilder();
						for (int i = 0; i < selectedLinkedItems.length; i++) {
							if (selectedLinkedItems[i] == true) {
								selectedContactNames.append(" ["
										+ linkedItemNames[i] + "] ");
							}
						}
						text.setText(selectedContactNames.toString());
					}
				}).show();
	}

	public void doUpdateGroup(View v) {
		if (renameGroup.length() == 0) {
			Toast.makeText(getApplication(), "Please fill Group Name",
					Toast.LENGTH_LONG).show();
			return;
		}

		Group newGroup = new Group(selectedItemId, renameGroup.getText()
				.toString(), "Available");
		for (int i = 0; i < selectedLinkedItems.length; i++) {
			if (selectedLinkedItems[i] == true) {
				newGroup.getContacts().add(
						new Profile(linkedItemIds[i], "", "", "", ""));
			}
		}

		try {
			Status status = groupManager.modifyGroup(newGroup);
			if (!status.isSuccess()) {
				throw new Exception(status.getMessage());
			} else {
				setResult(EDITGROUP_SUCCESSFUL);
				Toast.makeText(getApplicationContext(),
						"Group Updated Successfully", Toast.LENGTH_LONG).show();
				finish();
			}
		} catch (Exception e) {
			setResult(EDITGROUP_FAILURE);
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void doUpdateContact(View v) {
		try {
			Status status = groupManager.modifyContact((Profile) spinnerContact
					.getSelectedItem(), renameContact.getText().toString());
			if (!status.isSuccess()) {
				throw new Exception(status.getMessage());
			} else {
				setResult(EDITCONTACT_SUCCESSFUL);
				Toast.makeText(getApplicationContext(),
						"Contact Updated Successfully", Toast.LENGTH_LONG)
						.show();
				finish();
			}
		} catch (Exception e) {
			setResult(EDITCONTACT_FAILURE);
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void doBack(View v) {
		setResult(EDIT_CANCELLED);
		finish();
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