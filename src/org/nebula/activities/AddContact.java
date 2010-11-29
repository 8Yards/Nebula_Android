/*
 * author venkatesh, saad
 */

package org.nebula.activities;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rest.Status;
import org.nebula.models.Group;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddContact extends Activity 
{
	private RESTGroupManager groupManager;

	private Status status;
	public static final int ADDCONTACT_SUCCESS = 1;
	public static final int ADDCONTACT_FAILURE = 0;

	private List<Group> groups;
	private CharSequence[] groupNames;
	private boolean[] selectedGroups;

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_contact);

		groupManager = new RESTGroupManager();
	}

	public void doSelectGroups(View v) {
		showDialog(0);
	}

	protected Dialog onCreateDialog(int id) 
	{
		AlertDialog ad = null;

		try {
			groups = groupManager.retrieveGroups();
			groupNames = new CharSequence[groups.size()]; 
			selectedGroups = new boolean[groups.size()];

			for(int i=0;i<groups.size();i++)
			{
				groupNames[i] = groups.get(i).getGroupName();
			}

			ad = new AlertDialog.Builder(this)
			.setTitle("Select Groups")
			.setMultiChoiceItems(
					groupNames, 
					selectedGroups,
					new DialogSelectionClickHandler())
			.setPositiveButton("OK",new OKHandler())
			.create();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ad;
	}

	
	protected class OKHandler implements DialogInterface.OnClickListener
	{
			String selectedGroupNames;
			EditText text = (EditText) findViewById(R.id.etSelectedGroups);

			public void onClick(DialogInterface dialog, int which) {
				selectedGroupNames = "";
				for (int i=0; i<groupNames.length;i++) {
					if(selectedGroups[i]==true){
						selectedGroupNames = selectedGroupNames + " [" + groupNames[i] + "] ";
					}
				}
				text.setText(selectedGroupNames);
			}
	}

	public void doAddContact(View v)
	{
		EditText etContactName = (EditText) findViewById(R.id.etContactName);
		EditText etNebulaId = (EditText) findViewById(R.id.etNebulaID);

		String contactName = etContactName.getText().toString();
		String nebulaId = etNebulaId.getText().toString();

		if(contactName == null || contactName.length() == 0
				|| nebulaId == null || nebulaId.length() == 0)
		{
			Toast.makeText(v.getContext(), "ONE OR MORE THE FIELDS ARE NOT SPECIFIED",
					Toast.LENGTH_LONG).show();
		}
		else
		{
			try {
				status  = groupManager.addContact(contactName, nebulaId);
				for(int i=0;i<selectedGroups.length;i++)
				{
					groupManager.insertUserIntoGroup(groups.get(i), nebulaId);
				}
			} catch (ClientProtocolException e) {
				doBack(v);
				e.printStackTrace();
			} catch (IOException e) {
				doBack(v);
				e.printStackTrace();
			} catch (JSONException e) {
				doBack(v);
				e.printStackTrace();
			}
			
			setResult(status.isSuccess()?ADDCONTACT_SUCCESS:ADDCONTACT_FAILURE);
			Toast.makeText(this.getApplicationContext(), status.getMessage(),
					Toast.LENGTH_LONG).show();
			finish();
			Intent myIntent = new Intent(AddContact.this, Main.class);
			startActivity(myIntent);
		}
	}

	public void doBack(View v) {
		setResult(ADDCONTACT_SUCCESS);
		finish();
		Intent myIntent = new Intent(AddContact.this, Main.class);
		startActivity(myIntent);
	}
	
	protected class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener 
	{
		public void onClick(DialogInterface dialog, int clicked,
				boolean selected) {}
	}
}