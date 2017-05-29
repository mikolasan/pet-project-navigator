package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

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

    CursorAdapter createAdapter(Cursor cursor) {
        return new SimpleCursorAdapter(getContext(), getLayoutId(), null, getColumnNames(), getLayoutItems(), 0);
    }
}
