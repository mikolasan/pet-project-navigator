package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import static io.github.mikolasan.petprojectnavigator.Tools.createTaskIntent;

public class ProjectActivity extends FragmentActivity {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_EDIT = 1;

    PetDatabase petDatabase;
    private PetDataLoader<PetTaskLoader> activityDataLoader;

    EditText projectName;
    EditText projectDesc;
    Button btnDeleteProject;
    ListView taskView;

    private int status;
    private int projectId;

    private void setButtonListeners() {
        final Button btn_add_task = (Button) findViewById(R.id.btn_add_task);
        btn_add_task.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
                intent.putExtra("status", TaskActivity.STATUS_NEW);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });

        final Button btn_add_project = (Button) findViewById(R.id.btn_add_project);
        projectName = (EditText) findViewById(R.id.e_name);
        projectDesc = (EditText) findViewById(R.id.e_desc);
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
                }
                ProjectActivity.this.finish();
            }
        });

        btnDeleteProject = (Button) findViewById(R.id.btn_delete_project);
        btnDeleteProject.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                petDatabase.deleteProject(projectId);
                ProjectActivity.this.finish();
            }
        });

    }

    private void initTaskView() {
        taskView = (ListView) findViewById(R.id.task_view);
        taskView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = createTaskIntent(getApplicationContext(), taskView, i);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("status", ProjectActivity.STATUS_EDIT);
                intent.putExtra("project_id", projectId);
                startActivity(intent);
            }
        });
        Context context = getApplicationContext();
        try {
            activityDataLoader = new PetDataLoader<>(context, PetTaskLoader.class, new PetTaskLoader(context, petDatabase), taskView);
            Bundle args = new Bundle();
            args.putInt("project_id", projectId);
            getSupportLoaderManager().initLoader(activityDataLoader.projectActivityId, args, activityDataLoader);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    void updateTaskView() {
        if (activityDataLoader != null) {
            Bundle args = new Bundle();
            args.putInt("project_id", projectId);
            getSupportLoaderManager().restartLoader(activityDataLoader.projectActivityId, args, activityDataLoader);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        petDatabase = PetDatabase.getOpenedInstance();
        setButtonListeners();
        initTaskView();

        final BottomSheetBehavior bottomSheetBehavior;
        final View view = findViewById(R.id.bottom_sheet_task);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(120);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
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
            default: {
                break;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTaskView();
    }
}
