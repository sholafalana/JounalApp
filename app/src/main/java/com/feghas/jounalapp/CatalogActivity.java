package com.feghas.jounalapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.feghas.jounalapp.data.JournalContract;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;


public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the thought data loader */
    private static final int THOUGHT_LOADER = 0;

    private static final int RC_SIGN_IN = 123;

    /** Adapter for the ListView */
    private ThoughtsCursorAdapter mCursorAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        mAuth = FirebaseAuth.getInstance();


        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {


                } else {

                    // Choose authentication providers
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setTheme(R.style.loginTheme)
                                    .setLogo(R.drawable.ic_launcher_foreground)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);


                }


//
            }
        };    // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the thoughts data
        ListView petListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of journal data in the Cursor.
        // There is no thought data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ThoughtsCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific thought that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link JournalEntry#CONTENT_URI}.

                Uri currentThoughtUri = ContentUris.withAppendedId(JournalContract.JournalEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentThoughtUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(THOUGHT_LOADER, null, this);
    }



  @Override
    protected  void onPause() {
      super.onPause();
      mAuth.removeAuthStateListener(firebaseAuthStateListener);
  }



    @Override
    protected  void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

// ...
  
    /**
     * Helper method to delete all thought in the database.
     */
    private void deleteAllThoughts() {
        int rowsDeleted = getContentResolver().delete(JournalContract.JournalEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from thought database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllThoughts();
                return true;

            case R.id.action_logout:
                AlertDialog.Builder  builder = new AlertDialog.Builder(CatalogActivity.this);
                builder.setTitle("Confirm Logout");
                builder.setMessage("Are you sure you want to Logout?");
//                builder.setIcon(R.drawable.logo);

                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(getApplicationContext(), "Pressed Yes button", Toast.LENGTH_LONG).show();

                    }
                });
                builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AuthUI.getInstance()
                                .signOut(getBaseContext())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // user is now signed out

//                                        Toast.makeText(getApplicationContext(), "Pressed No button", Toast.LENGTH_LONG).show();
                                    }
                                });



                    }
                });
                AlertDialog alert= builder.create();
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                JournalContract.JournalEntry._ID,
                JournalContract.JournalEntry.COLUMN_DATE,
                JournalContract.JournalEntry.COLUMN_SUMMARY,
                JournalContract.JournalEntry.COLUMN_TITLE,
                JournalContract.JournalEntry.COLUMN_MESSAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                JournalContract.JournalEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ThoughtsCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
