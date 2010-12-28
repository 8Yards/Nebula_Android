/*
 * author: saad ali
 * rearchitecture and programming: prajwol kumar nakarmi, saad ali, michel hognerund
 */
package org.nebula.activities;

import java.util.Map;

import org.nebula.R;
import org.nebula.client.localdb.NebulaLocalDB;
import org.nebula.client.localdb.NebulaSettingsManager;
import org.nebula.client.sip.SIPManager;
import org.nebula.utils.NebulaTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity {
	private static final int SHOW_SUB_ACTIVITY_REGISTER = 1;

	private NebulaSettingsManager settingsManager;

	private EditText userName;
	private EditText password;
	private CheckBox rememberPassword;

	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

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
		pd = ProgressDialog.show(this, "", "Loading. Please wait...", true);

		new NebulaTask() {
			protected Long doInBackground(Object... params) {
				String userName = (String)params[0];
				String password = (String)params[1];
				boolean rememberPassword = (Boolean)params[2];
				
				int status = SIPManager.doLogin(userName, password);
				setResult(status);

				if (status == SIPManager.LOGIN_SUCCESSFUL) {
					settingsManager
							.storeLoginParameters(userName,
								password, rememberPassword);
					finish();
				} else if (status == SIPManager.LOGIN_FAILURE) {
					
					runOnUiThread(new Runnable() {
						public void run() {
								showAlert("Invalid credentials or Need internet connection");
						}
					});
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
							finishLoading();
					}
				});
				
				return null;
			}
		}.execute(userName.getText().toString(), password.getText().toString(), rememberPassword
				.isChecked());
	}

	private void finishLoading() {
		pd.cancel();
	}

	public void doRegisterToNebula(View v) {
		Intent myIntent = new Intent(Login.this, Register.class);
		startActivityForResult(myIntent, SHOW_SUB_ACTIVITY_REGISTER);
	}

	public void showAlert(String message, Context context) {
		new AlertDialog.Builder(context).setMessage(message).show();
	}

	public void showAlert(String message) {
		showAlert(message, this);
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
}
