package android.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.restClient.RESTClient;
import org.nebula.restClient.RESTProfiles;
import org.nebula.restClient.Response;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class Contacts extends ExpandableListActivity{
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contact: {

			Intent intent = new Intent(Contacts.this, Addcontacts.class);
			startActivity(intent);
		}
			break;

		case R.id.group: {
			Intent intent = new Intent(Contacts.this, Addgroup.class);
			startActivity(intent);
		}
			break;
			
		case R.id.edit: {
			Intent intent = new Intent(Contacts.this, Editcontacts.class);
			startActivity(intent);
		}
			break;

		case R.id.signout: {
			Intent intent = new Intent(Contacts.this, nebula.class);
			startActivity(intent);
		}
			break;
		}
		return true;
	}
   
   synchronized public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.explist);
		String name = UserData.getInstance().getUserName() ; 
	     String pass = UserData.getInstance().getUserPassword() ;
	     
	   
		//String un = getIntent().getExtras().getString("userName");
		//String pwd = getIntent().getExtras().getString("passWord");
		
		RESTClient rc = new RESTClient(name, pass);
		RESTProfiles p = new RESTProfiles(rc);
		Response r = p.retrieveAllMyGroups();
		Log.v("nebula", Integer.toString(r.getStatus()));
		Log.v("nebula", r.getResult().toString());

		HashMap<String,ArrayList<HashMap<String,String>>> tempGroupMembers = new HashMap<String,ArrayList<HashMap<String,String>>>();
		ArrayList<HashMap<String,String>> membersList;
		HashMap<String,String> tempHashMap;

		JSONObject json = r.getResult();
		try {
			JSONObject entry;
			int i = 0;
			while((entry = json.getJSONObject(Integer.toString(i++))) != null)
			{
				String userName = (String)entry.get("username");
				String groupName = (String)entry.get("groupName");

				if(tempGroupMembers.containsKey(groupName))
				{
					membersList = tempGroupMembers.get(groupName);
				}
				else
				{
					membersList = new ArrayList<HashMap<String,String>>();
				}

				tempHashMap = new HashMap<String, String>();
				tempHashMap.put("userName", userName);
				membersList.add(tempHashMap);
				tempGroupMembers.put(groupName, membersList);
			}
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<HashMap<String,String>> groups = new ArrayList<HashMap<String,String>>();
		List<ArrayList<HashMap<String,String>>> groupMembers = new ArrayList<ArrayList<HashMap<String,String>>>();

		for(String groupName : tempGroupMembers.keySet())
		{
			tempHashMap = new HashMap<String, String>();
			tempHashMap.put("groupName", groupName);
			groups.add(tempHashMap);

			groupMembers.add(tempGroupMembers.get(groupName));
		}

		SimpleExpandableListAdapter expListAdapter =
			new SimpleExpandableListAdapter(
					this,
					groups,	// groupData describes the first-level entries
					R.layout.child_row,	// Layout for the first-level entries
					new String[] { "groupName" },	// Key in the groupData maps to display
					new int[] { R.id.childname },		// Data under "colorName" key goes into this TextView
					groupMembers,	// childData describes second-level entries
					R.layout.child_row,	// Layout for second-level entries
					new String[] { "userName" },	// Keys in childData maps to display
					new int[] { R.id.childname}	// Data under the keys above go into these TextViews
			);
		setListAdapter( expListAdapter );

	}
	
}




