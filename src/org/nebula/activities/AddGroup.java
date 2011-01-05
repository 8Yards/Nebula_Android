/*
 * author: saad ali
 * rearchitecture and programming: saad ali
 * added spatial group UI : Marco
 */
package org.nebula.activities;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.client.rest.RESTGroupManager;
import org.nebula.client.rest.Status;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AddGroup extends Activity implements OnSeekBarChangeListener {

	public static final int ADDGROUP_SUCCESSFULL = 1;
	public static final int ADDGROUP_FAILURE = 0;

	private AbsoluteLayout group;
	private EditText groupName;

	private AbsoluteLayout spatial;

	MyIdentity myIdentity;
	private Context ctx = null;

	SeekBar seekBar;
	RESTConversationManager rC;
	float[][] usersPosition;

	private TextView seekUsersNumber;
	private TextView seekDistance;
	private TextView seekBarValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_group);
		myIdentity = NebulaApplication.getInstance().getMyIdentity();

		ctx = getApplication();
		rC = new RESTConversationManager();
		try {
			Log.e("INDEX", "before calling");
			usersPosition = rC.distanceFromContact();
			Log.e("INDEX", "after calling");

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
		groupName = (EditText) findViewById(R.id.etGroupName);
		seekBar = (SeekBar) findViewById(R.id.sbVolume);
		seekBarValue = (TextView) findViewById(R.id.tvVolumeValue);
		seekDistance = (EditText) findViewById(R.id.etDistance);
		seekUsersNumber = (EditText) findViewById(R.id.etUsers);
		group = (AbsoluteLayout) findViewById(R.id.abGroup);
		spatial = (AbsoluteLayout) findViewById(R.id.abSpatial);
		seekBar.setProgress(1);
		seekBar.setOnSeekBarChangeListener(this);
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

		ProgressDialog.show(this, "", "Loading. Please wait...", true);

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

	public void doCreateSpatial(View v) {
		Intent GPS = new Intent(this, AddGeoGroup.class);
		this.startActivity(GPS);
		finish();
	}

	public void doBackToMain(View v) {
		setResult(ADDGROUP_FAILURE);
		finish();
	}

	public void addSpacialConversation(View v) {
		ConversationThread convThread = myIdentity.createThread();
		double distance = usersPosition[rC.returnIndex(seekBar.getProgress())][0];
		Conversation conv = new Conversation(
				myIdentity.createNewConversationName());
		convThread.addConversation(conv);
		Log.e("nebula", "GEO Convesation Created");
		try {
			rC.createSpatialConversation(conv, distance);
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("nebula", "JSON GEO parsing done");

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
			seekDistance
					.setText(""
							+ rC.getDistancesValue(rC.returnIndex(seekBar
									.getProgress())) + "   Km");
			seekUsersNumber.setText("0   Users");
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar arg0) {
	}
}