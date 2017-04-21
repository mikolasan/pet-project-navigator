package io.github.mikolasan.petprojectnavigator;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

/**
 * Вот класс, который должен убрать из любой активити лишник методы и члены, необходимые для
 * отображения данных из таблицы данных. Передаем ему класс активити и вьюху, в которой нужно
 * отобразить данные.
 */


class PetDataLoader<T extends PetAnyLoader> implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter cursorAdapter;
    private T t;

    public PetDataLoader(Context context, T t, ListView list) {
        this.t = t;
        String[] from = t.getColumnNames();
        int[] to = t.getLayoutItems();
        cursorAdapter = new SimpleCursorAdapter(context, t.getLayoutId(), null, from, to, 0);
        list.setAdapter(cursorAdapter);
    }

    @Override
    public T onCreateLoader(int id, Bundle args) {
        t.onCreate(id, args);
        return t;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.changeCursor(null);
    }
}
