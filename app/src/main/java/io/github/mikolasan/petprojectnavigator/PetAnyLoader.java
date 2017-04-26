package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;

class PetAnyLoader extends CursorLoader {
    DB db;
    private String[] columnNames;
    private int[] layoutItems;
    private int layoutId;
    int id;
    Bundle args;

    PetAnyLoader(Context context, DB db) {
        super(context);
        this.db = db;
    }

    void onCreate(int id, Bundle args) {
        setId(id);
        setArgs(args);
    }

    String[] getColumnNames() {
        return columnNames;
    }

    int[] getLayoutItems() {
        return layoutItems;
    }

    int getLayoutId() {
        return layoutId;
    }

    public Bundle getArgs() {
        return args;
    }

    void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    void setLayoutItems(int[] layoutItems) {
        this.layoutItems = layoutItems;
    }

    void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    private void setId(int id) {
        this.id = id;
    }

    private void setArgs(Bundle args) {
        this.args = args;
    }
}
