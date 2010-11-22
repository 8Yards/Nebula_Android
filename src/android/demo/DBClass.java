package android.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBClass {
	public static final String KEY_ROWID = "_id";
	public static final String Key_User_Name = "n_username";
	public static final String Key_Full_Name = "f_name";
	public static final String Key_email = "email";
	public static final String Key_pass = "pass";
	public static final String KEY_ROWID1 = "_id";
	public static final String Key_user = "user";
	public static final String Key_grp = "grp";

	private static final String DATABASE_NAME = "Neb";
	private static final String DATABASE_TABLE = "Contact12";
	private static final String DATABASE_TABLE1 = "NebulaContact";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table Contact12 (n_username text, "
			+ "_id integer primary key autoincrement,"
			+ "email text not null, "
			+ "pass text not null, "
			+ "f_name text not null);";

	private static final String DATABASE_CREATE1 = "create table NebulaContact (user text, "
			+ "_id integer primary key autoincrement," + "grp text not null);";

	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBClass(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String TAG = null;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE1);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS Contact12,NebulaContact");
			onCreate(db);
		}

	}

	// ---opens the database---
	public DBClass open() throws SQLException {

		db = DBHelper.getWritableDatabase();
		return this;

	}

	// ---closes the database---
	public void close() {
		DBHelper.close();
	}

	// ---insert a title into the database---
	public long insertTitle(String n_username, String f_name, String email,
			String pass) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(Key_User_Name, n_username);
		initialValues.put(Key_Full_Name, f_name);
		initialValues.put(Key_email, email);
		initialValues.put(Key_pass, pass);
		// initialValues.put(Key_confirm_password, C_pwd);
		long _long = db.insert(DATABASE_TABLE, null, initialValues);
		return _long;
	}

	public long insertTitle1(String user, String grp) {
		ContentValues initialValues1 = new ContentValues();
		initialValues1.put(Key_user, user);
		initialValues1.put(Key_grp, grp);

		// initialValues.put(Key_confirm_password, C_pwd);
		long _long = db.insert(DATABASE_TABLE1, null, initialValues1);
		return _long;
	}

	public Cursor getAllTitles() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				Key_User_Name, Key_Full_Name, Key_email, Key_pass }, null,
				null, null, null, null);
	}

	public Cursor getAllTitles1() {
		return db.query(DATABASE_TABLE1, new String[] { KEY_ROWID1, Key_user,
				Key_grp }, null, null, null, null, null);
	}

	public Cursor getTitle1(String user) throws SQLException {
		try {
			Cursor mCursor =

			db.query(true, DATABASE_TABLE1, new String[] { Key_user, },
					"user ='" + user.toString() + "'", null, null, null, null,
					null);

			return mCursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public Cursor getTitle(String n_username) throws SQLException {
		try {
			Cursor mCursor =

			db.query(true, DATABASE_TABLE, new String[] { Key_User_Name, },
					"n_username ='" + n_username.toString() + "'", null, null,
					null, null, null);

			return mCursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public Cursor getAuthentication(String n_username, String Password)
			throws SQLException {
		try {
			Cursor mCursor = db.query(true, DATABASE_TABLE,
					new String[] { Key_User_Name, }, "n_username ='"
							+ n_username.toString() + "' and  pass='"
							+ Password.toString() + "'", null, null, null,
					null, null);

			return mCursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public Cursor getAuthentication1(String user) throws SQLException {
		try {
			Cursor mCursor = db.query(true, DATABASE_TABLE1,
					new String[] { Key_user, }, "user ='" + user.toString()
							+ "'", null, null, null, null, null);

			return mCursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	// ---updates a title---
	public boolean updateTitle(String n_username, String f_name,
			String e_address) {
		ContentValues args = new ContentValues();
		args.put(Key_User_Name, n_username);
		args.put(Key_Full_Name, f_name);
		args.put(Key_email, e_address);
		return db.update(DATABASE_TABLE, args,
				Key_User_Name + "=" + n_username, null) > 0;
	}

	public boolean updateTitle1(String user, String grp) {
		ContentValues args = new ContentValues();
		args.put(Key_user, user);
		args.put(Key_grp, grp);

		return db.update(DATABASE_TABLE1, args, Key_user + "=" + user, null) > 0;
	}

}
