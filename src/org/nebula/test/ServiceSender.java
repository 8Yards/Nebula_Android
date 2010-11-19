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

public class ServiceSender extends Service implements RTPAppIntf  {
	private RTPClient rtpClientSender = null;
	boolean isRecording;

    @Override 
    public void onCreate() {
        Log.d("nebula", "onCreate");
    	super.onCreate();
    	isRecording = true;
    	
        Log.d("nebula", "finish create");
    } 
    
    public void addParticipant(Participant p) {
    	//Participant p = new Participant("130.229.141.195", 5062,5063);
    	Log.v("nebula", "add a participant");
    	rtpClientSender.addParticipant(p);
    }
    
    @Override 
    public int onStartCommand(Intent intent, int flags, int startId) { 
    	return 0;
    } 
    
    @Override 
    public void onDestroy() { 
    	isRecording = false;
    	rtpClientSender.endSession();
    	Log.v("nebula", "service finish");
} 
    
	@Override
	public boolean onUnbind(Intent intent) {
    	isRecording = false;
    	rtpClientSender.endSession();
    	Log.v("nebula", "service finish");
    	return true;
	} 
    
	@Override
	public IBinder onBind(Intent intent) {
		Log.v("nebula", "service start");
	    
		try {
			int portRTP = intent.getExtras().getInt("portRTP");
			int portRTCP = intent.getExtras().getInt("portRTCP");
			rtpClientSender = new RTPClient(portRTP, portRTCP, null, false);
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
        return new SenderBinder(this); 
	}
		
	protected void sendRTP(byte[] buffer, int size) {
		int offset=0;
		byte[] lens = new byte[size];
		for (int i = 0; (offset + i + 1) < size; i += 2) {
		    lens[i] = buffer[offset + i + 1];
		    lens[i + 1] = buffer[offset + i];
		}
		for (int i = 1, j = 0; i < size; i += 2, j++) {
		    lens[j] = lens[i];
		}
		
		Log.v("nebula", "sending");
		byte[] currentBuffer;
		if(size <= 1480) {
			currentBuffer = new byte[size];
			System.arraycopy(lens, 0, currentBuffer, 0, size);
			rtpClientSender.sendData(currentBuffer);
		}
		else {
			int i = size;
			currentBuffer = new byte[1480];
			while(i > 1480) {
				System.arraycopy(lens, size-i, currentBuffer, 0, 1480);
				rtpClientSender.sendData(currentBuffer);
				i -= 1480;
			}
			System.arraycopy(lens, size-i, currentBuffer, 0, i);
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

	public int frameSize(int payloadType) { return 0; }

	public void receiveData(DataFrame frame, Participant participant) {}

	public void userEvent(int type, Participant[] participant) {}

	public void startRecording() {
		
		Thread recordThread = new Thread(new Runnable() {
			public void run() {
				record();
			}
		});
		recordThread.start();
	}

	public void stopRecording() {
    	isRecording = false;
    	rtpClientSender.endSession();
	}
}