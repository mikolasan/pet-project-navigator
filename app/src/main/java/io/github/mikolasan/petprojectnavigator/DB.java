package io.github.mikolasan.petprojectnavigator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by neupo on 8/24/2016.
 */

public class DB {



    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STATEMENT = "statement";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_TECH_ID = "tech_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LINKS = "links";
    public static final String COLUMN_SAVED = "saved";
    public static final String COLUMN_TYPE_ID = "type_id";


    
    private final Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(DB_PROJECTS_TABLE, null, null, null, null, null, null);
    }

    public void addTask(String name,
                        String links,
                        String statement,
                        int tech,
                        int time,
                        int type) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_LINKS, links);
        cv.put(COLUMN_STATEMENT, statement);

        cv.put(COLUMN_PROJECT_ID, 0); //!TODO
        cv.put(COLUMN_TECH_ID, tech);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_TYPE_ID, type);
        mDB.insert(DB_TASKS_TABLE, null, cv);
    }

    public void addTech(String name) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        mDB.insert(DB_TECH_TABLE, null, cv);
    }

    public void delTech(long id) {
        mDB.delete(DB_TECH_TABLE, COLUMN_ID + " = " + id, null);
    }

    private static final String DB_NAME = "petprojectdb";
    private static final int DB_VERSION = 1;
    private static final String DB_TECH_TABLE = "pp_tech";
    private static final String DB_TYPES_TABLE = "pp_types";
    private static final String DB_TASKS_TABLE = "pp_tasks";
    private static final String DB_PROJECTS_TABLE = "pp_project";

    private static final String DB_TECH_CREATE =
            "create table " + DB_TECH_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text" +
                    ");";


    private static final String DB_TYPES_CREATE =
            "create table " + DB_TYPES_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text" +
                    ");";

    private static final String DB_TASKS_CREATE =
            "create table " + DB_TASKS_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text" +
                    COLUMN_LINKS + " text" +
                    COLUMN_STATEMENT + " text" +
                    COLUMN_PROJECT_ID + " integer" +
                    COLUMN_TECH_ID + " integer" +
                    COLUMN_TYPE_ID + " integer" +
                    COLUMN_TIME + " integer" +
                    COLUMN_SAVED + " integer" +
                    ");";

    private static final String DB_PROJECTS_CREATE =
            "create table " + DB_PROJECTS_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text" +
                    ");";


    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }


        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_TECH_CREATE);
            db.execSQL(DB_TYPES_CREATE);
            db.execSQL(DB_TASKS_CREATE);
            db.execSQL(DB_PROJECTS_CREATE);
	}       

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
