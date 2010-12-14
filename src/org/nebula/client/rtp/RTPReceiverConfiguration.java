package org.nebula.client.rtp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

public class RTPReceiverConfiguration {
	public static final RTPReceiverConfiguration instance = new RTPReceiverConfiguration();
	
	public int frequency = 8000;
	public int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	public int bufferSize = AudioRecord.getMinBufferSize(frequency,
			channelConfiguration, audioEncoding);
	public int frameSize = 160;

	public AudioTrack getNewTrack() {
		return new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
				channelConfiguration, audioEncoding, bufferSize,
				AudioTrack.MODE_STREAM);
	}
}