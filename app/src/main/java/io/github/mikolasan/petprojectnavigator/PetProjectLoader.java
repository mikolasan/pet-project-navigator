package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;

class PetProjectLoader extends PetAnyLoader {
    public PetProjectLoader(Context context, PetDatabase petDatabase) {
        super(context, petDatabase);
        this.setColumnNames(new String[] { PetDatabase.COLUMN_NAME, PetDatabase.COLUMN_DESC  });
        this.setLayoutItems(new int[] { R.id.lbl_title, R.id.lbl_desc });
        this.setLayoutId(R.layout.item_project);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = petDatabase.getAllProjects();
        return cursor;
    }
}
