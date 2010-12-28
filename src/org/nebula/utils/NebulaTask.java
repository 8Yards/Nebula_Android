package org.nebula.utils;

import android.os.AsyncTask;
	
public abstract class NebulaTask extends AsyncTask<Object, Integer, Long> {
	protected abstract Long doInBackground(Object... params);
}