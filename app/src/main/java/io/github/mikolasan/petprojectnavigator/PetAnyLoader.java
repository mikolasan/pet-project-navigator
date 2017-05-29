package io.github.mikolasan.petprojectnavigator;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


class PetAnyLoader extends CursorLoader {
    protected PetDatabase petDatabase;
    private String[] columnNames;
    private int[] layoutItems;
    private int layoutId;
    private Bundle args;
    private CursorAdapter adapter;

    protected void fillView(View view, Context context, Cursor cursor) {}

    private class TrueCursorAdapter extends CursorAdapter
    {
        public TrueCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            fillView(view, context, cursor);
        }
    }

    PetAnyLoader(Context context, PetDatabase petDatabase) {
        super(context);
        this.petDatabase = petDatabase;
    }

    void onCreate(int id, Bundle args) {
        setArgs(args);
    }

    CursorAdapter createAdapter(Cursor cursor) {
        return new TrueCursorAdapter(getContext(), cursor);
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    String[] getColumnNames() {
        return columnNames;
    }

    int[] getLayoutItems() {
        return layoutItems;
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

    private void setArgs(Bundle args) {
        this.args = args;
    }
}
