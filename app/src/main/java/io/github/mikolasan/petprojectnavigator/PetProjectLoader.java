package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;
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
        return petDatabase.getAllProjects();
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
