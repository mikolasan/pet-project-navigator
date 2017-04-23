package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createTaskIntent;

public class TaskListActivity extends FragmentActivity {

    DB db;
    private PetDataLoader<PetTaskLoader> activityDataLoader;

    private void initView() {
        final ListView list = (ListView) findViewById(R.id.task_full_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createTaskIntent(getApplicationContext(), list, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", TaskActivity.STATUS_EDIT);
                startActivity(intent);
            }
        });
        Context context = getApplicationContext();
        try {
            activityDataLoader = new PetDataLoader<>(context, PetTaskLoader.class, new PetTaskLoader(context, db), list);
            Bundle args = new Bundle();
            args.putBoolean("all_projects", true);
            getLoaderManager().initLoader(activityDataLoader.tasksActivityId, args, activityDataLoader);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        db = DB.getOpenedInstance();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle args = new Bundle();
        args.putBoolean("all_projects", true);
        getLoaderManager().restartLoader(activityDataLoader.tasksActivityId, args, activityDataLoader);
    }
}
