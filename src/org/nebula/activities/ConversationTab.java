/*
 * author - saad
 * refactored - prajwol
 */
package org.nebula.activities;

import java.util.ArrayList;
import java.util.List;

import org.nebula.R;
import org.nebula.client.sip.SIPManager;
import org.nebula.models.MyIdentity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ConversationTab extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conversation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v("nebula", "conversationTab: " + "calling nina");
		List<String> callee = new ArrayList<String>();
		callee.add("nina");

		SIPManager.doCall(callee);
		
//		SIPManager.doRefer("user", "192.16.124.211");
		return true;
	}
}
