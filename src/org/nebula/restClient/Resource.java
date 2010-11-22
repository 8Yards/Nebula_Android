package org.nebula.restClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

//import android.util.Base64;
import android.util.Log;

/*
 * Class used by Profiles, Contacts, Groups classes
 * It gives all the common methods to send requests and receive responses
 * @author	Nebula
 */
public abstract class Resource {
	protected String url;
	protected HashMap<String, Object> data = new HashMap<String, Object>();
	private RESTClient rc;

	/*
	 * Constructor
	 */
	public Resource() {
		this((RESTClient)null);
	}

	public Resource(RESTClient rc) {
		this.setRc(rc);
		String thisClass = this.getClass().getSimpleName();
		this.url = this.getRc().getServerIP()+"/"+thisClass.toLowerCase()+"/";
	}

	/*
	 * Creates a new element containing data
	 * @param	data	element parameters
	 */
	public Resource(HashMap<String, Object> data) {
		this();
		this.data = data;
	}

	public Resource(RESTClient rc, HashMap<String, Object> data) {
		this((RESTClient)null);
		this.data = data;
	}

	/*
	 * adds a parameter to the element
	 * @param	key		parameter's key
	 * @param	test	parameter's value
	 */
	public void addParam(String key, Object test) {
		this.data.put(key, test);
	}

	/*
	 * sends a GET request with no parameters/no method
	 * @return	Response	response from the server
	 */
	public Response get() {
		return get("", new HashMap<String, String>());
	}

	/*
	 * sends a GET request with a method
	 * @param	method		the name of the called method
	 * @return	Response	response from the server
	 */
	protected Response get(String method) {
		return get(method, new HashMap<String, String>());
	}

	/*
	 * sends a GET request with no method/with parameters
	 * @param	params		the get parameters
	 * @return	Response	response from the server
	 */
	protected Response get(HashMap<String, String> params) {
		return get("", params);
	}

	/*
	 * sends a GET request with a method and parameters
	 * @param	method		the name of the called method
	 * @param	params		parameters of the GET request
	 * @return	Response	response from the server
	 */
	protected Response get(String method, HashMap<String, String> params) {
		String requestURL = this.url;

		if(this.data.containsKey("id"))
			requestURL = requestURL + this.data.get("id") + "/";

		String optionsStr = "";
		Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			if(!((String)pairs.getKey()).toLowerCase().equals("id"))
				optionsStr = optionsStr + pairs.getKey() + "=" + pairs.getValue() + "&";
		}

		requestURL = requestURL + method;

		if(!optionsStr.equals(""))
			try {
				requestURL = requestURL + "?" + new UrlEncodedFormEntity(convertToList(params));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.exit(-1);
			}

