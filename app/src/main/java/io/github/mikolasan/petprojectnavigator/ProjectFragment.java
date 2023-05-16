package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.URISyntaxException;

import static io.github.mikolasan.petprojectnavigator.Tools.createTaskIntent;

public class ProjectFragment extends Fragment {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;
    public static final int STATUS_BUFFER = 2;

    PetDatabase petDatabase;
    private PetDataLoader<PetTaskLoader> activityDataLoader;

    EditText projectName;
    EditText projectDesc;
    Button btnDeleteProject;
    ListView taskView;

    private int status;
    private int projectId;

    private void setButtonListeners(View v) {
        final Button btn_add_task = (Button) v.findViewById(R.id.btn_add_task);
        btn_add_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TaskFragment.class);
                intent.putExtra("status", TaskFragment.STATUS_NEW);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });

        final Button btn_add_project = (Button) v.findViewById(R.id.btn_add_project);
        projectName = (EditText) v.findViewById(R.id.e_name);
        projectDesc = (EditText) v.findViewById(R.id.e_desc);
        btn_add_project.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = projectName.getText().toString();
                String description = projectDesc.getText().toString();
                switch (status) {
                    case STATUS_NEW:
                    {
                        petDatabase.addProject(name, description);
                        break;
                    }
                    case STATUS_EDIT:
                    {
                        petDatabase.saveProjectDetails(projectId, name, description);
                        break;
                    }
                    case STATUS_BUFFER:
                    {
                        petDatabase.projectFromBuffer(name, description);
                        break;
                    }
                }
//                ProjectActivity.this.finish();
            }
        });

        btnDeleteProject = (Button) v.findViewById(R.id.btn_delete_project);
        btnDeleteProject.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                petDatabase.deleteProject(projectId);
//                ProjectActivity.this.finish();
            }
        });

    }

    private void initTaskView(View v) {
        taskView = (ListView) v.findViewById(R.id.task_view);
        taskView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createTaskIntent(v.getContext(), taskView, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", ProjectFragment.STATUS_EDIT);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });
        Context context = v.getContext();
        try {
            activityDataLoader = new PetDataLoader<>(context, new PetTaskLoader(context, petDatabase), taskView);
            Bundle args = new Bundle();
            args.putBoolean("all_projects", false);
            args.putInt("project_id", projectId);
//            getSupportLoaderManager().initLoader(activityDataLoader.projectActivityId, args, activityDataLoader);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    void updateTaskView() {
        if (activityDataLoader != null) {
            Bundle args = new Bundle();
            args.putBoolean("all_projects", false);
            args.putInt("project_id", projectId);
//            getSupportLoaderManager().restartLoader(activityDataLoader.projectActivityId, args, activityDataLoader);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.project_fragment, container, false);

        petDatabase = PetDatabase.getOpenedInstance();
        setButtonListeners(v);
        initTaskView(v);

        final BottomSheetBehavior bottomSheetBehavior;
        final View view = v.findViewById(R.id.bottom_sheet_task);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(120);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        // TODO !!!
        Intent intent = null;
        try {
            intent = Intent.getIntent("");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        status = intent.getIntExtra("status", STATUS_NEW);
        switch  (status) {
            case STATUS_NEW: {
                btnDeleteProject.setVisibility(View.INVISIBLE);
                break;
            }
            case STATUS_EDIT: {
                btnDeleteProject.setVisibility(View.VISIBLE);
                projectName.setText(intent.getStringExtra("title"));
                projectDesc.setText(intent.getStringExtra("description"));
                projectId = intent.getIntExtra("project_id", 0);
                updateTaskView();
                break;
            }
            case STATUS_BUFFER: {
                btnDeleteProject.setVisibility(View.INVISIBLE);
                projectName.setText(R.string.sample_buffer_title);
                projectDesc.setText(R.string.sample_buffer_desc);
                projectId = 0;
                updateTaskView();
                break;
            }
            default: {
                break;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateTaskView();
    }
}
