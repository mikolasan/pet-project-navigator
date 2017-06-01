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

    PetDatabase petDatabase;
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
            activityDataLoader = new PetDataLoader<>(context, new PetTaskLoader(context, petDatabase), list);
            initLoader(null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void applyQuery(String query) {
        Bundle args = new Bundle();
        args.putString("query", query);
        restartLoader(args);
    }

    public void updateList() {
        restartLoader(null);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        petDatabase = PetDatabase.getOpenedInstance();
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
        restartLoader(null);
    }

    private Bundle checkLoaderArgs(Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean("all_projects", true);
        return args;
    }

    private void initLoader(Bundle args) {
        getLoaderManager().initLoader(PetDataLoader.tasksActivityId,
                checkLoaderArgs(args),
                activityDataLoader);
    }

    private void restartLoader(Bundle args) {
        getLoaderManager().restartLoader(PetDataLoader.tasksActivityId,
                checkLoaderArgs(args),
                activityDataLoader);
    }
}
