package org.nebula.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;

public class SDPUtils {

	public static String getSDP(String content) throws Exception {
		Pattern p1 = Pattern.compile(
				"Content-type: application/sdp\r\n(.*?)--8Yards",
				Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m1 = p1.matcher(content);
		if (m1.find()) {
			return m1.group(1).trim();
		} else {
			throw new Exception("No SDP found");
		}
	}

	public static String getRCL(String content) throws Exception {
		Pattern p1 = Pattern
				.compile(
						"Content-Type: application/resource-lists\\+xml\r\n(.*?)--8Yards",
						Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m1 = p1.matcher(content);
		if (m1.find()) {
			return m1.group(1).trim();
		} else {
			throw new Exception("No RCL found");
		}
	}

	public static String getMySDP() {
		MyIdentity myIdentity = NebulaApplication.getInstance().getMyIdentity();
		return "v=0\r\n" + "o=" + myIdentity.getMyUserName()
				+ " 122456 654221 IN IP4 " + myIdentity.getMyIP() + "\r\n"
				+ "s=A conversation\r\n" + "c=IN IP4 " + myIdentity.getMyIP()
				+ "\r\n" + "t=0 0\r\n" + "m=audio " + myIdentity.getMyRTPPort()
				+ " RTP/AVP 8\r\n" + "a=rtpmap:8 PCMA/16000/1" + "\r\n";
	}

	public static String retrieveIP(String sdp) throws Exception {
		Pattern p1 = Pattern.compile("c=[^ ]+ [^ ]+ (.*)");
		Matcher m1 = p1.matcher(sdp);
		if (m1.find())
			return m1.group(1);
		else
			throw new Exception("No IP found");
	}

	public static int retrievePort(String sdp) throws Exception {
		Pattern p2 = Pattern.compile("m=audio ([0-9]+) ");
		Matcher m2 = p2.matcher(sdp);
		if (m2.find())
			return Integer.parseInt(m2.group(1));
		else
			throw new Exception("No IP found");
	}
}
