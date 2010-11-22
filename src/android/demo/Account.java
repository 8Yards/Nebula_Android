package android.demo;

import org.nebula.restClient.RESTClient;
import org.nebula.restClient.RESTProfiles;
import org.nebula.restClient.Response;
import org.nebula.userData.Profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Account extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account);
		
		Button btnCreate = (Button) findViewById(R.id.createbutton);
		Button btncancel = (Button) findViewById(R.id.cancel);

		btnCreate.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				EditText Edit_useName = (EditText) findViewById(R.id.nname);
				EditText Edit_FullName = (EditText) findViewById(R.id.fname);
				EditText Edit_emailAddress = (EditText) findViewById(R.id.eaddress);
				EditText Edit_password = (EditText) findViewById(R.id.pwd);
				// EditText Edit_conPassword=(EditText)findViewById(R.id.cpwd);

				String vUsername = Edit_useName.getText().toString();
				String vFullname = Edit_FullName.getText().toString();
				String vEmail = Edit_emailAddress.getText().toString();
				String vPassword = Edit_password.getText().toString();
				// String vConpass=Edit_conPassword.getText().toString();

				Profile myProfile = new Profile();
				myProfile.setUsername(vUsername);
				myProfile.setPassword(vPassword);
				myProfile.setFullName(vFullname);
				myProfile.setEmail_address(vEmail);
				// myProfile.setFullName("Michel H");
				// myProfile.setEmail_address("leyou.m@gmail.com");
				RESTClient rc = new RESTClient();
				RESTProfiles p = new RESTProfiles(rc);
				Response r;
				r = p.register(myProfile);
				Log.v("nebula", Integer.toString(r.getStatus()));
				Log.v("nebula", r.getResult().toString());
				Log.v("nebula", myProfile.getUsername());

				int mresult = r.getStatus();
				//Toast.makeText(v.getContext(), mresult,Toast.LENGTH_LONG).show();
				if (mresult >= 200 && mresult<=299) {
					Toast.makeText(v.getContext(), "User Created Successfully",
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(Account.this, nebula.class);
					startActivity(intent);
					
				} else {
					Toast.makeText(v.getContext(),vUsername+ " Already exist",
							Toast.LENGTH_LONG).show();

				}
				}
		});

		btncancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}