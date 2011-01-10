/*
 * author: saad ali
 * rearchitecture and programming: prajwol kumar nakarmi, saad ali, michel hognerund
 */
package org.nebula.activities;

import java.util.Map;

import org.nebula.R;
import org.nebula.client.localdb.NebulaLocalDB;
import org.nebula.client.localdb.NebulaSettingsManager;
import org.nebula.client.sip.NebulaSIPConstants;
import org.nebula.client.sip.SIPManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity {
	private static final int SHOW_SUB_ACTIVITY_REGISTER = 1;

	private Message myMessage;
	private ProgressDialog busyDialog;
	private NebulaSettingsManager settingsManager;

	private EditText userName;
	private EditText password;
	private CheckBox rememberPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		busyDialog = new ProgressDialog(this);
		busyDialog.setIndeterminate(true);
		busyDialog.setMessage("Please wait ...");
		settingsManager = new NebulaSettingsManager(this);

		Map<String, String> params = settingsManager.getLoginParameters();

		userName = (EditText) findViewById(R.id.etUserID);
		password = (EditText) findViewById(R.id.etPassword);
		rememberPassword = (CheckBox) findViewById(R.id.cbRemember);

		userName.setText(params.get(NebulaLocalDB.KEY_USER));
		password.setText(params.get(NebulaLocalDB.KEY_PASSWORD));
		rememberPassword.setChecked(Boolean.valueOf(params
				.get(NebulaLocalDB.KEY_CHECKED)));
	}

	public void doSignInToNebula(View v) {
		busyDialog.show();
		new Thread() {
			public void run() {
				int status = SIPManager.doLogin(userName.getText().toString(),
						password.getText().toString());
				setResult(status);
				myMessage = Message.obtain();
				myMessage.setTarget(myHandler);
				myMessage.what = status;

				if (status == NebulaSIPConstants.REGISTER_SUCCESSFUL) {
					settingsManager.storeLoginParameters(userName.getText()
							.toString(), password.getText().toString(),
							rememberPassword.isChecked());
				}
				myMessage.sendToTarget();
			}
		}.start();
	}

	public void doRegisterToNebula(View v) {
		Intent myIntent = new Intent(Login.this, Register.class);
		startActivityForResult(myIntent, SHOW_SUB_ACTIVITY_REGISTER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		setResult(resultCode);
		switch (requestCode) {
		case (SHOW_SUB_ACTIVITY_REGISTER):
			if (resultCode == Register.REGISTER_SUCCESSFULL) {
				finish();
			} else {
				// TODO:: recheck if this is good way
				System.exit(-1);
			}
		default:
			break;
		}
	}

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			busyDialog.dismiss();
			switch (msg.what) {
			case NebulaSIPConstants.REGISTER_SUCCESSFUL:
				finish();
				break;
			case NebulaSIPConstants.REGISTER_FAILURE:
				showAlert("Invalid credentials");
				break;
			case NebulaSIPConstants.REGISTER_SIPEXCEPTION:
				showAlert("Could not connect to server");
				break;
			default:
				break;
			}

		}
	};

	public void showAlert(String message, Context context) {
		new AlertDialog.Builder(context).setMessage(message).show();
	}

	public void showAlert(String message) {
		showAlert(message, this);
	}
}
