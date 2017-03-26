package io.github.mikolasan.petprojectnavigator;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, ConnectionCallbacks,
        OnConnectionFailedListener {

    DB db;
    SimpleCursorAdapter cursorAdapter;
    private static final String TAG = "drive-quickstart";
    private static final String OPEN_FILE_TAG = "open-file-dialog";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_RESTORE_FILE = 4;
    private static final int REQUEST_EXTERNAL_STORAGE = 5;
    private GoogleApiClient mGoogleApiClient;


    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(FragmentActivity activity, String permission) {
        // Check if we have write permission
        int status = ContextCompat.checkSelfPermission(activity, permission);
        if (status != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[] {permission}, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        db = DB.getOpenedInstance();

        final Button btn_backup = (Button) findViewById(R.id.btn_backup_project);
        btn_backup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
            }
        });

        final Button btn_to_cloud = (Button) findViewById(R.id.btn_to_cloud);
        btn_to_cloud.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toCloud();
            }
        });

        final Button btn_restore = (Button) findViewById(R.id.btn_restore);
        btn_restore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                restoreDB();
            }
        });

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", ProjectActivity.STATUS_NEW);
                startActivity(intent);
            }
        });

        String[] from = new String[] { DB.COLUMN_NAME, DB.COLUMN_DESC  };
        int[] to = new int[] { R.id.lbl_title, R.id.lbl_desc };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_project, null, from, to, 0);
        final ListView list = (ListView) findViewById(R.id.project_view);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor) list.getItemAtPosition(i);

                int id_column = c.getColumnIndex(DB.COLUMN_ID);
                int projectId = c.getInt(id_column);
                int name_column = c.getColumnIndex(DB.COLUMN_NAME);
                int desc_column = c.getColumnIndex(DB.COLUMN_DESC);

                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", ProjectActivity.STATUS_EDIT);
                intent.putExtra("project_id", projectId);
                intent.putExtra("title", c.getString(name_column));
                intent.putExtra("description", c.getString(desc_column));
                startActivity(intent);
            }
        });

        // добавляем контекстное меню к списку
        //registerForContextMenu(list);

        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyStoragePermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API client connected.");
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    static class MyCursorLoader extends CursorLoader {

        DB db;

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllProjects();
            return cursor;
        }

        
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                }
                break;
            case REQUEST_CODE_RESTORE_FILE:
                if(resultCode == RESULT_OK) {
                    Context context = getApplicationContext();
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(OPEN_FILE_TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(context,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                            Log.i(OPEN_FILE_TAG, "File Selected: " + path);
                            File selectedFile = new File(path);
                            if(selectedFile.exists()) {
                                FileInputStream inputStream;
                                try {
                                    inputStream = new FileInputStream(selectedFile);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                int i;
                                try {
                                    i = inputStream.read();
                                    while (i != -1) {
                                        byteArrayOutputStream.write(i);
                                        i = inputStream.read();
                                    }
                                    inputStream.close();
                                } catch (IOException e) {
                                    Toast.makeText(context, "IO exception", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    return;
                                }
                                String json = byteArrayOutputStream.toString();
                                if(db.restore(json)){
                                    Toast.makeText(context, "DB restored", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to restore", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                CharSequence status = "File " + path + " doesn't exist";
                                Log.i(OPEN_FILE_TAG, status.toString());
                                Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(OPEN_FILE_TAG, "File select error", e);
                        }
                    } else {
                        Toast.makeText(context, "Bad data", Toast.LENGTH_SHORT).show();
                    }
                }else if(resultCode == RESULT_CANCELED) {
                    //
                }
        }
    }

    public void backup() {
        saveLocalCopy(db.prepareJson());
    }

    public void toCloud() {
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        String str = db.prepareJson();
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        try {
                            outputStream.write(str.getBytes(Charset.forName("UTF-8")));
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        String date = ""; // !TODO
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("application/json").setTitle("pet-project-navigator-backup" + date + ".json").build();
                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
    }

    private void saveLocalCopy(String str) {
        SharedPreferences sharedPref = getSharedPreferences("appData", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("json", str);
        prefEditor.apply(); // apply() is more efficient, it starts an asynchronous commit
    }

    private String loadLocalCopy() {
        SharedPreferences sharedPref = getSharedPreferences("appData", Context.MODE_PRIVATE);
        return sharedPref.getString("json", "");
    }

    private void restoreDB() {
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE_RESTORE_FILE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }
}