		HttpGet httpget = new HttpGet(requestURL);
		return send_and_receive(httpget);
	}

	/*
	 * sends a POST request with no method/data
	 * @return	Response	response from the server
	 */
	protected Response post() {
		return post("", new HashMap<String, Object>());
	}

	/*
	 * sends a POST request with a method/no data
	 * @param	method		the name of the method being called
	 * @return	Response	response from the server
	 */
	protected Response post(String method) {
		return post(method, new HashMap<String, Object>());
	}

	/*
	 * sends a POST request with no method/with data
	 * @param	options		options sent within the body of the request
	 * @return	Response	response from the server
	 */
	protected Response post(HashMap<String, Object> options) {
		return post("", options);
	}

	/*
	 * sends a POST request with method/and data
	 * @param	method		the name of the method being called
	 * @param	options		options sent within the body of the request
	 * @return	Response	response from the server
	 */
	protected Response post(String method, HashMap<String, Object> options) {
		String requestURL = this.url;

		if(this.data.containsKey("id"))
			requestURL = requestURL + this.data.get("id") + "/";

		requestURL += method;

		HttpPost httppost = new HttpPost(requestURL);
		try {
			JSONObject JSON = new JSONObject(options);
			StringEntity se = new StringEntity(JSON.toString(), HTTP.UTF_8);
			se.setContentType("application/json");
			httppost.setHeader("Content-Type", "application/json;charset=UTF-8");

			httppost.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return send_and_receive(httppost);
	}

	/*
	 * sends a PUT request with no method/data
	 * @return	Response	response from the server
	 */
	protected Response put() {
		return put("", new HashMap<String, Object>());
	}

	/*
	 * sends a PUT request with a method/no data
	 * @param	method		the name of the method being called
	 * @return	Response	response from the server
	 */
	protected Response put(String method) {
		return put(method, new HashMap<String, Object>());
	}

	/*
	 * sends a PUT request with no method/with data
	 * @param	options		options sent within the body of the request
	 * @return	Response	response from the server
	 */
	protected Response put(HashMap<String, Object> options) {
		return put("", options);
	}

	/*
	 * sends a PUT request with method/and data
	 * @param	method		the name of the method being called
	 * @param	options		options sent within the body of the request
	 * @return	Response	response from the server
	 */
	protected Response put(String method, HashMap<String, Object> options) {
		String requestURL = this.url;

		if(this.data.containsKey("id"))
			requestURL = requestURL + this.data.get("id") + "/";

		requestURL += method;

		HttpPut httpput = new HttpPut(requestURL);
		try {
			JSONObject JSON = new JSONObject(options);
			StringEntity se = new StringEntity(JSON.toString(), HTTP.UTF_8);
			se.setContentType("application/json");
			httpput.setHeader("Content-Type", "application/json;charset=UTF-8");
			httpput.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return send_and_receive(httpput);
	}

	/*
	 * sends a DELETE request
	 * @return	Response	response from the server
	 */
	protected Response delete() {
		String requestURL = this.url;

		if(this.data.containsKey("id"))
			requestURL = requestURL + this.data.get("id") + "/";

		HttpDelete httpdelete = new HttpDelete(requestURL);
		return send_and_receive(httpdelete);
	}

	/*
	 * sends a DELETE request
	 * @param	id			the id of the element being deleted
	 * @return	Response	response from the server
	 */
	protected Response delete(String id) {
		String requestURL = this.url + id + "/";
		HttpDelete httpdelete = new HttpDelete(requestURL);
		return send_and_receive(httpdelete);
	}

	/*
	 * converts a HashMap to a List
	 * @param	options		the hashmap being converted
	 * @return	result		the converted list
	 */
	private List<NameValuePair> convertToList(
		HashMap<String, String> options) {
		List<NameValuePair> result = new ArrayList<NameValuePair>(2);
		Iterator it = options.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			result.add(new BasicNameValuePair((String)pairs.getKey(), (String)pairs.getValue()));
		}
		return result;
	}

	HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
	    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
	        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
	        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
	                ClientContext.CREDS_PROVIDER);
	        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

	        if (authState.getAuthScheme() == null) {
	            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
	            Credentials creds = credsProvider.getCredentials(authScope);
	            if (creds != null) {
	                authState.setAuthScheme(new BasicScheme());
	                authState.setCredentials(creds);
	            }
	        }
	    }    
	};

	/*
	 * sends the request and retrieve the response
	 * @param	request		the id of the element being deleted
	 * @return	Response	response from the server
	 */
	protected Response send_and_receive(HttpUriRequest request) {
		HttpClient httpclient = new DefaultHttpClient();

		if(this.getRc() == null)
			return null;

		Log.v("nebula", this.getRc().getUsername());
		if(this.getRc().getUsername() != "") {
			Log.v("nebula", "authentication");
			String username = this.getRc().getUsername();
			String password = this.getRc().getPassword();

			byte[] concat = new byte[username.length() + password.length() + 1];
			System.arraycopy(username.getBytes(), 0, concat, 0, username.length());
			System.arraycopy(":".getBytes(), 0, concat, username.length(), 1);
			System.arraycopy(password.getBytes(), 0, concat, username.length()+1, password.length());

			//byte[] base64 = Base64.encode(concat, 0);
			String base64 = Base64.encode(username+":"+password);

			//request.addHeader("Authorization", "Basic "+new String(base64).replace("\n", ""));
			request.addHeader("Authorization", "Basic "+base64.replace("\r\n", ""));
			//Log.v("nebula", "-"+base64+"-");
		}
		HttpResponse response;

		try {
			response = httpclient.execute(request);
	        InputStream instream = response.getEntity().getContent();
	        String result = convertStreamToString(instream);
	        int status = response.getStatusLine().getStatusCode();

			try {
				if(status >= 200 && status < 300) {
					return new Response(status, new JSONObject(result));
				} else {
					System.out.println(result);
					return new Response(status);
				}
			} catch(Exception e) {
				System.out.println(result);
				e.printStackTrace();
				System.exit(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/*
	 * convert a stream into a String
	 * @param	inputStream		The stream
	 * @return	result			The string
	 */
	public String convertStreamToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				inputStream.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public void setRc(RESTClient rc) {
		this.rc = rc;
	}

	public RESTClient getRc() {
		return rc;
	}
}