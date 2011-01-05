/*
 * author: prajwol kumar nakarmi, michel hognerund, nina mulkijanyan
 */
package org.nebula.client.sip;

import static org.nebula.client.sip.NebulaSIPConstants.BYE_SUCCESS;
import static org.nebula.client.sip.NebulaSIPConstants.BYE_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.CALL_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.CALL_SUCCESS;
import static org.nebula.client.sip.NebulaSIPConstants.PUBLISH_SUCCESSFUL;
import static org.nebula.client.sip.NebulaSIPConstants.PUBLISH_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.REFER_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.REFER_SUCCESS;
import static org.nebula.client.sip.NebulaSIPConstants.REGISTER_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.REGISTER_SUCCESSFUL;
import static org.nebula.client.sip.NebulaSIPConstants.SUBSCRIBE_FAILURE;
import static org.nebula.client.sip.NebulaSIPConstants.SUBSCRIBE_SUCCESSFUL;

import org.nebula.main.NebulaApplication;
import org.nebula.models.Conversation;
import org.nebula.models.ConversationThread;
import org.nebula.models.MyIdentity;
import org.nebula.models.Status;
import org.nebula.utils.SIPUtils;

import android.util.Log;

public class SIPManager {

	public static int doLogin(String userName, String password) {
		try {
			SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			MyIdentity myIdentity = NebulaApplication.getInstance()
					.getMyIdentity();
			myIdentity.setMyUserName(userName);
			myIdentity.setMyPassword(password);

			Status response = sip.sendRegister();
			if (response.isSuccess() == true) {
				return REGISTER_SUCCESSFUL;
			} else {
				myIdentity.setMyUserName("");
				myIdentity.setMyPassword("");
				throw new Exception(response.getMessage());
			}
		} catch (Exception e) {
			Log.e("nebula", "sip_manager:" + e.getMessage());
			return REGISTER_FAILURE;
		}
	}

	public static int doSubscribe(String toUserName, String toDomain) {
		try {
			SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			Status response = sip.sendSubscribe(toUserName, toDomain);
			if (response.isSuccess() == true) {
				return SUBSCRIBE_SUCCESSFUL;
			} else {
				throw new Exception("Subscribe didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "sipManager.doSubscribe:" + e.getMessage());
			return SUBSCRIBE_FAILURE;
		}
	}

	public static int doPublish(String status) {
		try {
			SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			Status response = sip.sendPublish(status, 3600);
			if (response.isSuccess() == true) {
				return PUBLISH_SUCCESSFUL;
			} else {
				throw new Exception("Publish didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "sipManager.doPublish:" + e.getMessage());
			return PUBLISH_FAILURE;
		}
	}

	// contact- prajwol, michel
	public static int doCall(ConversationThread thread,
			Conversation conversation) {
		try {
			Log.e("nebula", "SIPManager.doCall:" + conversation.getRcl());
			SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			Status response = sip.sendInvite(thread, conversation);
			if (response.isSuccess() == true) {
				NebulaApplication.getInstance().establishRTP(
						SIPUtils.retrieveIP(response.getMessage()),
						SIPUtils.retrievePort(response.getMessage()));
				return CALL_SUCCESS;
			} else {
				throw new Exception("Call didn't succeed");
			}
		} catch (Exception e) {
			Log.e("nebula", "SIPManager.doCall:" + e.getMessage());
			return CALL_FAILURE;
		}
	}

	/*
	 * contact nina
	 */
	public static int doLogout() {
		 try {
		// doPublish("Offline");
			 SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			 Status response = sip.sendByeToAll();
			 if (!response.isSuccess()) {
				 Log.e("nebula", "SIPManager.doLogout: bye error " + response.getMessage());
			 }
			 sip.sendRegister(0);
			 return BYE_SUCCESS;
		 }
		 catch(Exception e) {
			 Log.e("nebula", "sipManager.doLogout: " + e.getMessage()) ;
			 return BYE_FAILURE;
		 }
	}

	public static int doRefer(String referSIPUser, String referSIPDomain,
			String threadId, Conversation oldConversation,
			Conversation newConversation) {
		try {
			SIPHandler sip = NebulaApplication.getInstance().getMySIPHandler();
			Status resp = sip.sendRefer(referSIPUser, referSIPDomain, threadId,
					oldConversation, newConversation);
			if (resp.isSuccess() == true) {
				return REFER_SUCCESS;
			} else {
				throw new Exception("REFER did not succeed. "
						+ resp.getMessage());
			}
		} catch (Exception e) {
			Log.e("nebula", "SIPMmanager.doRefer: " + e.getMessage());
			return REFER_FAILURE;
		}
	}
}
