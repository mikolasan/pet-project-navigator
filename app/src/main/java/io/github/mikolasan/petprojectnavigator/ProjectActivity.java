package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProjectActivity extends AppCompatActivity {

    public static final int STATUS_NEW = 0;
    DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        db = DB.getOpenedInstance();

        final Button btn_add_task = (Button) findViewById(R.id.btn_add_task);
        btn_add_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
                intent.putExtra("status", "new");
                startActivity(intent);
            }
        });

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        final EditText project_name = (EditText) findViewById(R.id.e_name);
        final EditText project_desc = (EditText) findViewById(R.id.e_desc);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.addProject(project_name.getText().toString(),
                        project_desc.getText().toString());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        int status = intent.getIntExtra("status", STATUS_NEW);
        if (status == STATUS_NEW) {
            ;
        } else {
            ;
            //TextView lbl = (TextView) findViewById(R.id.etTitle);
            //lbl.setText(id);
        }

    }

}
