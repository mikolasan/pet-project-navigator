package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createTaskIntent;

public class TaskListActivity extends Fragment {

    DB db;
    private PetDataLoader<PetTaskLoader> activityDataLoader;

    private void initView(Context context, View v) {
        final ListView list = (ListView) v.findViewById(R.id.task_full_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createTaskIntent(context, list, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", TaskActivity.STATUS_EDIT);
                startActivity(intent);
            }
        });
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
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DB.getOpenedInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_task_list, container, false);
        initView(getActivity(), v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = new Bundle();
        args.putBoolean("all_projects", true);
        getLoaderManager().restartLoader(activityDataLoader.tasksActivityId, args, activityDataLoader);
    }
}
