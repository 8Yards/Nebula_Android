/*
 * author: saad ali
 * rearchitecture and programming: saad ali
 */
package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rest.Status;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Group;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddGroup extends Activity {
	private static final int SHOW_SUB_ACTIVITY_ContactsTab=1;
	
	public static final int ADDGROUP_SUCCESSFULL = 1;
	public static final int ADDGROUP_FAILURE = 0;

	private EditText groupName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_group);

		groupName = (EditText) findViewById(R.id.etGroupName);
	}

	public void doAddGroup(View v) {
		if(groupName.length()==0)
		{
			Toast.makeText(getApplication(),
					"Please fill Group Name", Toast.LENGTH_LONG).show();
		}
		else{
		RESTGroupManager groupManager = new RESTGroupManager();
		Group newGroup = new Group(0, groupName.getText().toString(),
				"Available");
		
		try {
			Status status = groupManager.addNewGroup(newGroup);
			
			if (status.isSuccess() == false) {
				Toast.makeText(getApplicationContext(),
						status.getMessage(), Toast.LENGTH_LONG).show();
				//throw new Exception(status.getMessage());
			} else {

				Toast.makeText(getApplicationContext(),
						"Group Created Successfully", Toast.LENGTH_LONG).show();

				setResult(ADDGROUP_SUCCESSFULL);
				Intent myIntent = new Intent(AddGroup.this, Main.class);
				startActivityForResult(myIntent, SHOW_SUB_ACTIVITY_ContactsTab);
			}
		} catch (Exception e) {

			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			setResult(ADDGROUP_FAILURE);
		}
		}
	}
	public void doBackToMain(View v) {
		setResult(ADDGROUP_FAILURE);
		finish();
	}
	
	
	
}