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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Journal app.
 */
public final class JournalContract {

    private JournalContract() {}

//    content authority is set up
    public static final String CONTENT_AUTHORITY = "com.feghas.jounalapp.data";

    /**
     * The CONTENT_AUTHORITY is used to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_THOUGHTS = "thoughts";


    public static final class JournalEntry implements BaseColumns {

        /** The content URI to access the journal data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_THOUGHTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of thoughts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THOUGHTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single thoughts.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THOUGHTS;

        /** Name of database table for journal app */
        public final static String TABLE_NAME = "thoughts";

        /**
         * Unique ID number for the thought (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the thought.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DATE ="date";

        /**
         * message in the jounal.
         *
         * Type: TEXT
         */
        public final static String COLUMN_MESSAGE = "message";

        /**
         * summary.
         *
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUMMARY = "summary";

        /**
         * title of the messsage .
         *
         * Type: TEXT
         */
        public final static String COLUMN_TITLE = "title";

    }

}

