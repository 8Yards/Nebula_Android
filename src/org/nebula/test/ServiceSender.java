package org.nebula.test;

import java.net.SocketException;

import org.nebula.rtpClient.RTPClient;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

public class ServiceSender extends Service implements RTPAppIntf {
	private RTPClient rtpClientSender = null;
	boolean isRecording;

    @Override 
    public void onCreate() {
        Log.d("nebula", "onCreate");
    	super.onCreate();
    	isRecording = true;
		
        Log.d("nebula", "finish create");
    } 
    
    @Override 
    public int onStartCommand(Intent intent, int flags, int startId) { 
		Log.v("nebula", "service start");
	    
		try {
			int portRTP = intent.getExtras().getInt("portRTP");//5066;//
			int portRTCP = intent.getExtras().getInt("portRTCP");//5067;//
			rtpClientSender = new RTPClient(portRTP, portRTCP, null, false);
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		Participant p = new Participant("130.229.141.195", 5062,5063);
		rtpClientSender.addParticipant(p);
		
		Thread recordThread = new Thread(new Runnable() {
			public void run() {
				record();
			}
		});
		recordThread.start();
        return START_STICKY; 
    } 
    
    @Override 
    public void onDestroy() { 
    	isRecording = false;
    	rtpClientSender.endSession();
    	Log.v("nebula", "service finish");
    } 
    
	@Override
	public IBinder onBind(Intent arg0) { return null; }
		
	protected void sendRTP(byte[] buffer, int size) {
		Log.v("nebula", "sending");
		byte[] currentBuffer;
		if(size <= 1480) {
			currentBuffer = new byte[size];
			System.arraycopy(buffer, 0, currentBuffer, 0, size);
			rtpClientSender.sendData(currentBuffer);
		}
		else {
			int i = size;
			currentBuffer = new byte[1480];
			while(i > 1480) {
				System.arraycopy(buffer, size-i, currentBuffer, 0, 1480);
				rtpClientSender.sendData(currentBuffer);
				i -= 1480;
			}
			System.arraycopy(buffer, size-i, currentBuffer, 0, i);
			rtpClientSender.sendData(currentBuffer);
		}
	}
	
	public void record() {
		Log.v("nebula", "call record");
		int frequency = 11025;
		int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	  
		int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration,  audioEncoding);
		AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 
	                                              frequency, channelConfiguration, 
	                                              audioEncoding, bufferSize);
	   
		byte[] buffer = new byte[bufferSize];   
		audioRecord.startRecording();
		Log.v("nebula", "recording..");
	
		while (isRecording) {
			int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
			sendRTP(buffer, bufferReadResult);
		}
	
		Log.v("nebula", "stop recording..");
	
		audioRecord.stop();   
	}
	
	private void stopRecording() throws Exception {
		Log.v("nebula", "stopRecording");
		isRecording=false;
	}

	public int frameSize(int payloadType) { return 0; }

	public void receiveData(DataFrame frame, Participant participant) {}

	public void userEvent(int type, Participant[] participant) {}
}