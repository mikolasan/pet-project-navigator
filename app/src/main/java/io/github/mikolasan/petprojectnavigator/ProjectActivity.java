package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createIntent;

public class ProjectActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    DB db;
    SimpleCursorAdapter cursorAdapter;
    ProjectActivity.MyCursorLoader mCursorLoader;

    EditText project_name;
    EditText project_desc;
    Button btn_delete_project;
    ListView taskView;

    private int status;
    private int project_id;

    private void setButtonListeners() {
        final Button btn_add_task = (Button) findViewById(R.id.btn_add_task);
        btn_add_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
                intent.putExtra("status", TaskActivity.STATUS_NEW);
                intent.putExtra("project_id", project_id);
                startActivity(intent);
            }
        });

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        project_name = (EditText) findViewById(R.id.e_name);
        project_desc = (EditText) findViewById(R.id.e_desc);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = project_name.getText().toString();
                String description = project_desc.getText().toString();
                switch (status) {
                    case STATUS_NEW:
                    {
                        db.addProject(name, description);
                        break;
                    }
                    case STATUS_EDIT:
                    {
                        db.saveProjectDetails(project_id, name, description);
                        break;
                    }
                }
                ProjectActivity.this.finish();
            }
        });

        btn_delete_project = (Button) findViewById(R.id.btn_delete_project);
        btn_delete_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.deleteProject(project_id);
                ProjectActivity.this.finish();
            }
        });

    }

    private void initTaskView(SimpleCursorAdapter cursorAdapter) {
        taskView = (ListView) findViewById(R.id.task_view);
        taskView.setAdapter(cursorAdapter);
        taskView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createIntent(getApplicationContext(), taskView, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", ProjectActivity.STATUS_EDIT);
                intent.putExtra("project_id", project_id);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        db = DB.getOpenedInstance();
        setButtonListeners();
        String[] from = new String[] { DB.COLUMN_NAME };
        int[] to = new int[] { R.id.lbl_title };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_task, null, from, to, 0);
        initTaskView(cursorAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        status = intent.getIntExtra("status", STATUS_NEW);
        switch  (status) {
            case STATUS_NEW: {
                btn_delete_project.setVisibility(View.INVISIBLE);
                break;
            }
            case STATUS_EDIT: {
                btn_delete_project.setVisibility(View.VISIBLE);
                project_name.setText(intent.getStringExtra("title"));
                project_desc.setText(intent.getStringExtra("description"));
                project_id = intent.getIntExtra("project_id", 0);
                break;
            }
            default: {
                break;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(project_id, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mCursorLoader = new ProjectActivity.MyCursorLoader(this, db);
        mCursorLoader.setProjectId(id);
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.changeCursor(null);
    }

    static class MyCursorLoader extends CursorLoader {

        DB db;
        int project_id;

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
            this.project_id = 0;
        }

        public void setProjectId(int id) {
            project_id = id;
        }

        @Override
        public Cursor loadInBackground() {
            return db.dbTask.getAll(project_id);
        }

    }
}
