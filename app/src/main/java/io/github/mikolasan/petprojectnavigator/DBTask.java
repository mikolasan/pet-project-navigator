package io.github.mikolasan.petprojectnavigator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import static io.github.mikolasan.petprojectnavigator.Tools.getOrSelection;

/**
 * Created by neupo on 3/25/2017.
 */

public class DBTask {
    static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STATEMENT = "statement";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_TECH_ID = "tech_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LINKS = "links";
    public static final String COLUMN_SAVED = "saved";
    public static final String COLUMN_TYPE_ID = "type_id";
    private final int BUFFER_ID = 0;
    private SQLiteDatabase mDB;

    public DBTask(SQLiteDatabase mDB) {
        this.mDB = mDB;
    }

    public boolean hasObject(String table, String id, String value) {
        String selectString = "SELECT * FROM " + table + " WHERE " + id + " =?";
        Cursor cursor = mDB.rawQuery(selectString, new String[] {value});
        boolean hasObject = cursor.getCount() > 0;
        cursor.close();
        return hasObject;
    }

    Cursor getAll() {
        return mDB.query(DB_TASKS_TABLE, null, null, null, null, null, null);
    }

    Cursor getAllByProject(int projectId) {
        String selection = COLUMN_PROJECT_ID + " = " + projectId;
        return mDB.query(DB_TASKS_TABLE, null, selection, null, null, null, null);
    }

    void add(PetTask petTask) {
        String id = String.valueOf(petTask.getTaskId());
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROJECT_ID, petTask.getProjectId());

        cv.put(COLUMN_NAME, petTask.getName());
        cv.put(COLUMN_LINKS, petTask.getLinks());
        cv.put(COLUMN_STATEMENT, petTask.getStatement());

        cv.put(COLUMN_TECH_ID, petTask.getTech());
        cv.put(COLUMN_TIME, petTask.getTime());
        cv.put(COLUMN_TYPE_ID, petTask.getType());

        if (hasObject(DB_TASKS_TABLE, PetDatabase.COLUMN_ID, id)) {
            mDB.update(DB_TASKS_TABLE, cv, PetDatabase.COLUMN_ID + " = ?", new String[] {id});
        } else {
            mDB.insert(DB_TASKS_TABLE, null, cv);
        }
    }

    void copy(int taskId, int projectId) {
        String[] columns = new String[] {COLUMN_PROJECT_ID, COLUMN_NAME, COLUMN_LINKS, COLUMN_LINKS, COLUMN_TECH_ID, COLUMN_TIME, COLUMN_TYPE_ID};
        try(Cursor c = mDB.query(DB_TASKS_TABLE,
                columns,
                COLUMN_ID + " = ?",
                new String[] {Integer.toString(taskId)},
                null, null, null)) {
            if (c.moveToFirst()) {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(c, cv);
                cv.put(COLUMN_PROJECT_ID, projectId);
                mDB.insert(DB_TASKS_TABLE, null, cv);
            }
        } catch (Exception e) {
            Log.e("DBTask copy", "bad copy argument", e);
        }
    }

    void delete(int taskId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_ID + " = " + taskId, null);
    }

    void deleteTasksOfProject(int projectId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_PROJECT_ID + " = ? ", new String[] {Integer.toString(projectId)});
    }

    void move(int taskId, int projectId) {
        copy(taskId, projectId);
        delete(taskId);
    }

    void moveToBuffer(int taskId) {
        move(taskId, BUFFER_ID);
    }

    void moveBufferToProject(int projectId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_ID, projectId);
        mDB.update(DB_TASKS_TABLE, values,
                COLUMN_PROJECT_ID + " = ?", new String[] {Integer.toString(BUFFER_ID)});
    }

    Cursor getAllByTech(String techName) {
        ArrayList<String> technologies = new ArrayList<>();
        try (Cursor cursor = mDB.query(true,
                PetDatabase.DB_TECH_TABLE,
                new String[]{PetDatabase.COLUMN_ID},
                PetDatabase.COLUMN_NAME + " LIKE ?",
                new String[]{"%" + techName + "%"},
                null,
                null,
                null,
                null)) {
            int id = cursor.getColumnIndex(PetDatabase.COLUMN_ID);
            while (cursor.moveToNext()) {
                int tech = cursor.getInt(id);
                technologies.add(Integer.toString(tech));
            }
            if (!technologies.isEmpty()) {
                String selection = getOrSelection(COLUMN_TECH_ID, technologies.size());
                return mDB.query(DB_TASKS_TABLE, null, selection, technologies.toArray(new String[0]), null, null, null);
            }
        } catch (Exception e) {
            Log.e("getAllByTech", "hmmm... :(", e);
        }
        return null;
    }


    public Cursor getAllByName(String query) {
        return mDB.query(PetDatabase.DB_TASKS_TABLE, null, PetDatabase.COLUMN_NAME + " LIKE ?", new String[] {"%" + query + "%"}, null, null, null);
    }

    public Cursor getAllByDesc(String query) {
        return mDB.query(PetDatabase.DB_TASKS_TABLE, null, PetDatabase.COLUMN_DESC + " LIKE ?", new String[] {"%" + query + "%"}, null, null, null);
    }

    public Cursor getAllByTime(String query) {
        try {
            int time = Integer.parseInt(query);
            if (time  > 0)
                return mDB.query(PetDatabase.DB_TASKS_TABLE, null, PetDatabase.COLUMN_TIME + " <= ?", new String[]{query}, null, null, null);
            else
                return getAll();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static final String DB_TASKS_TABLE = "pp_tasks";
    private static final String DB_TASKS_CREATE =
            "create table " + DB_TASKS_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_LINKS + " text, " +
                    COLUMN_STATEMENT + " text, " +
                    COLUMN_PROJECT_ID + " integer, " +
                    COLUMN_TECH_ID + " integer, " +
                    COLUMN_TYPE_ID + " integer, " +
                    COLUMN_TIME + " integer, " +
                    COLUMN_SAVED + " integer" +
                    ");";

    void clearAll(SQLiteDatabase db){
        db.execSQL("drop table " + DB_TASKS_TABLE);
    }

    void create(SQLiteDatabase db){
        db.execSQL(DB_TASKS_CREATE);
    }
}
