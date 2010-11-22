package android.demo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.demo.R;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class Editcontacts extends Activity {
	 DBClass _DB=new DBClass(this);
	
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.edit_contact);
			
			
			Button add=(Button)findViewById(R.id.add);
			Button back=(Button)findViewById(R.id.back);
			
			Spinner spinner = (Spinner) findViewById(R.id.spinner);
		    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this, R.array.group_prompt, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    spinner.setAdapter(adapter);
		    
		    back.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					finish();
				}
				});
		    
		    
		    add.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View view) {
	               _DB.open();				
	 				
					EditText Edit_user=(EditText)findViewById(R.id.edittext);
					//Spinner Edit_group=(Spinner)findViewById(R.id.spinner);
				
				 //   EditText Edit_conPassword=(EditText)findViewById(R.id.cpwd);
				    
				    String vuser=Edit_user.getText().toString().trim();
				   // String vgroup=Edit_group.getContext().toString();
				   
				  //  String vConpass=Edit_conPassword.getText().toString();	
				    
				    Cursor d= _DB.getTitle1(vuser);
				    try
				    {
				    if (d.moveToFirst())
			        {
				    	String str=d.getString(0);
				    	Toast.makeText(view.getContext(), str +" This Contact already Exits!!!", Toast.LENGTH_LONG).show();
			        }
				    else
				    {
				    	_DB.insertTitle1(vuser,null);
				    	Toast.makeText(view.getContext(), "SAVE SUCCESSFULLY", Toast.LENGTH_LONG).show();
				    	Intent intent = new Intent(Editcontacts.this,Main.class);
				        startActivity(intent);
				    }
				    }
				    catch (Exception e) {
						e.printStackTrace();
					}
				
				 _DB.close();
			       
					
			}
		});
	
	
	}
}
		
	
	
