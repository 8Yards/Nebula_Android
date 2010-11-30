/*
 * author: prajwol kumar nakarmi, michel hognerund, nina mulkijanyan
 */
package org.nebula.client.sip;

import java.util.List;

import javax.sip.message.Response;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

import android.util.Log;

public class SIPManager {
	public static final int LOGIN_FAILURE = 0;
	public static final int LOGIN_SUCCESSFUL = 1;
	public static final int SUBSCRIBE_FAILURE = 2;
	public static final int SUBSCRIBE_SUCCESSFUL = 3;
	public static final int PUBLISH_FAILURE = 4;
	public static final int PUBLISH_SUCCESSFUL = 5;
	private static final int LOGOUT_FAILURE = 6;
	private static final int LOGOUT_SUCCESS = 7;
	private static final int CALL_FAILURE = 8;
	private static final int CALL_SUCCESS = 9;

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

	public static int doPublish(String status) {
		try {
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			Response response = sip.send(sip.publish(status, 3600));
			if (response.getStatusCode() == 200) {
				return PUBLISH_SUCCESSFUL;
			} else {
				throw new Exception("Publish didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager:" + e.getMessage());
			return PUBLISH_FAILURE;
		}
	}

	// contact- prajwol, michel
	public static int doCall(List<String> toUsers) {
		try {
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			Log.v("nebula", "sipManager: " + "calling " + toUsers.get(0));
			Response response = sip.send(sip.invite(toUsers));
			if (response.getStatusCode() == 200) {
				return CALL_SUCCESS;
			} else {
				throw new Exception("Call didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager:" + e.getMessage());
			return CALL_FAILURE;
		}
	}

	/*
	 * contact nina
	 */
	public static int doLogout() {
		Log.v("nebula", "sip_manager: logout called");

		try {
			// bye should be sent to all active peers
			SIPClient sip = NebulaApplication.getInstance().getMySIPClient();
			// Response resp ;= sip.send(sip.bye());

			Response resp = sip.send(sip.register(0));
			if (resp.getStatusCode() == Response.OK) {
				return LOGOUT_SUCCESS;
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager: logout error: " + e.getMessage());
			return LOGOUT_FAILURE;
		}

		return LOGOUT_SUCCESS;
	}
	// -- logout_success;
	// -- logout_failure
	// -- doLogout()
	// -- sip.send(sip.bye) - we have only one dialogue
	// -- sip.send(sip.register(0)) - refactor register - parameter = timeout
}
