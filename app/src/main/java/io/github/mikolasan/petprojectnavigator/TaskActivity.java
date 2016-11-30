package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TaskActivity extends AppCompatActivity {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    private int project_id;
    DB db;
    Button btn_save_task;
    EditText e_name;
    EditText e_desc;
    EditText e_links;

    SimpleCursorAdapter spinnerAdapter;
    private Spinner s_tech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        db = DB.getOpenedInstance();

        String[] from = new String[] { DB.COLUMN_NAME };
        int[] to = new int[] { android.R.id.text1 };
        int flags = 0;
        Cursor techCursor = db.getAllTech();
        spinnerAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                techCursor,
                from,
                to,
                flags);
        s_tech = (Spinner) findViewById(R.id.s_tech);
        s_tech.setAdapter(spinnerAdapter);

        btn_save_task = (Button) findViewById(R.id.btn_save_task);
        e_name = (EditText) findViewById(R.id.e_name);
        e_desc = (EditText) findViewById(R.id.e_desc);
        e_links = (EditText) findViewById(R.id.e_links);



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

        s_tech.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
                int index = cursor.getColumnIndex(DB.COLUMN_ID);
                int tech_id = cursor.getInt(index);
                if (tech_id == DB.TECH_UNDEFINED_ID) {
                    
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

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
