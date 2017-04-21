package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;

class PetProjectLoader extends PetAnyLoader {
    public PetProjectLoader(Context context, DB db) {
        super(context, db);
        this.setColumnNames(new String[] { DB.COLUMN_NAME, DB.COLUMN_DESC  });
        this.setLayoutItems(new int[] { R.id.lbl_title, R.id.lbl_desc });
        this.setLayoutId(R.layout.item_project);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = db.getAllProjects();
        return cursor;
    }
}
