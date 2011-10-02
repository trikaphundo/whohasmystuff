/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.freewarepoint.whohasmystuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenLendDbAdapter {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_DATE = "date";
    public static final String KEY_PERSON = "person";
	public static final String KEY_PERSON_KEY = "person_key";
    public static final String KEY_BACK = "back";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "OpenLendDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String LENTOBJECTS_DATABASE_CREATE =
        "create table lentobjects (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_DESCRIPTION + " text not null, " + KEY_DATE + " date not null, "
        + KEY_PERSON + " text not null, " + KEY_PERSON_KEY + " text, "
		+ KEY_BACK + " integer not null);";

    private static final String DATABASE_NAME = "data";
    private static final String LENTOBJECTS_DATABASE_TABLE = "lentobjects";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(LENTOBJECTS_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS lentobjects");
            onCreate(db);
        }
    }

    public OpenLendDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public OpenLendDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createLentObject(String description, Date date, String personName, String personKey) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESCRIPTION, description);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initialValues.put(KEY_DATE, dateFormat.format(date));
        initialValues.put(KEY_PERSON, personName);
		initialValues.put(KEY_PERSON_KEY, personKey);
        initialValues.put(KEY_BACK, false);

		Log.e("Tag", "Adding with " + personKey);

        return mDb.insert(LENTOBJECTS_DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteLentObject(long rowId) {
        return mDb.delete(LENTOBJECTS_DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchLentObjects() {
        return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_DESCRIPTION, KEY_DATE, KEY_PERSON, KEY_PERSON_KEY, KEY_BACK}, KEY_BACK + "=0", null, null, null, null);
    }

	public Cursor fetchReturnedObjects() {
		return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
				KEY_DESCRIPTION, KEY_DATE, KEY_PERSON, KEY_PERSON_KEY, KEY_BACK}, KEY_BACK + "=1", null, null, null, null);
	}

    public boolean updateLentObject(long rowId, String description, Date date, String personName, String personKey) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, description);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        args.put(KEY_DATE, dateFormat.format(date));
        args.put(KEY_PERSON, personName);
		args.put(KEY_PERSON_KEY, personKey);

		Log.e("Tag", "Adding with " + personKey);

		return updateLentObject(rowId, args);
    }

	public boolean markLentObjectAsReturned(long rowId) {
		ContentValues values = new ContentValues();
		values.put(KEY_BACK, true);
		return updateLentObject(rowId, values);
	}

	private boolean updateLentObject(long rowId, ContentValues values) {
		return mDb.update(LENTOBJECTS_DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
