package org.nebula.client.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NebulaLocalDB extends SQLiteOpenHelper {
	private SQLiteDatabase db;

	private static final String DATABASE_NAME = "NebulaDB";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_SETTINGS = "settings";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_VALUE = "value";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_USER = "userName";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_CHECKED = "isChecked";
	public static final String KEY_VOLUME = "volume";

	private static final String DATABASE_CREATE_QUERY = "CREATE TABLE "
			+ TABLE_SETTINGS + " (" + "_id integer primary key autoincrement, "
			+ COLUMN_KEY + " text  null, " + COLUMN_VALUE + " text  null);";

	public NebulaLocalDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// CAUTION: we drop the previous table here
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		onCreate(db);
	}

	public void open() throws SQLException {
		db = getWritableDatabase();
	}

	public long insertUpdateKeyValue(String key, String value) {
		db.delete(TABLE_SETTINGS, COLUMN_KEY + " = ? ", new String[] { key });

		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_KEY, key);
		initialValues.put(COLUMN_VALUE, value);
		return db.insert(TABLE_SETTINGS, null, initialValues);
	}

	public String getValueByKey(String key) {
		Cursor mCursor = db.query(true, TABLE_SETTINGS,
				new String[] { COLUMN_VALUE }, COLUMN_KEY + " = ? ",
				new String[] { key }, null, null, null, null);
		if (mCursor != null) {
			if (mCursor.moveToFirst() == true) {
				return mCursor.getString(0);
			}
		}
		return "";
	}

}
