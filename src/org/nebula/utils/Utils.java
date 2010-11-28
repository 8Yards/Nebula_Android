/*
 * author: michel hognerund, prajwol kumar nakarmi
 */
package org.nebula.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class Utils {
	public static String getLocalIpAddress() throws Exception {
		for (Enumeration<NetworkInterface> en = NetworkInterface
				.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress()
						&& inetAddress instanceof Inet4Address) {
					return inetAddress.getHostAddress().toString();
				}
			}
		}
		throw new Exception("IP could not be found");
	}

	public static int getNextRandomPort() throws IOException {
		ServerSocket ss = new ServerSocket(0);
		int randomPort = ss.getLocalPort();
		ss.close();

		// TODO: may be remove this :)
		randomPort = 9876;

		return randomPort;
	}

	public static String digest(String nonce, String realm, String username,
			String password, String uri, String method) {
		String response;
		String ha1;
		String ha2;

		ha1 = md5(username + ":" + realm + ":" + password);
		ha2 = md5(method + ":" + uri);

		response = md5(ha1 + ":" + nonce + ":" + ha2);
		return response;
	}

	// TODO: throw exception
	public static String md5(String plaintext) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		m.reset();
		m.update(plaintext.getBytes());
		BigInteger bigInt = new BigInteger(1, m.digest());
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while (hashtext.length() < 32)
			hashtext = "0" + hashtext;
		return hashtext;
	}

	public static String convertStreamToString(InputStream inputStream)
			throws IOException {
		if (inputStream != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, "UTF-8"));

			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();

			return sb.toString();
		} else {
			return "";
		}
	}
}
