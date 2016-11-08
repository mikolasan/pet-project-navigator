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
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DB db;
    SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DB.getOpenedInstance();

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", ProjectActivity.STATUS_NEW);
                startActivity(intent);
            }
        });

        String[] from = new String[] { DB.COLUMN_NAME  };
        int[] to = new int[] { R.id.lbl_title };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.item_project, null, from, to, 0);
        final ListView list = (ListView) findViewById(R.id.project_view);
        list.setAdapter(cursorAdapter);

        /*
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor) list.getItemAtPosition(i);
                int column = c.getColumnIndex("txt");
                String selectedFromList = c.getString(column);
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("id", 1);
                intent.putExtra("title", selectedFromList);
                startActivity(intent);
            }
        });
        */

        // добавляем контекстное меню к списку
        //registerForContextMenu(list);

        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
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
}
