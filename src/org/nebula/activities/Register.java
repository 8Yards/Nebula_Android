/*
 * author: saad ali
 * rearchitecture and programming: saad ali, prajwol
 * validation: sharique
 */
package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.rest.RESTProfileManager;
import org.nebula.client.rest.Status;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Activity {
	public static final int REGISTER_SUCCESSFULL = 1;
	public static final int REGISTER_FAILURE = 0;

	private EditText fullName;
	private EditText userName;
	private EditText emailAddress;
	private EditText password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		fullName = (EditText) findViewById(R.id.etFullName);
		userName = (EditText) findViewById(R.id.etNebulaName);
		emailAddress = (EditText) findViewById(R.id.etEmailAddress);
		password = (EditText) findViewById(R.id.etPassword);
	}

	public void doRegsiterToNebula(View v) {
		if (userName.length() <= 0 || fullName.length() <= 0
				|| password.length() <= 0 || emailAddress.length() <= 0) {
				Toast.makeText(v.getContext(), "Please fill all fields!",
				Toast.LENGTH_LONG).show();

		}
		else
		{
		RESTProfileManager profileManager = new RESTProfileManager();
		Profile newProfile = new Profile(0, userName.getText().toString(),
				fullName.getText().toString(), emailAddress.getText()
						.toString(), password.getText().toString());

		MyIdentity myIdentity = NebulaApplication.getInstance().getMyIdentity();

		try {
			Status status = profileManager.register(newProfile);

			if (status.isSuccess() == false) {
				throw new Exception(status.getMessage());
			} else {
				myIdentity.setMyUserName(userName.getText().toString());
				myIdentity.setMyPassword(password.getText().toString());

				Toast.makeText(getApplicationContext(),
						"Account Created Successfully", Toast.LENGTH_LONG)
						.show();

				setResult(REGISTER_SUCCESSFULL);
				finish();
			}
		} catch (Exception e) {
			myIdentity.setMyUserName("");
			myIdentity.setMyPassword("");

			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			setResult(REGISTER_FAILURE);
		}
		}
	}

	public void doBackToSignIn(View v) {
		setResult(REGISTER_FAILURE);
		finish();
	}
}