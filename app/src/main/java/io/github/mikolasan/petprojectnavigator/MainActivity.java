package io.github.mikolasan.petprojectnavigator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static io.github.mikolasan.petprojectnavigator.Tools.applyQuery;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    PetDatabase petDatabase;
    private static final String TAG = "drive-quickstart";
    private static final String OPEN_FILE_TAG = "open-file-dialog";
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_RESTORE_FILE = 4;
    private static final int REQUEST_EXTERNAL_STORAGE = 5;
    private GoogleApiClient mGoogleApiClient;

    private static final int PROJECTS_PAGE_ID = 0;
    private static final int TASKS_PAGE_ID = 1;
    private static final int BUFFER_PAGE_ID = 2;
    private static final int N_PAGES = 3;
    private int currentPage = 0;
    private int criterion = 0;
    private ProjectFragment projectFragment;
    private TaskListActivity taskFragment;
    private BufferFragment bufferFragment;

    PetPagerAdapter pagerAdapter;
    ViewPager pager;
    ListView listView;
    BottomNavigationView bottomNavigationView;
    SearchView searchView;
    ArrayList<String> searchPerPage;
    MenuItem prevMenuItem;

    private final int drawerOpenProjectsPos = 0;
    private final int drawerOpenTasksPos = 1;
    private final int drawerOpenBufferPos = 2;
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
            switch (position) {
                case drawerAddProjectPos:
                    Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                    intent.putExtra("status", ProjectActivity.STATUS_NEW);
                    startActivity(intent);
                    break;
                case drawerOpenProjectsPos:
                    selectPage(PROJECTS_PAGE_ID, position);
                    break;
                case drawerOpenTasksPos:
                    selectPage(TASKS_PAGE_ID, position);
                    break;
                case drawerOpenBufferPos:
                    selectPage(BUFFER_PAGE_ID, position);
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
            drawerLayout.closeDrawer(listView);
        });
    }

    private void selectPage(int pageId, int listItemId) {
        pager.setCurrentItem(pageId);
        listView.setItemChecked(listItemId, true);
        if (prevMenuItem != null) {
            prevMenuItem.setChecked(false);
        } else {
            bottomNavigationView.getMenu().getItem( 0).setChecked(false);
        }
        bottomNavigationView.getMenu().getItem(pageId).setChecked(true);
        prevMenuItem = bottomNavigationView.getMenu().getItem(pageId);

        currentPage = pageId;
        if (searchView != null && searchPerPage.size() > pageId) {
            searchView.setQuery(searchPerPage.get(pageId), false);
        }
    }

    private int getCurrentPage() {
        return pager.getCurrentItem();
    }

    private void setButtonListeners() {
        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_projects:
                            selectPage(PROJECTS_PAGE_ID, drawerOpenProjectsPos);
                            break;
                        case R.id.action_tasks:
                            selectPage(TASKS_PAGE_ID, drawerOpenTasksPos);
                            break;
                        case R.id.action_buffer:
                            selectPage(BUFFER_PAGE_ID, drawerOpenBufferPos);
                            break;
                        default:
                            break;
                    }
                    return true;
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchPerPage = new ArrayList<>(N_PAGES);
        petDatabase = PetDatabase.getOpenedInstance();
        setContentView(R.layout.activity_main);
        setButtonListeners();
        initDrawer();

        projectFragment = new ProjectFragment();
        taskFragment = new TaskListActivity();
        bufferFragment = new BufferFragment();

        pagerAdapter = new PetPagerAdapter(getSupportFragmentManager());

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case PROJECTS_PAGE_ID:
                        selectPage(position, drawerOpenProjectsPos);
                        break;
                    case TASKS_PAGE_ID:
                        selectPage(position, drawerOpenTasksPos);
                        break;
                    case BUFFER_PAGE_ID:
                        selectPage(position, drawerOpenBufferPos);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

        final Toolbar petToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(petToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Configure the search info and add any event listeners...
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int page = getCurrentPage();
                searchPerPage.set(page, newText);
                switch (page){
                    case PROJECTS_PAGE_ID:
                        applyQuery(projectFragment, projectFragment.activityDataLoader, criterion, newText);
                        break;
                    case TASKS_PAGE_ID:
                        applyQuery(taskFragment, taskFragment.activityDataLoader, criterion, newText);
                        break;
                    case BUFFER_PAGE_ID:
                        //applyQuery(bufferFragment, bufferFragment.activityDataLoader, criterion, newText);
                        break;
                }
                return true;
            }
        });

        // Define the listener
        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchPerPage.clear();
                        applyQuery(projectFragment, projectFragment.activityDataLoader, 0, "");
                        applyQuery(taskFragment, taskFragment.activityDataLoader, 0, "");
                        //applyQuery(bufferFragment, bufferFragment.activityDataLoader, 0, "");
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchPerPage.add("");
                        searchPerPage.add("");
                        searchPerPage.add("");
                        searchView.setQuery("", true);
                        return true;
                    default:
                        return false;
                }
            }
        };

        // Assign the listener to that action item
        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);

        return super.onCreateOptionsMenu(menu);
    }

    private void setCriterion(int criterion) {
        this.criterion = criterion;
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
                setCriterion(item.getItemId());
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private class PetPagerAdapter extends FragmentPagerAdapter {

        private PetPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            // Do NOT try to save references to the Fragments in getItem(),
            // because getItem() is not always called. If the Fragment
            // was already created then it will be retrieved from the FragmentManger
            // and not here (i.e. getItem() won't be called again).
            switch (position) {
                case PROJECTS_PAGE_ID:
                    return projectFragment;
                case TASKS_PAGE_ID:
                    return taskFragment;
                case BUFFER_PAGE_ID:
                    return bufferFragment;
                default:
                    // This should never happen. Always account for each position above
                    return null;
            }
        }

        @Override
        public int getCount() {
            return N_PAGES;
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
                        readBackupFile(data.getData());
                    } else {
                        Toast.makeText(getApplicationContext(), "Bad data", Toast.LENGTH_SHORT).show();
                    }
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
                if(petDatabase.restore(json)){
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
