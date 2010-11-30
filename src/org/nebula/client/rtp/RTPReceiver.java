/*
 * author - michel
 * refactor - michel, prajwol
 */
package org.nebula.client.rtp;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class RTPReceiver implements RTPAppIntf {
	private RTPSession rtpReceiver = null;
	private ReceiverConfiguration conf = new ReceiverConfiguration();

	public void stopPlaying() {
		// TODO: session is shared between Reciever and Sender - handle the
		// integrity
		rtpReceiver.endSession();
		conf.audioTrack.stop();
	}

	/*
	 * Start playing RTP
	 */
	// TODO:: check if play is done in separate thread itself
	public void startPlaying() {
		new Thread(new Runnable() {
			public void run() {
				conf.audioTrack.play();
				Log.v("nebula", "servicereceiver: inPlayer...");
			}
		}).start();
	}

	public void receiveData(final DataFrame frame, final Participant participant) {
		final byte[] data = frame.getConcatenatedData();
		conf.audioTrack.write(data, 0, data.length);		
		Log.v("nebula", "servicereceiver: received - " + data.length);
	}

	// interface method
	public int frameSize(int payloadType) {
		return 0;
	}

	// interface method
	public void userEvent(int type, Participant[] participant) {
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