//skeleton code taken from http://www.firstdroid.com/2010/04/29/android-development-using-gps-to-get-current-location-2/

package org.nebula.activities;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.MyIdentity;

import android.app.Activity;
import android.os.Bundle;



import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
public class AddGeoGroup extends Activity implements OnSeekBarChangeListener{
	/** Called when the activity is first created. */
//	LocationManager mlocManager = null;
//
//	LocationListener mlocListener = null;
	private Context ctx = null;
	MyIdentity myIdentity;
	SeekBar seekBar;
	RESTConversationManager rC;
	//private TextView seekBarValue;
	float[][] usersPosition;

	private TextView seekUsersNumber;

	private TextView seekDistance;

	private TextView seekBarValue;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		myIdentity = NebulaApplication.getInstance().getMyIdentity();

		ctx = getApplication();
		rC = new RESTConversationManager();
		try {
			Log.e("INDEX","before calling");
			usersPosition= rC.distanceFromContact();
			Log.e("INDEX","after calling");

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("marco","creation");
		super.onCreate(savedInstanceState);
		//	Log.e("marco","created");
		setContentView(R.layout.spatial_group);
		//		Log.e("marco","view");
		/* Use the LocationManager class to obtain GPS locations */
//		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		//		Log.e("marco","loc manager");
//		LocationListener mlocListener = new MyLocationListener();
//		//		Log.e("marco","loc listener");
//		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		//		Log.e("marco","loc updater");

		/* Class My Location Listener */


		seekBar = (SeekBar) findViewById(R.id.sbVolume);
		seekBarValue = (TextView) findViewById(R.id.tvVolumeValue);
		seekDistance = (EditText) findViewById(R.id.etDistance);
		seekUsersNumber = (EditText) findViewById(R.id.etUsers);
		//int getVol = DatabaseManager.getVolume(myIdentity.getMyUserName(), this);
		seekBar.setProgress(1);
		//seekBarValue.setText(1);


		seekBar.setOnSeekBarChangeListener(this);
	}

	public void doBackToMain(View v) {

		finish();
	}

	public void addSpacialConversation(View v){
		ConversationThread convThread = myIdentity.createThread();
		double distance = usersPosition[rC.returnIndex(seekBar.getProgress())][0];
		Conversation conv = new Conversation(myIdentity.createNewConversationName());
		convThread.addConversation(conv);
		Log.e("nebula", "GEO Convesation Created");
		try {
			rC.createSpatialConversation(conv, distance);
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("nebula", "JSON GEO parsing done");

		finish();
	}


	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		seekBarValue.setText("");
		if(usersPosition!=null){
			seekDistance.setText(""+usersPosition[rC.returnIndex(seekBar.getProgress())][0]+ "   Km");
			seekUsersNumber.setText(""+(int)usersPosition[rC.returnIndex(seekBar.getProgress())][1]+ "   Users");
		}
		else{
			seekDistance.setText(""+rC.getDistancesValue(rC.returnIndex(seekBar.getProgress()))+ "   Km");
			seekUsersNumber.setText("0   Users");
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	} 
	
	public void onStopTrackingTouch(SeekBar arg0) {
	}
	
	
}