package org.nebula.client.sip;

public class NebulaSIPConstants {
	public static final String NOTIFY_PRESENCE = "NOTIFY_PRESENCE_EVENT";
	public static final String NOTIFY_INVITE = "NOTIFY_INVITE_EVENT";
	public static final String NOTIFY_BYE = "NOTIFY_BYE_EVENT";
	public static final String SUBSCRIBE_NOTIFY_CALLID = "nebula_subscribe_notify";

	public static final String PIDF_NS_VALUE = "urn:ietf:params:xml:ns:pidf";
	public static final String PRESENCE_ELEMENT = "presence";
	public static final String TUPLE_ELEMENT = "tuple";
	public static final String NOTE_ELEMENT = "note";
	public static final String ENTITY_ATTRIBUTE = "entity";

	public static final String PRESENCE_ONLINE = "Online";
	public static final String PRESENCE_BUSY = "Busy";
	public static final String PRESENCE_AWAY = "Away";
	public static final String PRESENCE_OFFLINE = "Offline";

	public static final String THREAD_PARAMETER = "thread";
	public static final String CONVERSATION_PARAMETER = "conv";
	public static final String OLD_CONVERSATION_PARAMETER = "newvalue";

	public static final int REGISTER_FAILURE = 0;
	public static final int REGISTER_SUCCESSFUL = 1;
	public static final int REGISTER_SIPEXCEPTION = 12;
	public static final int SUBSCRIBE_FAILURE = 2;
	public static final int SUBSCRIBE_SUCCESSFUL = 3;
	public static final int PUBLISH_FAILURE = 4;
	public static final int PUBLISH_SUCCESSFUL = 5;
	public static final int BYE_FAILURE = 6;
	public static final int BYE_SUCCESS = 7;
	public static final int CALL_FAILURE = 8;
	public static final int CALL_SUCCESS = 9;
	public static final int REFER_FAILURE = 10;
	public static final int REFER_SUCCESS = 11;
}
