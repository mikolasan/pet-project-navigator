package io.github.mikolasan.petprojectnavigator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.FileNotFoundException;

import static io.github.mikolasan.petprojectnavigator.BackupManager.readBackupFile;

// If you want the backported Material Design look, use AppCompatActivity
public class MainActivity extends AppCompatActivity {

    private PetDatabase petDatabase;
    private PetDriveCommunicator petDriveCommunicator;
    private PetMainPager mainPager;

    public static final int REQUEST_CODE_CREATOR = 2;
    public static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_RESTORE_FILE = 4;
    public static final int REQUEST_EXTERNAL_STORAGE = 5;
    private final int drawerBackupPos = 3;
    private final int drawerRestorePos = 4;
    private final int drawerToCloudPos = 5;
    private final int drawerFromCloudPos = 6;
    private final int drawerAddProjectPos = 7;

    private class MainOnItemClickListener implements PetOnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mainPager.defineAction(position);
            switch (position) {
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
                    petDriveCommunicator.toCloud(petDatabase.prepareJson());
                    break;
                case drawerFromCloudPos:
                    restoreDB();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        petDatabase = PetDatabase.getOpenedInstance();
        petDriveCommunicator = new PetDriveCommunicator(this);
        setContentView(R.layout.activity_main);
        mainPager = new PetMainPager(this);
        mainPager.setButtonListeners(this);
        mainPager.setOnItemClickListener(new MainOnItemClickListener());

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
            intent.putExtra("status", ProjectActivity.STATUS_BUFFER);
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
        petDriveCommunicator.verifyStoragePermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
        petDatabase = PetDatabase.getOpenedInstance();
    }

    @Override
    protected void onPause() {
        petDriveCommunicator.pause();
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        petDatabase.close();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Saved on Google drive", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_RESTORE_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    readAndRestore(data);
                } else {
                    Toast.makeText(getApplicationContext(), "Bad data", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void readAndRestore(final Intent data) {
        // Get the URI of the selected file
        try {
            String json = readBackupFile(this, data.getData());
            if (petDatabase.restore(json)) {
                Toast.makeText(this, "DB restored", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to restore", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void backup() {
        saveLocalCopy(petDatabase.prepareJson());
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
