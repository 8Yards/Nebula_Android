/*
 * author - michel
 * refactor - prajwol, michel
 */
package org.nebula.client.rtp;

import java.util.Iterator;

import jlibrtp.Participant;
import jlibrtp.RTPSession;

import org.sipdroid.codecs.G711;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RTPSender extends Service {
	private final IBinder binder = new SenderBinder();

	// TODO:: check the thread-safety of this variable
	private boolean isRecording;
	private RTPSession rtpSender = null;
	private RTPSenderConfiguration config = new RTPSenderConfiguration();

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

	protected void sendRTP(byte[] buffer, int size) {
		int i = size;
		int maxUnitSize = 160;
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

	public void startRecordingAndSending() {
		// TODO:: check how AsyncTask can be used to update the progress and UI
		new Thread(new Runnable() {
			public void run() {
				isRecording = true;
				config.audioRecord.startRecording();

				Log.v("nebula", "rtpSender: recording...");
				while (isRecording) {
					int bufferReadResult = config.audioRecord.read(
							config.buffer, 0, config.frameSize);

					byte[] aLawBuffer = new byte[bufferReadResult];
					G711.linear2alaw(config.buffer, 0, aLawBuffer,
							bufferReadResult);
					sendRTP(aLawBuffer, bufferReadResult);
				}
				config.audioRecord.stop();
				Log.v("nebula", "rtpSender: recording stopped.");
			}
		}).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO:: check what to return
		return START_STICKY_COMPATIBILITY;
	}

	public class SenderBinder extends Binder {
		public RTPSender getService() {
			return RTPSender.this;
		}
	}

	public RTPSession getRtpSender() {
		return rtpSender;
	}

	public void setRtpSender(RTPSession rtpSender) {
		this.rtpSender = rtpSender;
	}
}