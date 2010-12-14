package org.nebula.client.rtp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class RTPSenderConfiguration {
	public int frequency = 8000;
	public int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	public int bufferSize = AudioRecord.getMinBufferSize(frequency,
			channelConfiguration, audioEncoding);
	public int frameSize = 160;
	public AudioRecord audioRecord = new AudioRecord(
			MediaRecorder.AudioSource.MIC, frequency, channelConfiguration,
			audioEncoding, bufferSize);
	public short[] buffer = new short[bufferSize];

}