package org.nebula.activities;

import org.nebula.R;
import org.nebula.client.localdb.NebulaSettingsManager;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Volume extends Activity implements OnSeekBarChangeListener {

	private NebulaSettingsManager settingsManager;

	private int valueVolume;
	private TextView seekBarValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.volume);

		settingsManager = new NebulaSettingsManager(this);

		SeekBar seekBar = (SeekBar) findViewById(R.id.sbVolume);
		seekBarValue = (TextView) findViewById(R.id.tvVolumeValue);

		int getVol = settingsManager.getVolume();
		seekBar.setProgress(getVol);
		seekBarValue.setText(String.valueOf(getVol));
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getVol,
				AudioManager.FLAG_PLAY_SOUND);

		seekBar.setOnSeekBarChangeListener(this);
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		seekBarValue.setText(String.valueOf(progress));
		valueVolume = progress;
	}

	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, valueVolume,
				AudioManager.FLAG_PLAY_SOUND);
		settingsManager.storeVolume(valueVolume);
	}

	public void doBackVolume(View v) {
		finish();
	}
}
