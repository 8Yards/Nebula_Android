/*
 * author - michel
 * refactor - michel, prajwol
 */
package org.nebula.client.rtp;

import java.util.Enumeration;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

import org.sipdroid.codecs.G711;

import android.util.Log;

public class RTPReceiver implements RTPAppIntf {
	private RTPSession rtpReceiver = null;

	public RTPReceiver(RTPSession rtpSession) {
		this.rtpReceiver = rtpSession;
	}

	public void stopPlaying() {
		Enumeration<Participant> enuPart = rtpReceiver.getParticipants();
		if (enuPart != null) {
			while (enuPart.hasMoreElements()) {
				enuPart.nextElement().track.stop();
			}
		}
		rtpReceiver.endSession();
		Log.v("nebula", "rtpReceiver: stopped");
	}

	public void startPlaying() {
		new Thread(new Runnable() {
			public void run() {
				Enumeration<Participant> enuPart = rtpReceiver
						.getParticipants();
				if (enuPart != null) {
					while (enuPart.hasMoreElements()) {
						enuPart.nextElement().track.play();
					}
				}
				Log.v("nebula", "rtpReceiver: inPlayer...");
			}
		}).start();
	}

	public void receiveData(final DataFrame frame, final Participant participant) {
		final byte[] data = frame.getConcatenatedData();
		short[] linData = new short[RTPReceiverConfiguration.instance.frameSize];

		G711.alaw2linear(data, linData, linData.length);
		participant.track.write(linData, 0, linData.length);
	}

	// interface method
	public int frameSize(int payloadType) {
		return 0;
	}

	// interface method
	public void userEvent(int type, Participant[] participant) {
	}
}