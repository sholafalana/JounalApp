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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.feghas.jounalapp.data.JournalContract.JournalEntry;

/**
 * {@link ContentProvider} for Journal app.
 */
public class JournalProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = JournalProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the Journal table */
    private static final int THOUGHTS = 100;

    /** URI matcher code for the content URI for a single thought in the thoughts table */
    private static final int THOUGHTS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY, JournalContract.PATH_THOUGHTS, THOUGHTS);


        sUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY, JournalContract.PATH_THOUGHTS + "/#", THOUGHTS_ID);
    }

    /** Database helper object */
    private JournalDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new JournalDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case THOUGHTS:
                // For the THOUGHTS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the thoughts table.
                cursor = database.query(JournalEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case THOUGHTS_ID:

                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = JournalEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Cursor containing that row of the table.
                cursor = database.query(JournalEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case THOUGHTS:
                return insertThought(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a thought into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertThought(Uri uri, ContentValues values) {
        // Check that the date is not null
        String date = values.getAsString(JournalContract.JournalEntry.COLUMN_DATE);
        if (date == null) {
            throw new IllegalArgumentException("Pet requires a date");
        }

        // Check that the titie is not null
        String title = values.getAsString(JournalEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Journal requires a title");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new thoughts with the given values
        long id = database.insert(JournalEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the thoughts content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case THOUGHTS:
                return updateThoughts(uri, contentValues, selection, selectionArgs);
            case THOUGHTS_ID:
                // For the THOUGHTS_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = JournalContract.JournalEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateThoughts(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update thought in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments.
     * Return the number of rows that were successfully updated.
     */
    private int updateThoughts(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link JournalEntry#COLUMN_DATE} key is present,
        // check that the date value is not null.
        if (values.containsKey(JournalContract.JournalEntry.COLUMN_DATE)) {
            String date = values.getAsString(JournalContract.JournalEntry.COLUMN_DATE);
            if (date == null) {
                throw new IllegalArgumentException("Journal requires a name ");
            }
        }

//         If the {@link JournalEntry#COLUMN_SUMMARY} key is present,
//         check that the summary value is null.
        if (values.containsKey(JournalContract.JournalEntry.COLUMN_SUMMARY)) {
            String summary = values.getAsString(JournalEntry.COLUMN_SUMMARY);
            if (summary == null) {
                throw new IllegalArgumentException("Journal requires valid summary");
            }
        }

        // If the {@link JournalEntry#COLUMN_TITLE} key is present,
        // check that the title value is null.
        if (values.containsKey(JournalEntry.COLUMN_TITLE)) {

            String title = values.getAsString(JournalEntry.COLUMN_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Journal requires valid title");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(JournalEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case THOUGHTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case THOUGHTS_ID:
                // Delete a single row given by the ID in the URI
                selection = JournalEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(JournalContract.JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case THOUGHTS:
                return JournalEntry.CONTENT_LIST_TYPE;
            case THOUGHTS_ID:
                return JournalEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
