package android.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.nebula.sipClient.SIPClient;
import org.nebula.sipClient.SIPInterface;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class nebula extends Activity implements SIPInterface {

	private SIPClient sip;

	private String myAddress;
	private int myPort;
	Button no_nebula;
	Button cancel;

	String vPass;
	String vUserName;
	Request request;
	Response response;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		no_nebula = (Button) this.findViewById(R.id.no_nebula);
		Button btnSign = (Button) findViewById(R.id.login);
		Log.v("nebula", "saad");
		
		setArguments();
		
		try {
			sip = new SIPClient(myAddress, myPort, "",
					myAddress, "", null);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.exit(-1);
		}
		
		try {

			btnSign.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					EditText edit_User = (EditText) findViewById(R.id.txtUserID);
					EditText edit_Pass = (EditText) findViewById(R.id.txtPass);
					vUserName = edit_User.getText().toString();
					vPass = edit_Pass.getText().toString();
					try {
						sip.setMySIPName(vUserName);
						sip.setMyPassword(vPass);
						
						Log.v("nebula", myAddress);
						Log.v("nebula", String.valueOf(myPort));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// Log.v("nebula", "4");
					try {

						request = sip.register();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.v("nebula", "3");

					try {
						response = sip.send(request);
					} catch (Exception e) {
						// TODO Auto-generated catch block

					}

					// Log.v("nebula",
					// Integer.toString(response.getStatusCode()));
					if (response.getStatusCode() == 200) {
						UserData instance = UserData.getInstance();

						instance.setUserName(vUserName);
						instance.setUserPassword(vPass);
						Log.v("nebula", "success!!");
						Toast.makeText(getApplicationContext(),
								"Login successfull", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(nebula.this, Main.class);
						startActivity(intent);

					} else {
						Log.v("nebula", "unauthorized");
						// System.out.println("Authentication problem");
						// Toast.makeText(getApplicationContext(), vUserName,
						// Toast.LENGTH_LONG).show();
						Toast.makeText(getApplicationContext(),
								"Invalid UserName and password",
								Toast.LENGTH_LONG).show();
					}

				}

			});
		}

		catch (Exception e) {
			Log.v("nebula", "6");
			e.printStackTrace();
		}

		Log.v("nebula", "waiting..");

		pages();
	}

	private void setArguments() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						myAddress = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			Log.e("nebula", e.getMessage());
			myAddress = "127.0.0.1";
		}

		try {
			ServerSocket ss = new ServerSocket(0);
			myPort = ss.getLocalPort();
			ss.close();
		} catch (IOException e) {
			myPort = new Random(new Date().getTime()).nextInt();
		}
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		try {
			Request request = requestReceivedEvent.getRequest();
			ServerTransaction serverTransactionId = requestReceivedEvent
					.getServerTransaction();
			if (serverTransactionId == null)
				serverTransactionId = SIPClient.getSipProvider()
						.getNewServerTransaction(request);

			Log.v("nebula", "\n\nRequest " + request.getMethod()
					+ " received at " + SIPClient.getSipStack().getStackName()
					+ " with server transaction id " + serverTransactionId);

			if (request.getMethod().equals(Request.BYE))
				sip.processBye(request, serverTransactionId);
			else if (request.getMethod().equals(Request.INVITE))
				processInvite(request, serverTransactionId);
			else
				serverTransactionId.sendResponse(SIPClient.getMessageFactory()
						.createResponse(202, request));
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
	}

	private void processInvite(Request request, ServerTransaction st) {
		Log.v("nebula", "send 180");
		// dialog = serverTransactionId.getDialog();

		try {
			st.sendResponse(SIPClient.getMessageFactory().createResponse(
					Response.RINGING, request));
		} catch (Exception e) {
			e.printStackTrace();
		}
		processInviteOK(request, st);
		// processInviteCancel(request, st);
		// processBusyHere(request, st);

		Log.v("nebula", "done");
	}

	private void processInviteOK(Request request, ServerTransaction st) {
		try {
			Thread.sleep(1000);
			Log.v("nebula", "send 200");
			HeaderFactory headerFactory = SIPClient.getHeaderFactory();
			Response response = SIPClient.getMessageFactory().createResponse(
					Response.OK, request);
			Address address = SIPClient.getAddressFactory().createAddress(
					vUserName + " <sip:" + myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);

			byte[] content = request.getRawContent();

			if (content != null) {
				ContentTypeHeader contentTypeHeader = headerFactory
						.createContentTypeHeader("application", "sdp");
				System.out.println("response = " + response);
				response.setContent(content, contentTypeHeader);
			}

			st.sendResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void pages() {

		if (no_nebula.isClickable()) {
			no_nebula.setOnClickListener(new TextView.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(nebula.this, Account.class);
					startActivity(intent);

				}
			});
		}
	}

}
