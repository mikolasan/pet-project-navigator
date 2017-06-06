package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

class PetProjectLoader extends PetAnyLoader {
    private static final String TASK_COUNTER_COLUMN = "counter";
    private static final String TOTAL_TIME_COLUMN = "time";
    private static final String TECHNOLOGY_COLUMN = "tech";

    public PetProjectLoader(Context context, PetDatabase petDatabase) {
        super(context, petDatabase);
        this.setColumnNames(new String[] { PetDatabase.COLUMN_NAME, PetDatabase.COLUMN_DESC, TASK_COUNTER_COLUMN, TOTAL_TIME_COLUMN, TECHNOLOGY_COLUMN });
        this.setLayoutItems(new int[] { R.id.lbl_title, R.id.lbl_desc, R.id.lbl_task_counter, R.id.lbl_time, R.id.lbl_tech });
        this.setLayoutId(R.layout.item_project);
    }

    @Override
    public Cursor loadInBackground() {
        Bundle args = super.getArgs();
        String query = "";
        if (args != null && !args.isEmpty()) {
            query = super.getArgs().getString("query", "");
        }
        if (query.isEmpty()) {
            return petDatabase.getAllProjects();
        } else {
            int criterion = super.getArgs().getInt("criterion", 0);
            switch (criterion) {
                case R.id.criterion_name:
                    return petDatabase.getAllProjectsByName(query);
                case R.id.criterion_desc:
                    return petDatabase.getAllProjectsByDesc(query);
                case R.id.criterion_time:
                    return petDatabase.getAllProjectsByTime(query);
                case R.id.criterion_tech:
                default:
                    return petDatabase.getAllProjectByTech(query);
            }
        }
    }

    @Override
    protected void fillView(View view, Context context, Cursor cursor) {
        int id = cursor.getColumnIndex(PetDatabase.COLUMN_ID);
        int projectId = cursor.getInt(id);
        Cursor tasks = petDatabase.dbTask.getAllByProject(projectId);
        int totalTime = 0;
        String technology = "";
        while(tasks.moveToNext()) {
            totalTime += tasks.getInt(tasks.getColumnIndex(DBTask.COLUMN_TIME));
            int techId = tasks.getInt(tasks.getColumnIndex(DBTask.COLUMN_TECH_ID));
            technology += petDatabase.getTechName(techId) + ";";
        }
        String time = String.valueOf(totalTime) + " hour";
        if (totalTime > 1) time += "s";

        String task_counter = String.valueOf(tasks.getCount()) + " task";
        if (tasks.getCount() > 1) task_counter += "s";


        ((TextView) view.findViewById(R.id.lbl_title)).setText(
                cursor.getString(cursor.getColumnIndex(PetDatabase.COLUMN_NAME))
        );
        ((TextView) view.findViewById(R.id.lbl_desc)).setText(
                cursor.getString(cursor.getColumnIndex(PetDatabase.COLUMN_DESC))
        );
        ((TextView) view.findViewById(R.id.lbl_task_counter)).setText(
                task_counter
        );
        ((TextView) view.findViewById(R.id.lbl_time)).setText(
                time
        );
        ((TextView) view.findViewById(R.id.lbl_tech)).setText(
                technology
        );
    }
}
