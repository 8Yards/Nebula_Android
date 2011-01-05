/*
 * author: marco
 * debugging, refactoring:  marco
 */
package org.nebula.client.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;

import android.location.Location;
import android.util.Log;

public class RESTConversationManager extends Resource {

	private double[] distances ={0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10,20, 50, 100, 250, 500 };
	
	public double getDistancesValue(int index){
		if(index>= distances.length)
			return -1;
		return distances[index];
	}
	
	public RESTConversationManager() {
		super("RESTConversations");
	}

	public Status addNewConversation(Conversation conversation)
			throws ClientProtocolException, IOException, JSONException,
			ParseException {
		// group insertion
		HashMap<String, Object> hM = new HashMap<String, Object>();
		hM.put("thread", conversation.getThread().getThreadName());
		hM.put("conversation", conversation.getConversationName());

		if (conversation.getCallee() != null) {
			hM.put("calleeNumber", conversation.getCallee().size());
			for (int i = 0; i < conversation.getCallee().size(); i++) {
				hM.put("callee" + (i + 1) + "username", conversation
						.getCallee().get(i));
			}
		}

		Response r = this.post("insert", hM);
		// 201 = HTTP status code for conversation inserted
		if (r.getStatus() == 201) {
			conversation.setId(Integer.parseInt(r.getResult().getString("id")));
			conversation.setDate(r.getResult().getString("date"));
			return new Status(true, "Conversation added successfully");
		} else {
			return new Status(false, "" + r.getResult());
		}
	}

	public int returnIndex(int value){
		if(value==0)
			return 0;
		if(value==100)
			return distances.length-1;
		float percentage = 100/(distances.length-1);
		int index = (int) Math.ceil(value / percentage)-1;
		
		if (index >= distances.length)
			return distances.length-1;
		return index;
	}
	
	public List<ConversationThread> retrieveAll() throws JSONException,
			ClientProtocolException, IOException, ParseException {
		Response r = this.get("retrieveAll");
		List<ConversationThread> myThreads = new ArrayList<ConversationThread>();
		if (r.getStatus() == 200) {
			for (Iterator iterator = r.getResult().keys(); iterator.hasNext();) {
				String key = "" + iterator.next();
				JSONObject keyObj = r.getResult().getJSONObject(key);
				myThreads.add(new ConversationThread(keyObj, key));
			}
			return myThreads;
		}else if(r.getStatus()==201){
			return myThreads;
		}
		return null;
	}

	public Status updateTime(Conversation conversation)
			throws ClientProtocolException, IOException, JSONException,
			ParseException {
		Map<String, Object> h = new HashMap<String, Object>();
		h.put("id", conversation.getId());

		Response r = this.put("updateTime", h);
		if (r.getStatus() == 201) {
			conversation.setDate(r.getResult().getString("date"));
			return new Status(true, "Time updated successfully");
		} else {
			return new Status(false, r.getResult().getString("result"));
		}
	}
	
	public Status createSpatialConversation(Conversation conversation, double distance) throws ClientProtocolException,
	IOException, JSONException, ParseException {
		// group insertion
		HashMap<String, Object> hM = new HashMap<String, Object>();
		hM.put("thread", conversation.getThread().getThreadName());
		hM.put("conversation", conversation.getConversationName());
		hM.put("distance", distance);
		Log.e("CREATION","Thread: "+conversation.getThread() + "conversation: " + conversation.getConversationName() + "distance: "+ distance);
		Response r = this.post("createSpatialConversation", hM);
		// 201 = HTTP status code for conversation inserted
		
		if (r.getStatus() == 201) {
			conversation.setId(Integer.parseInt(r.getResult().getString("id")));
			conversation.setDate(r.getResult().getString("date"));
			JSONObject callee = r.getResult().getJSONObject("callees");
		
			List<String> callees = new ArrayList<String>();
			for (int i = 0; i < callee.length(); i++) {
				callees.add(callee.getString("" + i));
			}
			conversation.setCallee(callees);
			return new Status(true, "Conversation added successfully");
		} else {
			return new Status(false, "" + r.getResult());
		}
	}
	
	public int getDistancesIndex(double value){
		for (int i=0; i<distances.length; i++)
			if(distances[i]==value)
				return i;
		return -1;
	}
	
	public float[][] distanceFromContact() throws JSONException,
	ClientProtocolException, IOException {
		Response r = this.get("distanceFromContact");
		Log.e("GPS", "Message: "+r.getResult());
		Log.e("GPS", "Status: "+r.getStatus());
		if(r.getStatus()!=200)
			return null;
		float[][] values = new float[distances.length][2];
		// scan the data for the name of the groups
		int i = 0;

		for(int h = 0; h<distances.length;h++){
			values[h][0] = (float)distances[h];
			values[h][1] = 0;
		}
		//empty
		if(r.getStatus()==201)
			return values;
		if(r.getStatus()!=200)
			return null;
		for (Iterator iterator = r.getResult().keys(); iterator.hasNext();) {
			String distance = "" + iterator.next();
			// retrieve the JSON object and instantiates a new Group with it
			String number = r.getResult().getString(distance);
			i = getDistancesIndex(Double.parseDouble(distance));
			if (i==-1)
				Log.e("INDEX", "-1   Value searched:  " +Double.parseDouble(distance));
			if(i<=distances[distances.length-1]){
				values[i][0] = (float)distances[i];
				values[i][1] = (float)Integer.parseInt(number);
			}
			
		}
		for (int j=1;j<distances.length;j++){
			values[j][1] = values[j][1] + values[j-1][1];
		}
		return values;
	}
	
	public Status updatePosition(Location loc) throws ClientProtocolException, IOException, JSONException{
		HashMap<String, Object> hM = new HashMap<String, Object>();
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		hM.put("latitude", latitude);
		hM.put("longitude", longitude);
		

		Response r = this.put("updatePosition", hM);
		// 201 = HTTP status code for position updated
		
		if (r.getStatus() == 201) {
			return new Status(true, "Position updated successfully");
		} else {
			return new Status(false, "" + r.getResult());
		}
	}
	

}