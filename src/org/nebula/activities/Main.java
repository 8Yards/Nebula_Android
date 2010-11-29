/*
 * author: saad ali
 * author - refactor and rearchitecture: prajwol kumar nakarmi, saad ali
 */

package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.sip.SIPClient;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends TabActivity {
	private static final int SHOW_SUB_ACTIVITY_LOGIN = 1;

	private MyIdentity myIdentity = null;
	private PresenceReceiver presenceReceiver = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		loadUI();
	}

	@Override
	protected void onResume() {
		if (presenceReceiver == null) {
			presenceReceiver = new PresenceReceiver();		
			registerReceiver(presenceReceiver, new IntentFilter(
					SIPClient.NOTIFY_PRESENCE));
		}

		super.onResume();
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
			NebulaApplication.getInstance().reloadMyGroups();
			TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

			TabSpec contactSpec = tabHost.newTabSpec("contacts").setIndicator(
					"Contacts",
					getResources().getDrawable(R.drawable.ic_tab_albums))
					.setContent(new Intent(Main.this, ContactsTab.class));

			TabSpec conversationSpec = tabHost.newTabSpec("conversation")
					.setIndicator(
							"Conversation",
							getResources().getDrawable(
									R.drawable.ic_tab_artists)).setContent(
							new Intent(Main.this, ConversationTab.class));
			tabHost.addTab(contactSpec);
			tabHost.addTab(conversationSpec);
		}
	}

	public void doSubscribe(View v) {
//		EditText toUser = (EditText) findViewById(R.id.etToUser);
//		SIPManager.doSubscribe(toUser.getText().toString(), myIdentity
//				.getMySIPDomain());
		SIPManager.doLogout();
	}

	public class PresenceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] params = (Object[]) intent.getExtras().get("params");
			Log.v("nebula", "main:" + "params_len=" + params.length);
			for (int i = 0; i < params.length; i++) {
				Log.v("nebula", "main:" + "param-" + params[i]);
			}
			alert(params[1] + ", " + params[2]);
		}
	}

	public void alert(String message) {
		new AlertDialog.Builder(this).setMessage(message).show();
	}
}
