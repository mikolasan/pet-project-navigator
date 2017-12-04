package io.github.mikolasan.petprojectnavigator;

import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by neupo on 6/23/2017.
 */

public class PetDriveCommunicator implements ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = "drive-quickstart";
    private GoogleApiClient mGoogleApiClient;
    private Activity activity;
    private String json;

    public PetDriveCommunicator(Activity activity) {
        this.activity = activity;
    }

    // Checks if the app has permission to write to device storage
    // If the app does not has permission then the user will be prompted to grant permissions
    protected void verifyStoragePermissions(String permission) {
        // Check if we have write permission
        int status = ContextCompat.checkSelfPermission(activity, permission);
        if (status != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[] {permission}, MainActivity.REQUEST_EXTERNAL_STORAGE);
        }
    }

    protected void pause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void toCloud(String json) {
        this.json = json;
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API client connected.");
        saveFileToDrive();
    }

    private String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss", Locale.US);
        String date = simpleDateFormat.format(new Date());
        return "pet-project-navigator-backup" + date + ".json";
    }

    private class DriveCommunicatorResult implements ResultCallback<DriveApi.DriveContentsResult> {

        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            // If the operation was not successful, we cannot do anything and must fail.
            if (!result.getStatus().isSuccess()) {
                Log.i(TAG, "Failed to create new contents.");
                return;
            }
            // Otherwise, we can write our data to the new contents.
            Log.i(TAG, "New contents created.");
            // Get an output stream for the contents.
            try {
                result.getDriveContents()
                        .getOutputStream()
                        .write(json.getBytes(Charset.forName("UTF-8")));
            } catch (IOException e1) {
                Log.i(TAG, "Unable to write file contents.");
                return;
            }
            // Create the initial metadata - MIME type and title.
            // Note that the user will be able to change the title later.
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setMimeType("application/json").setTitle(getFileName()).build();
            // Create an intent for the file chooser, and start it.
            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialDriveContents(result.getDriveContents())
                    .build(mGoogleApiClient);
            try {
                activity.startIntentSenderForResult(
                        intentSender, MainActivity.REQUEST_CODE_CREATOR, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Failed to launch file chooser.");
            }
        }
    }

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new DriveCommunicatorResult());
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
            GoogleApiAvailability.getInstance().getErrorDialog(activity, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(activity, MainActivity.REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

}
