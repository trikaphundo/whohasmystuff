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

package de.freewarepoint;

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

    public static final String KEY_TYPE = "type";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_DATE = "date";
    public static final String KEY_BACK = "back";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "OpenLendDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String LENTOBJECTS_DATABASE_CREATE =
        "create table lentobjects (_id integer primary key autoincrement, "
        + "type text not null, description text not null, date date not null, "
        + "back integer not null);";

    private static final String LENTTYPES_DATABASE_CREATE =
            "create table lenttypes (_id integer primary key autoincrement, "
                    + "type text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String LENTOBJECTS_DATABASE_TABLE = "lentobjects";
    private static final String LENTTYPES_DATABASE_TABLE = "lenttypes";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(LENTOBJECTS_DATABASE_CREATE);
            db.execSQL(LENTTYPES_DATABASE_CREATE);
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


    public long createLentObject(String type, String description, Date date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_DESCRIPTION, description);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initialValues.put(KEY_DATE, dateFormat.format(date));
        initialValues.put(KEY_BACK, false);

        return mDb.insert(LENTOBJECTS_DATABASE_TABLE, null, initialValues);
    }

    public long createLentType(String type) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);

        return mDb.insert(LENTTYPES_DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteLentObject(long rowId) {
        return mDb.delete(LENTOBJECTS_DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteLentType(long rowId) {
        return mDb.delete(LENTTYPES_DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllLentObjects() {
        return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TYPE,
                KEY_DESCRIPTION, KEY_DATE, KEY_BACK}, null, null, null, null, null);
    }

    public Cursor fetchAllLentTypes() {
        return mDb.query(LENTTYPES_DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TYPE},
                null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws android.database.SQLException if note could not be found/retrieved
     */
    public Cursor fetchLentObject(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TYPE, KEY_DESCRIPTION, KEY_DATE, KEY_BACK}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateLentObject(long rowId, String type, String description, Date date) {
        ContentValues args = new ContentValues();
        args.put(KEY_TYPE, type);
        args.put(KEY_DESCRIPTION, description);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        args.put(KEY_DATE, dateFormat.format(date));
        //TODO
        args.put(KEY_BACK, false);

        return mDb.update(LENTOBJECTS_DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
