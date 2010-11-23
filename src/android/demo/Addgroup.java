package android.demo;

import org.nebula.restClient.RESTClient;
import org.nebula.restClient.RESTGroups;
import org.nebula.restClient.Response;
import org.nebula.userData.Group;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Addgroup extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_group);

		Button add = (Button) findViewById(R.id.add);
		Button back = (Button) findViewById(R.id.back);

		add.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText grptxt = (EditText) findViewById(R.id.edittext);
				String grpname = grptxt.getText().toString();
				// provide HTTP auth credentia
				String name = UserData.getInstance().getUserName();
				String pass = UserData.getInstance().getUserPassword();
				RESTClient rc = new RESTClient(name, pass);
				// create a virtual profile
				RESTGroups rG = new RESTGroups(rc);
				// create the real profile
				Group group = new Group();
				// set the attributes
				group.setGroupName(grpname);
				// contact.setDomain("nebula.com");
				group.setGroupStatus("Available");
				Response r = rG.addNewGroup(group);

				Integer result = r.getStatus();
				

				if (result >= 200 && result <= 299) {
					Toast.makeText(v.getContext(),
							"Group " + grpname + " Created", Toast.LENGTH_LONG)
							.show();
					Intent intent = new Intent(Addgroup.this, Main.class);
					startActivity(intent);
					

				} else {
					Toast.makeText(v.getContext(), grpname + " Already exist",
							Toast.LENGTH_LONG).show();

				}
			}
		});

		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();

			}

		});

	}
}
