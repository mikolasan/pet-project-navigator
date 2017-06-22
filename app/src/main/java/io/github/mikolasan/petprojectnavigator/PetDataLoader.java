package io.github.mikolasan.petprojectnavigator;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.ListView;

import com.mobeta.android.dslv.DragSortCursorAdapter;
import com.mobeta.android.dslv.DragSortListView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
    private DragSortCursorAdapter dragAdapter;
    private ListView list;
    private DragSortListView dragList;

    public PetDataLoader(Context context, T loader, ListView list) throws NoSuchMethodException {
        this.loaderFactory = loader.getClass().getConstructor(Context.class, PetDatabase.class);
        this.loader = loader;
        this.list = list;
        this.dragList = null;
        cursorAdapter = null;
        dragAdapter = null;
    }

    public PetDataLoader(Context context, T loader, DragSortListView list) throws NoSuchMethodException {
        this.loaderFactory = loader.getClass().getConstructor(Context.class, PetDatabase.class);
        this.loader = loader;
        this.list = null;
        this.dragList = list;
        cursorAdapter = null;
        dragAdapter = null;
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
        if(list != null) {
            if (cursorAdapter == null) {
                cursorAdapter = this.loader.createAdapter(data);
                list.setAdapter(cursorAdapter);
            }
            cursorAdapter.swapCursor(data);
        } else if (dragList != null) {
            if (dragAdapter == null) {
                dragAdapter = this.loader.createDragAdapter(data);
                dragList.setAdapter(dragAdapter);
            }
            dragAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(list != null) {
            if (cursorAdapter != null) {
                cursorAdapter.swapCursor(null);
            }
        } else if (dragList != null) {
            if (dragAdapter != null) {
                dragAdapter.swapCursor(null);
            }
        }
    }

    public void onDropAction(int from, int to) {
        if (dragList != null && dragAdapter != null) {
            List list = dragAdapter.getCursorPositions();
            list.set(from, to);
            list.set(to, from);
        }
    }

    public void onRemoveAction(int which) {
        if (dragList != null && dragAdapter != null) {
            List list = dragAdapter.getCursorPositions();
            list.remove(which);
        }
    }
}
