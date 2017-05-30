package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

class PetTaskLoader extends PetAnyLoader {
    public PetTaskLoader(Context context, PetDatabase petDatabase) {
        super(context, petDatabase);
        this.setColumnNames(new String[] { PetDatabase.COLUMN_NAME, PetDatabase.COLUMN_TIME, PetDatabase.COLUMN_TECH_ID });
        this.setLayoutItems(new int[] { R.id.lbl_title, R.id.lbl_time, R.id.lbl_tech });
        this.setLayoutId(R.layout.item_task);
    }

    @Override
    public Cursor loadInBackground() {
        boolean loadAll = super.getArgs().getBoolean("all_projects", false);
        if(loadAll) {
            String query = super.getArgs().getString("query", "");
            if (query.isEmpty()) {
                return petDatabase.dbTask.getAll();
            } else {
                return petDatabase.dbTask.getAllByTech(query);
            }
        } else {
            int projectId = super.getArgs().getInt("project_id", 0);
            return petDatabase.dbTask.getAllByProject(projectId);
        }
    }

    @Override
    protected void fillView(View view, Context context, Cursor cursor) {
        int totalTime = cursor.getInt(cursor.getColumnIndex(DBTask.COLUMN_TIME));
        String time = String.valueOf(totalTime) + " hour";
        if (totalTime > 1) time += "s";

        int techId = cursor.getInt(cursor.getColumnIndex(DBTask.COLUMN_TECH_ID));
        String technology = petDatabase.getTechName(techId) + ";";

        ((TextView) view.findViewById(R.id.lbl_title)).setText(
                cursor.getString(cursor.getColumnIndex(PetDatabase.COLUMN_NAME))
        );
        ((TextView) view.findViewById(R.id.lbl_time)).setText(
                time
        );
        ((TextView) view.findViewById(R.id.lbl_tech)).setText(
                technology
        );
    }
}
