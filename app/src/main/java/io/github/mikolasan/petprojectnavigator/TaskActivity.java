package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TaskActivity extends AppCompatActivity {

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
                db.addTask(e_name.getText().toString(),
                        e_links.getText().toString(),
                        e_desc.getText().toString(),
                        0,
                        0,
                        0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
