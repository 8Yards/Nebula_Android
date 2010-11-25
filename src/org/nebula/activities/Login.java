/*
 * author: saad ali
 * rearchitecture and programming: prajwol kumar nakarmi, saad ali, michel hognerund
 */
package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.sip.SIPManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Login extends Activity {

	EditText userName;
	EditText password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		userName = (EditText) findViewById(R.id.etUserID);
		password = (EditText) findViewById(R.id.etPassword);
	}

	public void doSignInToNebula(View v) {
		int status = SIPManager.doLogin(userName.getText().toString(), password
				.getText().toString());
		setResult(status);

		if (status == SIPManager.LOGIN_SUCCESSFUL) {
			//showAlert("Good credentials :D");
			finish();
		} else if (status == SIPManager.LOGIN_FAILURE) {
			showAlert("Bad credentials :P");
		}
	}

	public void doRegisterToNebula(View v) {
		// TODO: implement this
	}

	public void showAlert(String message) {
		new AlertDialog.Builder(this).setMessage(message).show();
	}

}
