package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;

class PetTaskLoader extends PetAnyLoader {
    public PetTaskLoader(Context context, PetDatabase petDatabase) {
        super(context, petDatabase);
        this.setColumnNames(new String[] { PetDatabase.COLUMN_NAME });
        this.setLayoutItems(new int[] { R.id.lbl_title });
        this.setLayoutId(R.layout.item_task);
    }

    @Override
    public Cursor loadInBackground() {
        boolean loadAll = super.getArgs().getBoolean("all_projects", false);
        if(loadAll) {
            return petDatabase.dbTask.getAll();
        } else {
            int projectId = super.getArgs().getInt("project_id", 0);
            return petDatabase.dbTask.getAllByProject(projectId);
        }
    }
}
