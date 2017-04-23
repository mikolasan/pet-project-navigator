package io.github.mikolasan.petprojectnavigator;

import android.Manifest;
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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    DB db;
    private PetDataLoader<PetProjectLoader> activityDataLoader;
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

    private void setButtonListeners() {
        /*
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
        */
        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", ProjectActivity.STATUS_NEW);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_projects:

                                break;
                            case R.id.action_tasks:
                                Intent intent = new Intent(getApplicationContext(), TaskListActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.action_buffer:

                        }
                        return true;
                    }
                });
    }

    private void initProjectView() {
        final ListView list = (ListView) findViewById(R.id.project_view);
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
        Context context = getApplicationContext();
        try {
            activityDataLoader = new PetDataLoader<>(context, PetProjectLoader.class, new PetProjectLoader(context, db), list);
            getLoaderManager().initLoader(activityDataLoader.mainActivityId, null, activityDataLoader);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DB.getOpenedInstance();
        setContentView(R.layout.activity_main);
        setButtonListeners();
        initProjectView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyStoragePermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(activityDataLoader.mainActivityId, null, activityDataLoader);
        //activityDataLoader.notifyAdapter();
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
                    if (data != null) {
                        // Get the URI of the selected file
                        readBackupFile(data.getData());
                    } else {
                        Toast.makeText(getApplicationContext(), "Bad data", Toast.LENGTH_SHORT).show();
                    }
                }else if(resultCode == RESULT_CANCELED) {
                    //
                }
        }
    }

    private String inputToString(FileInputStream inputStream) {
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
            Toast.makeText(getApplicationContext(), "IO exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return "";
        }
        return byteArrayOutputStream.toString();
    }

    private void readBackupFile(Uri uri) {
        Log.i(OPEN_FILE_TAG, "Uri = " + uri.toString());
        try {
            Context context = getApplicationContext();
            // Get the file path from the URI
            final String path = "";//FileUtils.getPath(this, uri);
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
                String json = inputToString(inputStream);
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
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a DB backup file"), REQUEST_CODE_RESTORE_FILE);
    }
    /*
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE_RESTORE_FILE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    */
}
