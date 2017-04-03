package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createIntent;

public class TaskListActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DB db;
    SimpleCursorAdapter cursorAdapter;
    TasksCursorLoader tasksCursorLoader;
    private int projectId;

    private void initView() {
        final ListView list = (ListView) findViewById(R.id.task_full_view);
        list.setAdapter(cursorAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createIntent(getApplicationContext(), list, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", ProjectActivity.STATUS_EDIT);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        db = DB.getOpenedInstance();
        projectId = 0;
        String[] from = new String[] { DB.COLUMN_NAME, DB.COLUMN_DESC  };
        int[] to = new int[] { R.id.lbl_title, R.id.lbl_desc };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_project, null, from, to, 0);
        initView();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(projectId, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        tasksCursorLoader = new TasksCursorLoader(this, db);
        tasksCursorLoader.setProjectId(id);
        return tasksCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.changeCursor(null);
    }

}
