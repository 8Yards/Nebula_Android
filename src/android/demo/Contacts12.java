package android.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleExpandableListAdapter;

public class Contacts12 extends ExpandableListActivity{
	 
	 
		    static final String colors[] = {
			  "8yards",
			  "Salcas",
			  "Other Contacts"
			 
			};

			static final String shades[][] = {
		// Shades of grey
			  {
				"Saad", 
				"Venky",
				"Sharique",
				"Prajwol"
			  },
		// Shades of blue
			  {
				"Lakshmi",
				"Arun",
				"Amir"
			  },
		// Shades of yellow
			  {
				"Niklas",
				"Hans"
				
			  }
		    };

		    /** Called when the activity is first created. */
		    @Override
		    public void onCreate(Bundle icicle)
		    {
		        super.onCreate(icicle);
		        setContentView(R.layout.explist);
				SimpleExpandableListAdapter expListAdapter =
					new SimpleExpandableListAdapter(
						this,
						createGroupList(),	// groupData describes the first-level entries
						R.layout.child_row,	// Layout for the first-level entries
						new String[] { "colorName" },	// Key in the groupData maps to display
						new int[] { R.id.childname },		// Data under "colorName" key goes into this TextView
						createChildList(),	// childData describes second-level entries
						R.layout.child_row,	// Layout for second-level entries
						new String[] { "shadeName" },	// Keys in childData maps to display
						new int[] { R.id.childname}	// Data under the keys above go into these TextViews
					);
				setListAdapter( expListAdapter );
		    }

		/**
		  * Creates the group list out of the colors[] array according to
		  * the structure required by SimpleExpandableListAdapter. The resulting
		  * List contains Maps. Each Map contains one entry with key "colorName" and
		  * value of an entry in the colors[] array.
		  */
			private List createGroupList() {
			  ArrayList result = new ArrayList();
			  for( int i = 0 ; i < colors.length ; ++i ) {
				HashMap m = new HashMap();
			    m.put( "colorName",colors[i] );
				result.add( m );
			  }
			  return (List)result;
		    }

		/**
		  * Creates the child list out of the shades[] array according to the
		  * structure required by SimpleExpandableListAdapter. The resulting List
		  * contains one list for each group. Each such second-level group contains
		  * Maps. Each such Map contains two keys: "shadeName" is the name of the
		  * shade and "rgb" is the RGB value for the shade.
		  */
		  private List createChildList() {
			ArrayList result = new ArrayList();
			for( int i = 0 ; i < shades.length ; ++i ) {
		// Second-level lists
			  ArrayList secList = new ArrayList();
			  for( int n = 0 ; n < shades[i].length ; n += 2 ) {
			    HashMap child = new HashMap();
				child.put( "shadeName", shades[i][n] );
			    
				secList.add( child );
			  }
			  result.add( secList );
			}
			return result;
		  }
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

					Intent intent = new Intent(Contacts12.this, Addcontacts.class);
					startActivity(intent);
				}
					break;

				case R.id.group: {
					Intent intent = new Intent(Contacts12.this, Addgroup.class);
					startActivity(intent);
				}
					break;
					
				case R.id.edit: {
					Intent intent = new Intent(Contacts12.this, Editcontacts.class);
					startActivity(intent);
				}
					break;

				case R.id.signout: {
					Intent intent = new Intent(Contacts12.this, nebula.class);
					startActivity(intent);
				}
					break;
				}
				return true;
			}

		 
}
