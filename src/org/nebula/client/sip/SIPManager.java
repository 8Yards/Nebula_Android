/*
 * author: prajwol kumar nakarmi, michel hognerund, nina mulkijanyan
 */
package org.nebula.client.sip;

import javax.sip.message.Response;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.util.Log;

public class SIPManager {
	public static final int LOGIN_FAILURE = 0;
	public static final int LOGIN_SUCCESSFUL = 1;
	private static final int SUBSCRIBE_FAILURE = 2;
	private static final int SUBSCRIBE_SUCCESSFUL = 3;

	public static int doLogin(String userName, String password) {
		try {
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			MyIdentity myIdentity = NebulaApplication.getInstance()
					.getMyIdentity();
			myIdentity.setMyUserName(userName);
			myIdentity.setMyPassword(password);

			Response response = sip.send(sip.register());
			if (response.getStatusCode() == 200) {
				return LOGIN_SUCCESSFUL;
			} else {
				myIdentity.setMyUserName("");
				myIdentity.setMyPassword("");
				throw new Exception("Invalid credentials");
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager:" + e.getMessage());
			return LOGIN_FAILURE;
		}
	}

	public static int doSubscribe(String toUserName, String toDomain) {
		try {
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			Response response = sip.send(sip.subscribe(toUserName, toDomain));
			if (response.getStatusCode() == 200) {
				return SUBSCRIBE_SUCCESSFUL;
			} else {
				throw new Exception("Subscribe didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager:" + e.getMessage());
			return SUBSCRIBE_FAILURE;
		}
	}

}
