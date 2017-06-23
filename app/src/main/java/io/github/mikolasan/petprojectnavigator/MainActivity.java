package io.github.mikolasan.petprojectnavigator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static io.github.mikolasan.petprojectnavigator.BackupManager.readBackupFile;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    PetDatabase petDatabase;
    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_RESTORE_FILE = 4;
    private static final int REQUEST_EXTERNAL_STORAGE = 5;
    private GoogleApiClient mGoogleApiClient;

    PetMainPager mainPager;
    ListView listView;

    private final int drawerBackupPos = 3;
    private final int drawerRestorePos = 4;
    private final int drawerToCloudPos = 5;
    private final int drawerFromCloudPos = 6;
    private final int drawerAddProjectPos = 7;

    // Checks if the app has permission to write to device storage
    // If the app does not has permission then the user will be prompted to grant permissions
    public static void verifyStoragePermissions(Activity activity, String permission) {
        // Check if we have write permission
        int status = ContextCompat.checkSelfPermission(activity, permission);
        if (status != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[] {permission}, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void initDrawer()
    {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        listView.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, getResources().getStringArray(R.array.menu_list)));
        // Set the list's click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
		defineAction(position);
		drawerLayout.closeDrawer(listView);
	});
    }

    private void defineAction(int drawerPosition) {
        mainPager.defineAction(drawerPosition);
        switch (drawerPosition) {
            case drawerAddProjectPos:
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", ProjectActivity.STATUS_NEW);
                startActivity(intent);
                break;
            case drawerBackupPos:
                backup();
                break;
            case drawerRestorePos:
                petDatabase.restore(loadLocalCopy());
                break;
            case drawerToCloudPos:
                toCloud();
                break;
            case drawerFromCloudPos:
                restoreDB();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        petDatabase = PetDatabase.getOpenedInstance();
        setContentView(R.layout.activity_main);
        mainPager = new PetMainPager(this);
        mainPager.setButtonListeners(this);
        initDrawer();

        final Toolbar petToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(petToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void createMenu(Menu menu) {
        MenuItem combineItem = menu.findItem(R.id.combine_buffer);
        combineItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(this, ProjectActivity.class);
            intent.putExtra("status", ProjectActivity.STATUS_NEW);
            intent.putExtra("from_buffer", true);
            startActivity(intent);
            return true;
        });

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mainPager.setupSearchItem(searchItem);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        createMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_add_project:
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", ProjectActivity.STATUS_NEW);
                startActivity(intent);
                return true;

            case R.id.criterion_tech:
            case R.id.criterion_name:
            case R.id.criterion_desc:
            case R.id.criterion_time:
                mainPager.setSearchCriterion(item.getItemId());
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyStoragePermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
        petDatabase.close();
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
                        try {
                            String json = readBackupFile(data.getData());
                            if (petDatabase.restore(json)) {
                                Toast.makeText(this, "DB restored", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to restore", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Bad data", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    public void backup() {
        saveLocalCopy(petDatabase.prepareJson());
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
        String str = petDatabase.prepareJson();
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(result -> {
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
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss", Locale.US);
                    String date = simpleDateFormat.toString();
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
}
