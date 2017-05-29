package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.ListView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Вот класс, который должен убрать из любой активити лишник методы и члены, необходимые для
 * отображения данных из таблицы данных. Передаем ему класс активити и вьюху, в которой нужно
 * отобразить данные.
 */


class PetDataLoader<T extends PetAnyLoader> implements LoaderManager.LoaderCallbacks<Cursor> {
    private final Constructor<? extends PetAnyLoader> loaderFactory;
    private T loader;
    public static final int mainActivityId = 1;
    public static final int projectActivityId = 2;
    public static final int tasksActivityId = 3;
    private CursorAdapter cursorAdapter;
    private ListView list;

    public PetDataLoader(Context context, T loader, ListView list) throws NoSuchMethodException {
        this.loaderFactory = loader.getClass().getConstructor(Context.class, PetDatabase.class);
        this.loader = loader;
        this.list = list;
        cursorAdapter = null;
    }

    @Override
    public T onCreateLoader(int id, Bundle args) {
        PetDatabase petDatabase = loader.petDatabase;
        Context context = loader.getContext();
        loader = null;
        try {
            loader = (T) loaderFactory.newInstance(context, petDatabase);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        loader.onCreate(id, args);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (cursorAdapter == null) {
            cursorAdapter = this.loader.createAdapter(data);
            list.setAdapter(cursorAdapter);
        }
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (cursorAdapter != null) {
            cursorAdapter.swapCursor(null);
        }
    }
}
