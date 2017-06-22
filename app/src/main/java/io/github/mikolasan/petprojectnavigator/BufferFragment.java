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
import static io.github.mikolasan.petprojectnavigator.Tools.initLoader;
import static io.github.mikolasan.petprojectnavigator.Tools.restartLoader;

/**
 * Created by neupo on 4/25/2017.
 */

public class BufferFragment extends Fragment {

    public PetDataLoader<PetTaskLoader> activityDataLoader;

    private void initView(Context context, View v) {
        final ListView list = (ListView) v.findViewById(R.id.task_buffer_view);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = createTaskIntent(context, list, i);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("status", TaskActivity.STATUS_EDIT);
            startActivity(intent);
        });
        try {
            activityDataLoader = new PetDataLoader<>(context, new PetTaskLoader(context, PetDatabase.getOpenedInstance()), list);
            Bundle args = new Bundle();
            args.putBoolean("all_projects", false);
            args.putInt("project_id", 0);
            initLoader(this, activityDataLoader, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buffer, container, false);
        initView(getActivity(), v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = new Bundle();
        args.putBoolean("all_projects", false);
        args.putInt("project_id", 0);
        restartLoader(this, activityDataLoader, args);
    }

}
