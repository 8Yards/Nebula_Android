package android.demo;

import java.util.ArrayList;

import org.nebula.restClient.RESTClient;
import org.nebula.restClient.RESTGroups;
import org.nebula.restClient.Response;
import org.nebula.userData.Group;
import org.nebula.userData.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Addcontacts extends Activity {
	DBClass _DB = new DBClass(this);
	protected CharSequence[] _options = { "8yards", "Salcas", "Carenet",
			"Minne" };
	protected boolean[] _selections = new boolean[_options.length];

	protected Button _optionsButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_contact);
		_optionsButton = (Button) findViewById(R.id.Button01);
		_optionsButton.setOnClickListener(new ButtonClickHandler());

		// EditText text=(EditText)findViewById(R.id.EditText01);
		Button add = (Button) findViewById(R.id.add);
		Button back = (Button) findViewById(R.id.back);

		/*
		 * Spinner spinner = (Spinner) findViewById(R.id.spinner);
		 * ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		 * this, R.array.group_prompt, android.R.layout.simple_spinner_item);
		 * adapter
		 * .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item
		 * ); spinner.setAdapter(adapter);
		 */

		back.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

		add.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String name = UserData.getInstance().getUserName() ; 
			     String pass = UserData.getInstance().getUserPassword() ;
				RESTClient rc = new RESTClient(name,pass);
				  //create a virtual profile
				  RESTGroups rG = new RESTGroups(rc);
				  Group group = new Group();
				ArrayList<Profile> aLP = new ArrayList<Profile>();
				//adding three empty profile as example, we need to manipulate real data
				  Profile prof1 = new Profile();
				  prof1.setId(1);
				  aLP.add(prof1);
				  Profile prof2 = new Profile();
				  prof2.setId(2);
				  aLP.add(prof2);
				  Profile prof3 = new Profile();
				  prof3.setId(3);
				  aLP.add(prof3);
				  //add the contact into the group
				  group.setContacts(aLP);
				  Response r=rG.insertUsersIntoGroup(group);
				//insert into database the users
				
				//_DB.open();

				EditText Edit_user = (EditText) findViewById(R.id.edittext);
				// Spinner Edit_group=(Spinner)findViewById(R.id.spinner);

				// EditText Edit_conPassword=(EditText)findViewById(R.id.cpwd);

				String vuser = Edit_user.getText().toString().trim();
				// String vgroup=Edit_group.getContext().toString();

				// String vConpass=Edit_conPassword.getText().toString();

				Cursor d = _DB.getTitle1(vuser);
				try {
					if (d.moveToFirst()) {
						String str = d.getString(0);
						Toast.makeText(view.getContext(),
								str + " This Contact already Exits!!!",
								Toast.LENGTH_LONG).show();
					} else {
						_DB.insertTitle1(vuser, null);
						Toast.makeText(view.getContext(), "SAVE SUCCESSFULLY",
								Toast.LENGTH_LONG).show();
						Intent intent = new Intent(Addcontacts.this, Main.class);
						startActivity(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				_DB.close();

			}
		});
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			showDialog(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
				.setTitle("Select Groups")
				.setMultiChoiceItems(_options, _selections,
						new DialogSelectionClickHandler())
				.setPositiveButton("OK", new DialogButtonClickHandler())
				.create();
	}

	public class DialogSelectionClickHandler implements
			DialogInterface.OnMultiChoiceClickListener {
		String mvalue = "";
		EditText text = (EditText) findViewById(R.id.EditText01);
		
		public void onClick(DialogInterface dialog, int clicked,
				boolean selected) {
			
			Log.i("ME", _options[clicked] + " selected: " + selected);
			mvalue = mvalue + ", " + _options[clicked];
			text.setText(mvalue);
	}
			
	}

	public class DialogButtonClickHandler implements
			DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialog, int clicked) {
			switch (clicked) {
			case DialogInterface.BUTTON_POSITIVE:
				printSelectedPlanets();
				break;
			}
		}
	}

	protected void printSelectedPlanets() {

		// Log.i( "ME", _options[ i ] + " selected: " + _selections[i] );
		// String mvalue="";
		// EditText text=(EditText)findViewById(R.id.EditText01);
		for (int i = 0; i < _options.length; i++) {

			// text.getText();
			// Toast.makeText(getApplicationContext(),"_selections[i],Toast.LENGTH_LONG).show();
		}
	}


}