package android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Conversation extends Activity{
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menucon, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contact: {

			Intent intent = new Intent(Conversation.this, Addcontacts.class);
			startActivity(intent);
		}
			break;

		case R.id.group: {
			Intent intent = new Intent(Conversation.this, Addgroup.class);
			startActivity(intent);
		}
			break;

		case R.id.signout: {
			Intent intent = new Intent(Conversation.this, nebula.class);
			startActivity(intent);
		}
			break;
		}
		return true;
	}
}
