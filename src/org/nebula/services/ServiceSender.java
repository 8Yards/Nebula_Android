/*
 * author - michel
 * refactor - michel, prajwol
 */
package org.nebula.services;

import java.util.Iterator;

import jlibrtp.Participant;
import jlibrtp.RTPSession;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServiceSender extends Service {
	private final IBinder binder = new SenderBinder();

	// TODO:: check the thread-safety of this variable
	private boolean isRecording;
	private RTPSession rtpSender = null;
	private SenderConfiguration config = new SenderConfiguration();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopRecording();
		return true;
	}

	@Override
	public void onDestroy() {
		// TODO:: check the lifecycle - is onUnbind called before this??
		stopRecording();
	}

	public void stopRecording() {
		isRecording = false;
		rtpSender.endSession();
	}

	public int numberOfReceivers() {
		Iterator<Participant> receivers = rtpSender.getUnicastReceivers();
		int i = 0;
		while (receivers.hasNext()) {
			receivers.next();
			i++;
		}
		return i;
	}

	/*
	 * Send the audio buffer through RTP
	 * 
	 * @param buffer
	 * 
	 * @param size
	 */
	protected void sendRTP(byte[] buffer, int size) {
		int i = size;
		int maxUnitSize = 1480;
		byte[] currentBuffer = new byte[maxUnitSize];

		// send the pieces
		while (i > maxUnitSize) {
			System.arraycopy(buffer, size - i, currentBuffer, 0, maxUnitSize);
			rtpSender.sendData(currentBuffer);
			i -= maxUnitSize;
		}

		// send rest of the buffer
		System.arraycopy(buffer, size - i, currentBuffer, 0, i);
		rtpSender.sendData(currentBuffer);
	}

	/*
	 * Record and send RTP
	 */
	public void record() {
		Log.v("nebula", "servicesender: call record");

		isRecording = true;
		// TODO:: check if new AudioRecord is necessary or not
		config.audioRecord.startRecording();

		Log.v("nebula", "servicesender: recording...");
		while (isRecording) {
			int bufferReadResult = config.audioRecord.read(config.buffer, 0,
					config.bufferSize);
			sendRTP(config.buffer, bufferReadResult);
		}

		config.audioRecord.stop();
		Log.v("nebula", "servicesender: recording stopped.");
	}

	/*
	 * Start recording
	 */
	public void startRecording() {
		// TODO:: check how AsyncTask can be used to update the progress and UI
		new Thread(new Runnable() {
			public void run() {
				record();
			}
		}).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO:: check what to return
		return START_STICKY_COMPATIBILITY;
	}

	public class SenderBinder extends Binder {
		public ServiceSender getService() {
			return ServiceSender.this;
		}
	}

	private class SenderConfiguration {
		public int frequency = 11025;
		public int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

		public int bufferSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);
		public AudioRecord audioRecord = new AudioRecord(
				MediaRecorder.AudioSource.MIC, frequency, channelConfiguration,
				audioEncoding, bufferSize);

		public byte[] buffer = new byte[bufferSize];
	}

	public RTPSession getRtpSender() {
		return rtpSender;
	}

	public void setRtpSender(RTPSession rtpSender) {
		this.rtpSender = rtpSender;
	}
}