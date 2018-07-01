 /*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feghas.jounalapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.feghas.jounalapp.data.JournalContract.JournalEntry;

/**
 * Database helper for journal app. Manages database creation and version management.
 */
public class JournalDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = JournalDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "journals.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link JournalDbHelper}.
     *
     * @param context of the app
     */
    public JournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the thoughts table
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + JournalEntry.TABLE_NAME + " ("
                + JournalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + JournalContract.JournalEntry.COLUMN_DATE + " TEXT NOT NULL, "
                + JournalEntry.COLUMN_MESSAGE + " TEXT, "
                + JournalEntry.COLUMN_SUMMARY + " TEXT NOT NULL, "
                + JournalContract.JournalEntry.COLUMN_TITLE + " TEXT NOT NULL );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}