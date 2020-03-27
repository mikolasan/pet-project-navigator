package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
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

    static Bundle checkLoaderArgs(Bundle args) {
        if (args == null) {
            args = new Bundle();
            args.putBoolean("all_projects", true);
        }
        return args;
    }

    static void initLoader(Fragment fragment, PetDataLoader loader, Bundle args) {
        fragment.getLoaderManager().initLoader(PetDataLoader.tasksActivityId,
                checkLoaderArgs(args),
                loader);
    }

    static void restartLoader(Fragment fragment, PetDataLoader loader, Bundle args) {
        fragment.getLoaderManager().restartLoader(PetDataLoader.tasksActivityId,
                checkLoaderArgs(args),
                loader);
    }

    static void applyQuery(Fragment fragment, PetDataLoader loader, int criterion, String query) {
        Bundle args = new Bundle();
        args.putString("query", query);
        if (criterion != 0) args.putInt("criterion", criterion);
        restartLoader(fragment, loader, args);
    }

    static String getOrSelection(String column, int n) {
        String selection = column + " = ?";
        if (n > 1) {
            selection = "(" + selection;
            int i = 1;
            while (i++ < n) {
                selection += " or " + column + " = ?";
            }
            selection += ")";
        }
        return selection;
    }
}
