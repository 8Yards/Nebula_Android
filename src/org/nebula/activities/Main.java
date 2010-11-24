/*
 * author: saad ali
 * author - refactor and rearchitecture: prajwol kumar nakarmi, saad ali, michel hognurand
 */

package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;

public class Main extends TabActivity {
	private static final int SHOW_SUB_ACTIVITY_LOGIN = 1;

	private MyIdentity myIdentity = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: extract the code to appropriate place
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		if (myIdentity == null || myIdentity.getMySIPName() == null
				|| myIdentity.getMySIPName().trim().equals("")) {
			// user is not logged in. start the login activity
			Intent myIntent = new Intent(Main.this, Login.class);
			startActivityForResult(myIntent, SHOW_SUB_ACTIVITY_LOGIN);
		} else {
			loadUI();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (SHOW_SUB_ACTIVITY_LOGIN): {
			if (resultCode == SIPManager.LOGIN_SUCCESSFUL) {
				loadUI();
			} else {
				System.exit(-1);
			}

			break;
		}
		default:
			break;
		}

	}

	private void loadUI() {
		setContentView(R.layout.main);

		/*
		 * Resources res = getResources(); // Resource object to get Drawables
		 * TabHost tabHost = getTabHost(); // The activity TabHost
		 * TabHost.TabSpec spec; // Resusable TabSpec for each tab Intent
		 * intent; // Reusable Intent for each tab
		 * 
		 * // Create an Intent to launch an Activity for the tab (to be reused)
		 * intent = new Intent().setClass(this, Contacts.class);
		 * 
		 * // Initialize a TabSpec for each tab and add it to the TabHost spec =
		 * tabHost .newTabSpec("contacts") .setIndicator("Contacts",
		 * res.getDrawable(R.drawable.ic_tab_albums)) .setContent(intent);
		 * tabHost.addTab(spec);
		 * 
		 * // Do the same for the other tabs intent = new
		 * Intent().setClass(this, Conversation.class); spec = tabHost
		 * .newTabSpec("conversation") .setIndicator("Conversation",
		 * res.getDrawable(R.drawable.ic_tab_artists)) .setContent(intent);
		 * tabHost.addTab(spec);
		 * 
		 * 
		 * 
		 * tabHost.setCurrentTab(0);
		 */
	}

}
