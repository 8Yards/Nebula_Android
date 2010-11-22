package org.nebula.restClient;
import org.json.JSONObject;

/*
 * Class to contain the server's response
 * @author	Nebula
 */
public class Response {
	private int status;
	private JSONObject result;

	/*
	 * Constructor
	 * @param	status	The status of the reponse
	 * @param	result	The body of the response
	 */
	public Response(int status, JSONObject result) {
		this.status = status;
		this.result = result;
	}

	/*
	 * Constructor
	 * @param	status	The status of the reponse
	 */
	public Response(int status) {
		this.status = status;
		this.result = new JSONObject();
	}

	/*
	 * Returns the status
	 * @return	status	The status of the response
	 */
	public int getStatus() { return status; }
	
	/*
	 * Returns the body
	 * @return	result	The body of the response
	 */
	public JSONObject getResult() { return result; }
}
