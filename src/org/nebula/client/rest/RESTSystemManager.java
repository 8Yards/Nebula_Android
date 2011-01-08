/*
 * author: marco
 * debugging, refactoring: prajwol, marco
 */
package org.nebula.client.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

public class RESTSystemManager extends Resource {

	public RESTSystemManager() {
		super("RESTSystem");
	}

	public String reflectorName() throws JSONException,
			ClientProtocolException, IOException {
		return this.get("reflectorName").getResult().getString("result");
	}
}