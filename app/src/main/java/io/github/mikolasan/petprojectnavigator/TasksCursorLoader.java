package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

class TasksCursorLoader extends CursorLoader {
    private DB db;
    private int projectId;

    TasksCursorLoader(Context context, DB db) {
        super(context);
        this.db = db;
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
