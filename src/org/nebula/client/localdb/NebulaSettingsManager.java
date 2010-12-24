package org.nebula.client.localdb;

import java.util.HashMap;
import java.util.Map;

import org.nebula.main.NebulaApplication;

import android.content.Context;

public class NebulaSettingsManager {
	private NebulaLocalDB localDB;

	public NebulaSettingsManager(Context ctx) {
		localDB = NebulaApplication.getInstance().getMyLocalDB();
	}

	public void storeLoginParameters(String userName, String password,
			boolean isChecked) {

		if (isChecked == false) {
			password = "";
		}

		localDB.open();
		localDB.insertUpdateKeyValue(NebulaLocalDB.KEY_USER, userName);
		localDB.insertUpdateKeyValue(NebulaLocalDB.KEY_PASSWORD, password);
		localDB.insertUpdateKeyValue(NebulaLocalDB.KEY_CHECKED, String
				.valueOf(isChecked));
		localDB.close();
	}

	public Map<String, String> getLoginParameters() {
		Map<String, String> loginParameters = new HashMap<String, String>();

		localDB.open();
		loginParameters.put(NebulaLocalDB.KEY_USER, localDB
				.getValueByKey(NebulaLocalDB.KEY_USER));
		loginParameters.put(NebulaLocalDB.KEY_PASSWORD, localDB
				.getValueByKey(NebulaLocalDB.KEY_PASSWORD));
		loginParameters.put(NebulaLocalDB.KEY_CHECKED, localDB
				.getValueByKey(NebulaLocalDB.KEY_CHECKED));
		localDB.close();

		return loginParameters;
	}

	public void storeVolume(int value) {
		localDB.open();
		localDB.insertUpdateKeyValue(NebulaLocalDB.KEY_VOLUME, String
				.valueOf(value));
		localDB.close();
	}

	public int getVolume() {
		localDB.open();
		String value = localDB.getValueByKey(NebulaLocalDB.KEY_VOLUME);
		localDB.close();

		if (value == "") {
			return 50;
		}
		return Integer.parseInt(value);
	}
}
