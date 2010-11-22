package android.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.restClient.RESTClient;
import org.nebula.restClient.RESTGroups;
import org.nebula.restClient.Response;
import org.nebula.userData.Groups;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class Contacts extends ExpandableListActivity{
	SimpleExpandableListAdapter expListAdapter;
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
			//Intent intent = new Intent(Contacts.this, nebula.class);
			finish();
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
	   	 RESTGroups rG = new RESTGroups(rc);
	   	 
	   		 Response r=null;
			try {
				r = rG.retrieveGroups();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   	 
	   	 Log.e("nebula",Groups.get(1).getGroupName());
	   
	   	
		HashMap<String,ArrayList<HashMap<String,String>>> tempGroupMembers = new HashMap<String,ArrayList<HashMap<String,String>>>();
		ArrayList<HashMap<String,String>> membersList;
		HashMap<String,String> tempHashMap;

		JSONObject json = r.getResult();
		try {
			JSONObject entry;
			int i = 0;
			while((entry = json.getJSONObject(Integer.toString(i++))) != null)
			{
				String ownerUserID = (String)entry.get("ownerUserID");
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
				tempHashMap.put("aa", ownerUserID);
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

		expListAdapter =
			new SimpleExpandableListAdapter(
					this,
					groups,	// groupData describes the first-level entries
					R.layout.group_row,	// Layout for the first-level entries
					new String[] { "groupName" },	// Key in the groupData maps to display
					new int[] { R.id.groupname },		// Data under "colorName" key goes into this TextView
					groupMembers,	// childData describes second-level entries
					R.layout.child_row,	// Layout for second-level entries
					new String[] { "aa" },	// Keys in childData maps to display
					new int[] { R.id.childname}	// Data under the keys above go into these TextViews
			);
		setListAdapter( expListAdapter );

	}
   public void  onContentChanged  () {
       super.onContentChanged();
      // Log.d( LOG_TAG, "onContentChanged" );
   }

   public boolean onChildClick(
           ExpandableListView parent, 
           View v, 
           int groupPosition,
           int childPosition,
           long id) {
     //  Log.d( LOG_TAG, "onChildClick: "+childPosition );
       CheckBox cb = (CheckBox)v.findViewById( R.id.check1 );
       if( cb != null )
           cb.toggle();
       return false;
   }

   public void  onGroupExpand  (int groupPosition) {
      // Log.d( LOG_TAG,"onGroupExpand: "+groupPosition );
   }
   public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	    AlertDialog.Builder adb=new AlertDialog.Builder(Contacts.this);
	   
	    adb.setTitle("LVSelectedItemExample");
	   
	    adb.setMessage("Selected Item is = "+expListAdapter.getItemAtPosition(position));
	   
	    adb.setPositiveButton("Ok", null);
	   
	    adb.show();
	   	    }
	   
	    });
	   
	    }

}




