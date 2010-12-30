/*
 * author - michel, prajwol
 */
package org.nebula.utils;

import static org.nebula.client.sip.NebulaSIPConstants.ENTITY_ATTRIBUTE;
import static org.nebula.client.sip.NebulaSIPConstants.NOTE_ELEMENT;
import static org.nebula.client.sip.NebulaSIPConstants.PIDF_NS_VALUE;
import static org.nebula.client.sip.NebulaSIPConstants.PRESENCE_ELEMENT;
import static org.nebula.client.sip.NebulaSIPConstants.TUPLE_ELEMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.nebula.main.NebulaApplication;
import org.nebula.models.MyIdentity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SIPUtils {

	public static String getSDP(String content) throws Exception {
		Pattern p1 = Pattern.compile(
				"Content-type: application/sdp\r\n(.*?)--8Yards",
				Pattern.MULTILINE | Pattern.DOTALL);

		// Pattern p1 = Pattern.compile("\r\n\r\n(.*)", Pattern.MULTILINE
		// | Pattern.DOTALL);

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
						"Content-type: application/resource-lists\\+xml\r\n(.*?)--8Yards",
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
				+ " RTP/AVP 8\r\n" + "a=rtpmap:8 PCMA/8000/1" + "\r\n";
	}

	public static String retrieveIP(String sdp) throws Exception {
		Pattern p1 = Pattern.compile("c=[^ ]+ [^ ]+ (.*)");
		Matcher m1 = p1.matcher(sdp);
		if (m1.find()) {
			return m1.group(1).trim();
		} else {
			throw new Exception("No IP found");
		}
	}

	public static int retrievePort(String sdp) throws Exception {
		Pattern p2 = Pattern.compile("m=audio ([0-9]+) ");
		Matcher m2 = p2.matcher(sdp);
		if (m2.find()) {
			return Integer.parseInt(m2.group(1));
		} else {
			throw new Exception("No Port found");
		}
	}

	public static String[] parsePresenceXML(String inXML)
			throws ParserConfigurationException, FactoryConfigurationError,
			SAXException, IOException {

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		// TODO:: purpose of each instance ---- assinged to NINA
		Document doc = builder.parse(new InputSource(new StringReader(inXML)));

		// get presence element
		NodeList presList = doc.getElementsByTagNameNS(PIDF_NS_VALUE,
				PRESENCE_ELEMENT);

		if (presList.getLength() == 0) {
			presList = doc.getElementsByTagName(PRESENCE_ELEMENT);
			if (presList.getLength() == 0) {
				throw new SAXException("presList length is zero");
			}
		}

		// we only use the first presence list =)
		Node presNode = presList.item(0);
		Element presence = (Element) presNode;
		NodeList tupleList = presence.getElementsByTagName(TUPLE_ELEMENT);

		for (int i = 0; i < tupleList.getLength(); i++) {
			Node tupleNode = tupleList.item(i);

			if (tupleNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element tuple = (Element) tupleNode;
			NodeList noteList = tuple.getElementsByTagName(NOTE_ELEMENT);

			if (noteList.getLength() > 0) {
				return new String[] { presence.getAttribute(ENTITY_ATTRIBUTE),
						noteList.item(0).getFirstChild().getNodeValue() };
			}
		}

		return null;
	}
}
