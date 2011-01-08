/*
 * author: saad ali
 * author - refactor and rearchitecture: prajwol kumar nakarmi, saad ali
 */

package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends TabActivity {
	public static final int SHOW_SUB_ACTIVITY_LOGIN = 1;
	public static final String CONTACTS_TAB = "contacts";
	public static final String CONVERSATION_TAB = "conversation";

	private TabHost tabHost;
	private MyIdentity myIdentity = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		loadUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (SHOW_SUB_ACTIVITY_LOGIN):
			if (resultCode == SIPManager.LOGIN_SUCCESSFUL
					|| resultCode == Register.REGISTER_SUCCESSFULL) {
				loadUI();
			} else {
				System.exit(-1);
			}
			break;
		default:
			break;
		}
	}

	private void loadUI() {
		if (myIdentity == null || myIdentity.getMyUserName() == null
				|| myIdentity.getMyUserName().trim().equals("")) {
			// user is not logged in. start the login activity
			Intent myIntent = new Intent(Main.this, Login.class);
			startActivityForResult(myIntent, SHOW_SUB_ACTIVITY_LOGIN);
		} else {
			setContentView(R.layout.main);
			//NebulaApplication.getInstance().reloadMyGroups();

			tabHost = (TabHost) findViewById(android.R.id.tabhost);

			TabSpec contactSpec = tabHost.newTabSpec(CONTACTS_TAB)
					.setIndicator("Contacts",
							getResources().getDrawable(R.drawable.contacts))
					.setContent(new Intent(Main.this, ContactsTab.class));

			TabSpec conversationSpec = tabHost.newTabSpec(CONVERSATION_TAB)
					.setIndicator("Conversation",
							getResources().getDrawable(R.drawable.groups))
					.setContent(new Intent(Main.this, ConversationTab.class));

			tabHost.addTab(contactSpec);
			tabHost.addTab(conversationSpec);
		}
	}

	public void setTabByTag(String tabTag) {
		tabHost.setCurrentTabByTag(tabTag);
	}
}
