package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.ListView;

/**
 * Created by neupo on 4/2/2017.
 */

class Tools {
    static int getNumber(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id < 0) {
            return 0;
        }
        return cursor.getInt(id);
    }

    static String getString(Cursor cursor, String column) {
        int id = cursor.getColumnIndex(column);
        if (id < 0) {
            return "";
        }
        return cursor.getString(id);
    }

    static Intent parseTaskItem(Intent intent, Cursor c) {
        intent.putExtra("task_id", getNumber(c, PetDatabase.COLUMN_ID));
        intent.putExtra("title", getString(c, PetDatabase.COLUMN_NAME));
        intent.putExtra("time", getNumber(c, PetDatabase.COLUMN_TIME));
        intent.putExtra("tech_id", getNumber(c, PetDatabase.COLUMN_TECH_ID));
        intent.putExtra("statement", getString(c, PetDatabase.COLUMN_STATEMENT));
        intent.putExtra("links", getString(c, PetDatabase.COLUMN_LINKS));
        intent.putExtra("type_id", getNumber(c, PetDatabase.COLUMN_TYPE_ID));
        return intent;
    }

    static Intent createTaskIntent(Context context, ListView view, int selectedItem) {
        Intent intent = new Intent(context, TaskActivity.class);
        Cursor c = (Cursor) view.getItemAtPosition(selectedItem);
        parseTaskItem(intent, c);
        return intent;
    }
}
