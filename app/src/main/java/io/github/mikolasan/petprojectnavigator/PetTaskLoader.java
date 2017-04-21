package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;

class PetTaskLoader extends PetAnyLoader {
    private int projectId;

    PetTaskLoader(Context context, DB db) {
        super(context, db);
        this.setColumnNames(new String[] { DB.COLUMN_NAME });
        this.setLayoutItems(new int[] { R.id.lbl_title });
        this.setLayoutId(R.layout.item_task);
        this.projectId = 0;
    }

    void setProjectId(int id) {
        projectId = id;
    }

    @Override
    public Cursor loadInBackground() {
        return db.dbTask.getAll(projectId);
    }
}
