/*
 * author: venkatesh, saad
 * refactor: prajwol, venkatesh
 */

package org.nebula.activities;

import java.util.List;

import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rest.Status;
import org.nebula.models.Group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddContact extends Activity implements
		DialogInterface.OnClickListener,
		DialogInterface.OnMultiChoiceClickListener {
	private RESTGroupManager groupManager;

	private Status status;
	public static final int ADDCONTACT_SUCCESS = 1;
	public static final int ADDCONTACT_FAILURE = 0;

	private List<Group> groups;
	private String[] groupNames;
	private boolean[] selectedGroups;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_contact);

		groupManager = new RESTGroupManager();
	}

	public void doSelectGroups(View v) {
		try {
			groups = groupManager.retrieveGroups();
		} catch (Exception e) {
			Toast.makeText(v.getContext(),
					"Groups could not be fetched",
					Toast.LENGTH_LONG).show();
			Log.e("nebula", "addcontact: " + e.getMessage());
			return;
		}

		groupNames = new String[groups.size()];
		selectedGroups = new boolean[groups.size()];

		for (int i = 0; i < groups.size(); i++) {
			groupNames[i] = groups.get(i).getGroupName();
		}

		new AlertDialog.Builder(this).setTitle("Select Groups")
				.setMultiChoiceItems(groupNames, selectedGroups, this)
				.setPositiveButton("OK", this).show();
	}

	public void onClick(DialogInterface dialog, int which) {
		EditText text = (EditText) findViewById(R.id.etSelectedGroups);
		String selectedGroupNames = "";
		for (int i = 0; i < groupNames.length; i++) {
			if (selectedGroups[i] == true) {
				selectedGroupNames = selectedGroupNames + " [" + groupNames[i]
						+ "] ";
			}
		}
		text.setText(selectedGroupNames);
	}

	public void onClick(DialogInterface dialog, int clicked, boolean selected) {
		// do nothing here :P
	}

	public void doAddContact(View v) {
		String contactName = ((EditText) findViewById(R.id.etContactName))
				.getText().toString();
		String nebulaId = ((EditText) findViewById(R.id.etNebulaID)).getText()
				.toString();

		if (contactName == null || contactName.length() == 0
				|| nebulaId == null || nebulaId.length() == 0) {
			Toast.makeText(v.getContext(),
					"ONE OR MORE THE FIELDS ARE NOT SPECIFIED",
					Toast.LENGTH_LONG).show();
			return;
		}

		try {
			status = groupManager.addContact(nebulaId, contactName);

			if (!status.isSuccess()) {
				throw new Exception(status.getMessage());
			}

		} catch (Exception e) {
			Toast.makeText(this.getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e("nebula", "Add Contact: REST server error");

			setResult(ADDCONTACT_FAILURE);
			return;
		}

		if(selectedGroups!=null){
		for (int i = 0; i < selectedGroups.length; i++) {
			// TODO Remove this try catch and club the job of adding the
			// new contact
			// into the mentioned groups also with the REST call above
			try {
				if (selectedGroups[i]) {
					groupManager.insertUserIntoGroup(groups.get(i), nebulaId);
				}
			} catch (Exception e) {
				// continue to the next
				Log.e("nebula", "addcontact: " + e.getMessage());
			}
		}
		}
		Toast.makeText(this.getApplicationContext(), status.getMessage(),
				Toast.LENGTH_LONG).show();
		setResult(ADDCONTACT_SUCCESS);
		finish();
	}

	public void doBack(View v) {
		setResult(ADDCONTACT_FAILURE);
		finish();
	}
}