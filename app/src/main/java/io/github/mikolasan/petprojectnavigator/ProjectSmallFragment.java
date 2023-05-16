package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import static io.github.mikolasan.petprojectnavigator.Tools.restartLoader;

/**
 * Created by neupo on 4/25/2017.
 */

public class ProjectSmallFragment extends Fragment {
    PetDatabase petDatabase;
    public PetDataLoader<PetProjectLoader> activityDataLoader;

    private void setButtonListeners(View v) {
        final Button btn_add_project = (Button) v.findViewById(R.id.btn_add_project);
        btn_add_project.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), ProjectFragment.class);
            intent.putExtra("status", ProjectFragment.STATUS_NEW);
            startActivity(intent);
        });
    }

    private void initView(Context context, View v) {
        final DragSortListView list = (DragSortListView) v.findViewById(R.id.project_view);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            Cursor c = (Cursor) list.getItemAtPosition(i);

            int id_column = c.getColumnIndex(PetDatabase.COLUMN_ID);
            int projectId = c.getInt(id_column);
            int name_column = c.getColumnIndex(PetDatabase.COLUMN_NAME);
            int desc_column = c.getColumnIndex(PetDatabase.COLUMN_DESC);

            Intent intent = new Intent(context, ProjectFragment.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("status", ProjectFragment.STATUS_EDIT);
            intent.putExtra("project_id", projectId);
            intent.putExtra("title", c.getString(name_column));
            intent.putExtra("description", c.getString(desc_column));
            startActivity(intent);
        });

        try {
            activityDataLoader = new PetDataLoader<>(context, new PetProjectLoader(context, petDatabase), list);
            getLoaderManager().initLoader(PetDataLoader.mainActivityId, null, activityDataLoader);
            list.setDropListener((from, to) -> {
                if (from != to) {
                    activityDataLoader.onDropAction(from, to);
                    Toast.makeText(getActivity(), "End - position: " + to, Toast.LENGTH_SHORT).show();
                }
            });

            list.setRemoveListener(which -> {
                activityDataLoader.onRemoveAction(which);
                Toast.makeText(getActivity(), "Remove - position: " + which, Toast.LENGTH_SHORT).show();
            });

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
        petDatabase = PetDatabase.getOpenedInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_project_list, container, false);
        initView(getActivity(), v);
        setButtonListeners(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader(this, activityDataLoader, null);
    }
}
