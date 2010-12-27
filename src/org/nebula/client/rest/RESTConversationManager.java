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

public class RESTConversationManager extends Resource {

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

}