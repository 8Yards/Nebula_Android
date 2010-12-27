//skeleton code taken from http://www.firstdroid.com/2010/04/29/android-development-using-gps-to-get-current-location-2/

package org.nebula.activities;

import android.app.Activity;
import android.os.Bundle;



import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
public class GPSHandler extends Activity{
	/** Called when the activity is first created. */
	LocationManager mlocManager = null;
	
	LocationListener mlocListener = null;
	private Context ctx = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ctx = getApplication();

		Log.e("marco","creation");
		super.onCreate(savedInstanceState);
		Log.e("marco","created");
		setContentView(R.layout.main);
		Log.e("marco","view");
		/* Use the LocationManager class to obtain GPS locations */
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Log.e("marco","loc manager");
		LocationListener mlocListener = new MyLocationListener();
		Log.e("marco","loc listener");
		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		Log.e("marco","loc updater");
	}
	/* Class My Location Listener */
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

}