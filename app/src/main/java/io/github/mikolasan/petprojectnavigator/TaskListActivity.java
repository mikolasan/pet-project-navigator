package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createIntent;

public class TaskListActivity extends FragmentActivity {

    DB db;
    private PetDataLoader<PetTaskLoader> petDataLoader;
    private int projectId;

    private void initView() {
        final ListView list = (ListView) findViewById(R.id.task_full_view);
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
        Context context = getApplicationContext();
        petDataLoader = new PetDataLoader<>(context, new PetTaskLoader(context, db), list);
        getLoaderManager().initLoader(0, null, petDataLoader);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        db = DB.getOpenedInstance();
        projectId = 0;
        initView();
        getLoaderManager().initLoader(0, null, petDataLoader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getLoaderManager().restartLoader(projectId, null, petDataLoader);
    }
}
