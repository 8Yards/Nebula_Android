/*
 * author: saad, michel
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nebula.R;
import org.nebula.R.drawable;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.client.sip.NebulaSIPConstants;
import org.nebula.client.sip.SIPManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.Group;
import org.nebula.models.MyIdentity;
import org.nebula.models.Profile;
import org.nebula.models.Status;
import org.nebula.ui.ContactRow;
import org.nebula.ui.ConversationRow;
import org.nebula.ui.ConversationTabExpandableListAdapter;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Toast;

public class ConversationTab extends ExpandableListActivity implements
		OnClickListener {
	public static final String NOTIFY_MUTE = "NOTIFY_MUTE";
	public static final String NOTIFY_UNMUTE = "NOTIFY_UNMUTE";

	private List<ConversationRow> conversations = new ArrayList<ConversationRow>();
	private BaseExpandableListAdapter expListAdapter = null;

	private PresenceReceiver presenceReceiver = null;
	private InviteReceiver refreshReceiver = null;

	private Menu menu;
	public MyIdentity myIdentity;

	private String[] itemNames;
	private Conversation currentConversation;
	private SingleSelectHandler singleSelectHandler = new SingleSelectHandler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_tab);

		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		expListAdapter = new ConversationTabExpandableListAdapter(this,
				conversations);
		setListAdapter(expListAdapter);

		NebulaApplication.getInstance().reloadConversation();
		reloadConversationList();
	}

	@Override
	protected void onResume() {
		if (presenceReceiver == null) {
			presenceReceiver = new PresenceReceiver();
			registerReceiver(presenceReceiver, new IntentFilter(
					NebulaSIPConstants.NOTIFY_PRESENCE));
		}

		if (refreshReceiver == null) {
			refreshReceiver = new InviteReceiver();
			registerReceiver(refreshReceiver, new IntentFilter(
					NebulaSIPConstants.NOTIFY_INVITE));
		}
		reloadConversationList();
		super.onResume();
	}

	public void reloadConversationList() {
		conversations.clear();
		for (ConversationThread ct : myIdentity.getMyThreads()) {
			for (Conversation conv : ct.getMyConversations()) {
				conversations.add(new ConversationRow(conv));
			}
		}
		Collections.sort(conversations);
		expListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conversation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iMute:
			MenuItem iMute = menu.getItem(0);
			if (iMute.getTitle().equals("Mute")) {
				iMute.setTitle("Unmute");
				iMute.setIcon(drawable.unmute);
				// TODO:: mute it
			} else {
				iMute.setTitle("Mute");
				iMute.setIcon(drawable.mute);
				// TODO::unmute it
			}
			break;
		case R.id.iVolume:
			startActivity(new Intent(ConversationTab.this, Volume.class));
			break;
		case R.id.iSignout:
			SIPManager.doLogout();
			myIdentity.setMyUserName(null);
			finish();
			System.exit(-1);
			break;
		}
		return true;
	}

	public void doInviteMembers(View v) {
		currentConversation = (Conversation) v.getTag();

		List<String> contactNamesList = new ArrayList<String>();
		for (Group group : myIdentity.getMyGroups()) {
			for (Profile profile : group.getContacts()) {
				if (!profile.getUsername().equals("null")
						&& !contactNamesList.contains(profile.getUsername())
						&& !currentConversation.getCallee().contains(
								profile.getUsername())) {
					contactNamesList.add(profile.getUsername());
				}
			}
		}

		if (contactNamesList.size() == 0) {
			Toast.makeText(this.getApplicationContext(),
					"No contact to invite", Toast.LENGTH_LONG).show();
			return;
		}

		itemNames = new String[contactNamesList.size()];
		contactNamesList.toArray(itemNames);

		new AlertDialog.Builder(this).setTitle("Select Members")
				.setSingleChoiceItems(itemNames, -1, singleSelectHandler)
				.setPositiveButton("OK", this).show();
	}

	public void onClick(DialogInterface dialog, int which) {
		which = singleSelectHandler.getWhich();

		ConversationThread thread = currentConversation.getThread();
		List<String> newCalleList = new ArrayList<String>();
		for (String oldCallee : currentConversation.getCallee()) {
			newCalleList.add(oldCallee);
		}
		newCalleList.add(itemNames[which]);

		Conversation newConversation = thread.addConversation(newCalleList);
		try {
			RESTConversationManager conversationManager = new RESTConversationManager();
			Status status = conversationManager
					.addNewConversation(newConversation);
			if (!status.isSuccess()) {
				throw new Exception(status.getMessage());
			} else {
				SIPManager.doRefer(itemNames[which], myIdentity
						.getMySIPDomain(), thread.getThreadName(),
						currentConversation, newConversation);
				reloadConversationList();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public class PresenceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] params = (Object[]) intent.getExtras().get("params");
			String sipURI = params[1].toString().split("sip:")[1].split("@")[0];
			String status = params[2].toString();
			updateStatus(sipURI, status);
		}
	}

	public class InviteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			((Main) getParent()).setTabByTag(Main.CONVERSATION_TAB);
			reloadConversationList();
		}
	}

	public void updateStatus(String username, String status) {
		for (ConversationRow conversation : conversations) {
			for (ContactRow member : conversation.getMembers()) {
				if (member.getUserName().equals(username)) {
					member.setStatus(status);
				}
			}
		}

		expListAdapter.notifyDataSetChanged();
	}

	private class SingleSelectHandler implements OnClickListener {
		private int which = -1;

		public void onClick(DialogInterface dialog, int which) {
			this.which = which;
		}

		public int getWhich() {
			return this.which;
		}
	}

	// public boolean onLongClick(View v) {
	// Conversation conversation = (Conversation) v.getTag();
	// if (conversation.equals(NebulaApplication.getInstance().getMyIdentity()
	// .getCurrentConversation())) {
	// NebulaApplication.getInstance().getMyIdentity()
	// .setCurrentConversation(null);
	// v.setBackgroundColor(Color.TRANSPARENT);
	// NebulaApplication.getInstance().stopRecording();
	// } else {
	// NebulaApplication.getInstance().getMyIdentity()
	// .setCurrentConversation(conversation);
	// v.setBackgroundColor(Color.rgb(0x35, 0x6A, 0xA0));
	// SIPManager.doCall(conversation.getThread(), conversation);
	// }
	// expListAdapter.notifyDataSetChanged();
	// return true;
	// }
}
