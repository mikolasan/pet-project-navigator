package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ProjectActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    DB db;
    SimpleCursorAdapter cursorAdapter;
    ProjectActivity.MyCursorLoader mCursorLoader;

    EditText project_name;
    EditText project_desc;
    ListView taskView;

    private int project_id;

    private int getNumber(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id < 0) {
            return 0;
        }
        return cursor.getInt(id);
    }

    private String getString(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id < 0) {
            return "";
        }
        return cursor.getString(id);
    }

    private Intent createIntent(int selectedItem) {
        Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
        Cursor c = (Cursor) taskView.getItemAtPosition(selectedItem);
        intent.putExtra("task_id", getNumber(c, DB.COLUMN_ID));
        intent.putExtra("title", getString(c, DB.COLUMN_NAME));
        intent.putExtra("time", getNumber(c, DB.COLUMN_TIME));
        intent.putExtra("tech_id", getNumber(c, DB.COLUMN_TECH_ID));
        intent.putExtra("statement", getString(c, DB.COLUMN_STATEMENT));
        intent.putExtra("links", getString(c, DB.COLUMN_LINKS));
        intent.putExtra("type_id", getNumber(c, DB.COLUMN_TYPE_ID));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        db = DB.getOpenedInstance();

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
                db.addProject(project_name.getText().toString(),
                        project_desc.getText().toString());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        String[] from = new String[] { DB.COLUMN_NAME };
        int[] to = new int[] { R.id.lbl_title };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_task, null, from, to, 0);
        taskView = (ListView) findViewById(R.id.task_view);
        taskView.setAdapter(cursorAdapter);

        taskView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createIntent(i);
                intent.putExtra("status", ProjectActivity.STATUS_EDIT);
                intent.putExtra("project_id", project_id);
                startActivity(intent);
            }
        });

        // добавляем контекстное меню к списку
        //registerForContextMenu(list);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        int status = intent.getIntExtra("status", STATUS_NEW);
        switch  (status) {
            case STATUS_NEW: {
                break;
            }
            case STATUS_EDIT: {
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
            return db.getAllTasks(project_id);
        }

    }
}
