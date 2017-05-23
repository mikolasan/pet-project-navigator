package io.github.mikolasan.petprojectnavigator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

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
        Cursor c = mDB.query(DB_TASKS_TABLE, new String[] {COLUMN_PROJECT_ID, COLUMN_NAME, COLUMN_LINKS, COLUMN_LINKS, COLUMN_TECH_ID, COLUMN_TIME, COLUMN_TYPE_ID},
                COLUMN_ID + " = " + taskId, null, null, null, null);
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, cv);
        cv.put(COLUMN_PROJECT_ID, projectId);
        mDB.insert(DB_TASKS_TABLE, null, cv);
    }

    void delete(int taskId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_ID + " = " + taskId, null);
    }

    void deleteTasksOfProject(int projectId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_PROJECT_ID + " = ?", new String[] {Integer.toString(projectId)});
    }

    void move(int taskId, int projectId) {
        copy(taskId, projectId);
        delete(taskId);
    }

    void moveToBuffer(int taskId) {
        move(taskId, BUFFER_ID);
    }

    Cursor getAllByTech(String techId) {
        return mDB.query(DB_TASKS_TABLE, null, COLUMN_ID + " = ?", new String[] {techId}, null, null, null);
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
