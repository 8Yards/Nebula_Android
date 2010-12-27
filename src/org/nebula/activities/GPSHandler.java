//skeleton code taken from http://www.firstdroid.com/2010/04/29/android-development-using-gps-to-get-current-location-2/

package org.nebula.activities;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.R;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.app.Activity;
import android.os.Bundle;



import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
public class GPSHandler extends Activity implements OnSeekBarChangeListener{
	/** Called when the activity is first created. */
	LocationManager mlocManager = null;
	
	LocationListener mlocListener = null;
	private Context ctx = null;
	MyIdentity myIdentity;
	SeekBar seekBar;
	RESTConversationManager rC;
	//private TextView seekBarValue;
	float[][] usersPosition;

	private TextView seekUsersNumber;

	private TextView seekDistance;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		ctx = getApplication();
		rC = new RESTConversationManager();
		try {
			usersPosition= rC.distanceFromContact();
			Log.e("GPS", ""+usersPosition[0][0]);
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
		Log.e("marco","view");
		/* Use the LocationManager class to obtain GPS locations */
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Log.e("marco","loc manager");
		LocationListener mlocListener = new MyLocationListener();
		Log.e("marco","loc listener");
		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		Log.e("marco","loc updater");
	
	/* Class My Location Listener */
	

	seekBar = (SeekBar) findViewById(R.id.sbVolume);
	//seekBarValue = (TextView) findViewById(R.id.tvVolumeValue);
	seekDistance = (EditText) findViewById(R.id.etDistance);
	seekUsersNumber = (EditText) findViewById(R.id.etUsers);
	//int getVol = DatabaseManager.getVolume(myIdentity.getMyUserName(), this);
	seekBar.setProgress(1);
	//seekBarValue.setText(1);


	seekBar.setOnSeekBarChangeListener(this);
}
		

public void onProgressChanged(SeekBar seekBar, int progress,
	boolean fromUser) {
	Log.e("GPS", ""+seekBar.getProgress());
	Log.e("GPS", ""+rC.returnIndex(seekBar.getProgress()));
	Log.e("GPS", ""+usersPosition[rC.returnIndex(seekBar.getProgress())][0]);
	seekDistance.setText(""+usersPosition[rC.returnIndex(seekBar.getProgress())][0]);
	seekUsersNumber.setText(""+usersPosition[rC.returnIndex(seekBar.getProgress())][1]);
}

public void onStartTrackingTouch(SeekBar seekBar) {

}

//public void onStopTrackingTouch(SeekBar seekBar) {
//	LocalDatabaseManager.doStoreVolume(myIdentity.getMyUserName(),
//			String.valueOf(valueVolume), getApplicationContext());
//	int volValue = LocalDatabaseManager.getVolume(
//					myIdentity.getMyUserName(), getApplicationContext());
//	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//					volValue, AudioManager.FLAG_PLAY_SOUND);
//}

	public class MyLocationListener implements LocationListener
	{
		public void onLocationChanged(Location loc){
			if(loc!=null){
				loc.getLatitude();
				loc.getLongitude();
				String Text = "My current location is: " +
				"Latitud = " + loc.getLatitude() +
				"Longitud = " + loc.getLongitude();
				Toast.makeText( ctx,
						Text,
						Toast.LENGTH_SHORT).show();
			}
		}
		public void onProviderDisabled(String provider){
			Toast.makeText( ctx,
					"Gps Disabled",
					Toast.LENGTH_SHORT ).show();
		}
		public void onProviderEnabled(String provider){
			Toast.makeText( ctx,
					"Gps Enabled",
					Toast.LENGTH_SHORT).show();
		}
		public void onStatusChanged(String provider, int status, Bundle extras){
			Toast.makeText( ctx,
					"State Changed",
					Toast.LENGTH_SHORT).show();
		}
	}/* End of Class MyLocationListener */
/* End of UseGps Activity */
	@Override
	protected void onResume() {
	    // TODO Auto-generated method stub
		if((mlocManager!=null) && (mlocListener!=null))
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f,mlocListener);
	    super.onResume();
	}


public void onStopTrackingTouch(SeekBar arg0) {
	// TODO Auto-generated method stub
	
}

}