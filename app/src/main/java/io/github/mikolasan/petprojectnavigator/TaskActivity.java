package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TaskActivity extends AppCompatActivity {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    private int project_id;
    DB db;
    Button btn_save_task;
    EditText e_name;
    EditText e_desc;
    EditText e_links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        btn_save_task = (Button) findViewById(R.id.btn_save_task);
        e_name = (EditText) findViewById(R.id.e_name);
        e_desc = (EditText) findViewById(R.id.e_desc);
        e_links = (EditText) findViewById(R.id.e_links);

        db = DB.getOpenedInstance();

        btn_save_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.addTask(project_id,
                        e_name.getText().toString(),
                        e_links.getText().toString(),
                        e_desc.getText().toString(),
                        0,
                        0,
                        0);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        int status = intent.getIntExtra("status", STATUS_NEW);
        switch  (status) {
            case STATUS_NEW: {
                project_id = intent.getIntExtra("project_id", 0);
                break;
            }
            case STATUS_EDIT: {


                break;
            }
            default: {
                break;
            }
        }
    }


}
