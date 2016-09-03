package io.github.mikolasan.petprojectnavigator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
                intent.putExtra("status", 0); // status: new
                startActivity(intent);
            }
        });

    }
}
