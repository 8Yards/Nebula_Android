/*
 * author: prajwol kumar nakarmi, michel hognerund
 */
package org.nebula.client.sip;

import javax.sip.message.Response;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;
import org.nebula.services.SIPClient;

import android.util.Log;

public class SIPManager {
	public static final int LOGIN_SUCCESSFUL = 1;
	public static final int LOGIN_FAILURE = 0;

	public static int doLogin(String userName, String password) {
		try {
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			MyIdentity myIdentity = NebulaApplication.getInstance().getMyIdentity();
			myIdentity.setMySIPName(userName);
			myIdentity.setMyPassword(password);
			
			Response response = sip.send(sip.register());
			if (response.getStatusCode() == 200) {
				return LOGIN_SUCCESSFUL;
			} else {
				myIdentity.setMySIPName("");
				myIdentity.setMyPassword("");
				throw new Exception("Invalid credentials");
			}
		} catch (Exception e) {
			Log.e("nebula", e.getMessage());
			return LOGIN_FAILURE;
		}
	}

}
