package org.nebula.client.gps;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.nebula.client.rest.RESTConversationManager;
import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class NebulaLocationListener implements LocationListener {
	private MyIdentity myIdentity;
	private RESTConversationManager rCM = null;

	public NebulaLocationListener(Context ctx) {
		myIdentity = NebulaApplication.getInstance().getMyIdentity();
		rCM = new RESTConversationManager();
		Log.e("nebula", "GPS Handler instantiated");
	}

	public void onLocationChanged(Location loc) {
		if ((loc != null) && (myIdentity != null && myIdentity.getMyUserName() != null
				&& !myIdentity.getMyUserName().trim().equals(""))) {
			
			try {
				 rCM.updatePosition(loc);
			} catch (ClientProtocolException e) {
				Log.e("nebula", "ClientProtocolException on updating position");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("nebula", "IOException on updating position");
				e.printStackTrace();
			} catch (JSONException e) {
				Log.e("nebula", "JSONException on updating position");
				e.printStackTrace();
			}
			Log.e("nebula", "Position updated on the server");
		}
	}

	public void onProviderDisabled(String provider) {
		Log.e("nebula", "Gps Disabled");
	}

	public void onProviderEnabled(String provider) {
		Log.e("nebula", "Gps Enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e("nebula", "State Changed");
	}
}