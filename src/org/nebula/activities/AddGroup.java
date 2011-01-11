/*
 * author: saad ali
 * rearchitecture and programming: saad ali
 */
package org.nebula.activities;

import java.util.ArrayList;

import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Status;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AddGroup extends Activity implements OnSeekBarChangeListener {

	public static final int ADDGROUP_SUCCESSFULL = 1;
	public static final int ADDGROUP_FAILURE = 0;

	private AbsoluteLayout group;
	private EditText groupName;

	private AbsoluteLayout spatial;

	private MyIdentity myIdentity;
	private SeekBar seekBar;
	private RESTConversationManager rC;
	float[][] usersPosition;

	private TextView seekUsersNumber;
	private TextView seekDistance;
	private TextView seekBarValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_group);
		group = (AbsoluteLayout) findViewById(R.id.abGroup);
		groupName = (EditText) findViewById(R.id.etGroupName);

		spatial = (AbsoluteLayout) findViewById(R.id.abSpatial);
		seekBar = (SeekBar) findViewById(R.id.sbVolume);
		seekBarValue = (TextView) findViewById(R.id.tvVolumeValue);
		seekDistance = (EditText) findViewById(R.id.etDistance);
		seekUsersNumber = (EditText) findViewById(R.id.etUsers);
		seekBar.setProgress(1);

		seekBar.setOnSeekBarChangeListener(this);

		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		rC = new RESTConversationManager();
		try {
			usersPosition = rC.distanceFromContact();
		} catch (Exception e) {
			// do nothing here
			Log.e("nebula-gps", "AddGeoGroup.onCreate: " + e.getMessage());
		}
	}

	public void doShowGroup(View v) {
		spatial.setVisibility(View.INVISIBLE);
		group.setVisibility(View.VISIBLE);
	}

	public void doShowSpatial(View v) {
		group.setVisibility(View.INVISIBLE);
		spatial.setVisibility(View.VISIBLE);
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

				// TODO:: the error is not always duplicate :@
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

	public void addSpacialConversation(View v) {
		if (seekUsersNumber.getText().toString().equals("0   Users")) {
			Toast.makeText(getApplicationContext(),
					"No users to create group from", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			double distance = usersPosition[rC.returnIndex(seekBar
					.getProgress())][0];
			ConversationThread thread = myIdentity.createThread();
			Conversation conversation = thread
					.addConversation(new ArrayList<String>());

			rC.createSpatialConversation(conversation, distance);
		} catch (Exception e) {
			// do nothing here
			Log.e("nebula-gps", "AddGeoGroup.addSpacialConversation: "
					+ e.getMessage());
			Toast.makeText(getApplicationContext(),
					"Could not perform operation", Toast.LENGTH_LONG).show();
		}

		finish();
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		seekBarValue.setText("");
		if (usersPosition != null) {
			seekDistance.setText(""
					+ usersPosition[rC.returnIndex(seekBar.getProgress())][0]
					+ "   Km");
			seekUsersNumber
					.setText(""
							+ (int) usersPosition[rC.returnIndex(seekBar
									.getProgress())][1] + "   Users");
		} else {
			seekDistance.setText(""
					+ rC.getDistancesValue(rC
							.returnIndex(seekBar.getProgress())) + "   Km");
			seekUsersNumber.setText("0   Users");
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar arg0) {
	}
}