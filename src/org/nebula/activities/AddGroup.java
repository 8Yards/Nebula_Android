/*
 * author: saad ali
 * rearchitecture and programming: saad ali
 */
package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.models.Group;
import org.nebula.models.Status;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddGroup extends Activity {

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
		if (groupName.length() == 0) {
			Toast.makeText(getApplication(), "Please fill Group Name",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		RESTGroupManager groupManager = new RESTGroupManager();
		Group newGroup = new Group(0, groupName.getText().toString(),
				"Available");
		try {
			Status status = groupManager.addNewGroup(newGroup);
			if (status.isSuccess() == false) {
				setResult(ADDGROUP_FAILURE);
				
				//TODO:: the error is not always duplicate :@
				Toast.makeText(getApplicationContext(),
						"Group Name already exists", Toast.LENGTH_LONG).show();
			} else {
				setResult(ADDGROUP_SUCCESSFULL);
				Toast.makeText(getApplicationContext(),
						"Group Created Successfully", Toast.LENGTH_LONG).show();
				finish();
			}
		} catch (Exception e) {
			setResult(ADDGROUP_FAILURE);
			Log.e("nebula", e.getMessage());
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	public void doBackToMain(View v) {
		setResult(ADDGROUP_FAILURE);
		finish();
	}
}