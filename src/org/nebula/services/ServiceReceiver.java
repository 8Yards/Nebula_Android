/*
 * author - michel
 * refactor - michel, prajwol
 */
package org.nebula.services;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServiceReceiver extends Service implements RTPAppIntf {
	private final IBinder binder = new ReceiverBinder();

	private RTPSession rtpReceiver = null;
	private ReceiverConfiguration conf = new ReceiverConfiguration();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopPlaying();
		return true;
	}

	@Override
	public void onDestroy() {
		// TODO:: check the lifecycle - is onUnbind called before this??
		stopPlaying();
	}

	public void stopPlaying() {
		// TODO: session is shared between Reciever and Sender - handle the
		// integrity
		rtpReceiver.endSession();
		conf.audioTrack.stop();
	}

	/*
	 * Start playing RTP
	 */
	public void startPlaying() {
		new Thread(new Runnable() {
			public void run() {
				play();
			}
		}).start();
	}

	/*
	 * Play audio
	 */
	public void play() {
		conf.audioTrack.play();
		Log.v("nebula", "servicereceiver: inPlayer...");
	}

	public void receiveData(final DataFrame frame, final Participant participant) {
		Log.v("nebula", "servicereceiver: receiving...");
		final byte[] data = frame.getConcatenatedData();
		conf.audioTrack.write(data, 0, data.length);
	}

	// interface method
	public int frameSize(int payloadType) {
		return 0;
	}

	// interface method
	public void userEvent(int type, Participant[] participant) {
	}

	public class ReceiverBinder extends Binder {
		public ServiceReceiver getService() {
			return ServiceReceiver.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO:: check what to return
		return START_STICKY_COMPATIBILITY;
	}

	private class ReceiverConfiguration {
		public int frequency = 11025;
		public int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

		public int bufferSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);
		public AudioTrack audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 11025,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize,
				AudioTrack.MODE_STREAM);
	}
}