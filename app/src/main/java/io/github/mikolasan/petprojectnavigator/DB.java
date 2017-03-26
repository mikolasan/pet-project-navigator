package io.github.mikolasan.petprojectnavigator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArraySet;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.codec.binary.Hex;

import java.util.Iterator;

class  PetTask {
    public PetTask() {
        this.taskId = 0;
        this.projectId = 0;
        this.name = "";
        this.links = "";
        this.statement = "";
        this.tech = 0;
        this.time = 0;
        this.type = 0;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public int getTech() {
        return tech;
    }

    public void setTech(int tech) {
        this.tech = tech;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    int taskId;
    int projectId;
    String name;
    String links;
    String statement;
    int tech;
    int time;
    int type;
}

class DB {

    private static DB instance = null;
    private static final String DB_NAME = "petprojectdb";
    private static final int DB_VERSION = 5;

    static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESC = "desc";
    public static final String COLUMN_STATEMENT = "statement";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_TECH_ID = "tech_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LINKS = "links";
    public static final String COLUMN_SAVED = "saved";
    public static final String COLUMN_TYPE_ID = "type_id";
    private final int BUFFER_ID = 0;

    private final Context mCtx;
    private static DBHelper mDBHelper = null;
    private SQLiteDatabase mDB;

    public static DB getOpenedInstance() {
        if(instance == null) {
            instance = new DB(AndroidClient.getAppContext());
            instance.open();
        }
        return instance;
    }

    private MatrixCursor new_tech;
    public static final int TECH_UNDEFINED_ID = -1;
    public static final String TECH_UNDEFINED_NAME = "Undefined";

    private MatrixCursor new_type;
    public static final int TYPE_UNDEFINED_ID = -1;
    public static final String TYPE_UNDEFINED_NAME = "Undefined";

    public DB(Context ctx) {
        new_tech = new MatrixCursor(new String[]{ COLUMN_ID, COLUMN_NAME});
        new_tech.addRow(new Object[]{TECH_UNDEFINED_ID, TECH_UNDEFINED_NAME});
        new_type = new MatrixCursor(new String[]{ COLUMN_ID, COLUMN_NAME});
        new_type.addRow(new Object[]{TYPE_UNDEFINED_ID, TYPE_UNDEFINED_NAME});
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

    public boolean hasObject(String table, String id, String value) {
        String selectString = "SELECT * FROM " + table + " WHERE " + id + " =?";
        Cursor cursor = mDB.rawQuery(selectString, new String[] {value});
        boolean hasObject = cursor.getCount() > 0;
        cursor.close();
        return hasObject;
    }

    /*
    *
    * Projects
    *
     */
    // получить все данные из таблицы DB_TABLE
    Cursor getAllProjects() {
        return mDB.query(DB_PROJECTS_TABLE, null, null, null, null, null, null);
    }

    long addProject(String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DESC, description);
        return mDB.insert(DB_PROJECTS_TABLE, null, cv);
    }

    void saveProjectDetails(int projectId, String name, String description) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DESC, description);
        mDB.update(DB_PROJECTS_TABLE, cv, COLUMN_ID + " = ?", new String[] {Integer.toString(projectId)});
    }

    void createProjectFromBuffer(String name, String description) {
        long projectId = addProject(name, description);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROJECT_ID, projectId);
        mDB.update(DB_TASKS_TABLE, cv, COLUMN_PROJECT_ID + " = ?", new String[] {Integer.toString(BUFFER_ID)});
    }

    void deleteProject(int projectId) {
        mDB.delete(DB_PROJECTS_TABLE, COLUMN_ID + " = " + projectId, null);
        deleteTasksOfProject(projectId);
    }

    /*
    *
    * Tasks
    *
     */
    Cursor getAllTasks(int projectId) {
        String selection = COLUMN_PROJECT_ID + " = " + projectId;
        return mDB.query(DB_TASKS_TABLE, null, selection, null, null, null, null);
    }

    void addTask(PetTask petTask) {
        String id = String.valueOf(petTask.getTaskId());
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROJECT_ID, petTask.getProjectId());

        cv.put(COLUMN_NAME, petTask.getName());
        cv.put(COLUMN_LINKS, petTask.getLinks());
        cv.put(COLUMN_STATEMENT, petTask.getStatement());

        cv.put(COLUMN_TECH_ID, petTask.getTech());
        cv.put(COLUMN_TIME, petTask.getTime());
        cv.put(COLUMN_TYPE_ID, petTask.getType());

        if (hasObject(DB_TASKS_TABLE, DB.COLUMN_ID, id)) {
            mDB.update(DB_TASKS_TABLE, cv, DB.COLUMN_ID + " = ?", new String[] {id});
        } else {
            mDB.insert(DB_TASKS_TABLE, null, cv);
        }
    }

    void copyTask(int taskId, int projectId) {
        Cursor c = mDB.query(DB_TASKS_TABLE, new String[] {COLUMN_PROJECT_ID, COLUMN_NAME, COLUMN_LINKS, COLUMN_LINKS, COLUMN_TECH_ID, COLUMN_TIME, COLUMN_TYPE_ID},
                COLUMN_ID + " = " + taskId, null, null, null, null);
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, cv);
        cv.put(COLUMN_PROJECT_ID, projectId);
        mDB.insert(DB_TASKS_TABLE, null, cv);
    }

    void deleteTask(int taskId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_ID + " = " + taskId, null);
    }

    void deleteTasksOfProject(int projectId) {
        mDB.delete(DB_TASKS_TABLE, COLUMN_PROJECT_ID + " = ?", new String[] {Integer.toString(projectId)});
    }

    void moveTask(int taskId, int projectId) {
        copyTask(taskId, projectId);
        deleteTask(taskId);
    }

    void moveTaskToBuffer(int taskId) {
        moveTask(taskId, BUFFER_ID);
    }

    /*
    *
    * Technologies
    *
     */
    public Cursor getAllTech() {
        Cursor[] cursors = {
                new_tech,
                mDB.query(DB_TECH_TABLE, null,  null, null, null, null, null)
        };
        return new MergeCursor(cursors);
    }

    public void addTech(String name) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        mDB.insert(DB_TECH_TABLE, null, cv);
    }

    public void deleteTech(String techId) {
        mDB.delete(DB_TECH_TABLE, COLUMN_ID + " = ?", new String[] {techId});
    }

    Cursor getAllTasksByTech(String techId) {
        return mDB.query(DB_TASKS_TABLE, null, COLUMN_ID + " = ?", new String[] {techId}, null, null, null);
    }

    Cursor getAllProjectByTech(String techId) {
        Cursor c = getAllTasksByTech(techId);
        ArraySet<String> projects = new ArraySet<>();
        if (c.moveToFirst()) {
            int columnIndex =  c.getColumnIndex(COLUMN_PROJECT_ID);
            do {
                projects.add(c.getString(columnIndex));
            } while (c.moveToNext());
        }
        String[] selection = new String[projects.size()];
        projects.toArray(selection);
        return mDB.query(DB_PROJECTS_TABLE, null, COLUMN_ID + " = ?", selection, null, null, null);
    }

    /*
    *
    * Types
    *
     */
    public Cursor getAllTypes() {
        Cursor[] cursors = {
                new_type,
                mDB.query(DB_TYPES_TABLE, null, null, null, null, null, null)
        };
        return new MergeCursor(cursors);
    }

    public void addType(String name) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        mDB.insert(DB_TYPES_TABLE, null, cv);
    }

    public static final String DB_TECH_TABLE = "pp_tech";
    public static final String DB_TYPES_TABLE = "pp_types";
    public static final String DB_TASKS_TABLE = "pp_tasks";
    public static final String DB_PROJECTS_TABLE = "pp_project";

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
                    COLUMN_NAME + " text, " +
                    COLUMN_LINKS + " text, " +
                    COLUMN_STATEMENT + " text, " +
                    COLUMN_PROJECT_ID + " integer, " +
                    COLUMN_TECH_ID + " integer, " +
                    COLUMN_TYPE_ID + " integer, " +
                    COLUMN_TIME + " integer, " +
                    COLUMN_SAVED + " integer" +
                    ");";

    private static final String DB_PROJECTS_CREATE =
            "create table " + DB_PROJECTS_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_DESC + " text, " +
                    COLUMN_NAME + " text" +
                    ");";


    private static final String LOG_TAG_NAME = "DB LOG";

    void clearAll(SQLiteDatabase db){
        db.execSQL("drop table " + DB_TECH_TABLE);
        db.execSQL("drop table " + DB_TYPES_TABLE);
        db.execSQL("drop table " + DB_TASKS_TABLE);
        db.execSQL("drop table " + DB_PROJECTS_TABLE);
    }

    void createNew(SQLiteDatabase db){
        db.execSQL(DB_TECH_CREATE);
        db.execSQL(DB_TYPES_CREATE);
        db.execSQL(DB_TASKS_CREATE);
        db.execSQL(DB_PROJECTS_CREATE);
    }

    private JSONObject cursorToJson(Cursor c) {
        JSONObject retVal = new JSONObject();
        for(int i=0; i<c.getColumnCount(); i++) {
            String cName = c.getColumnName(i);
            try {
                switch (c.getType(i)) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        retVal.put(cName, c.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        retVal.put(cName, c.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        retVal.put(cName, c.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        retVal.put(cName, Hex.encodeHexString(c.getBlob(i)));
                        break;
                }
            }
            catch(Exception ex) {
                Log.e(LOG_TAG_NAME, "Exception converting cursor column to json field: " + cName);
            }
        }
        return retVal;
    }

    private JSONArray tableToJson(String tableName) {
        Cursor cursor = mDB.query(tableName, null,  null, null, null, null, null);
        JSONArray result = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            result.put(cursorToJson(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("TAG_NAME", result.toString());
        return result;
    }

    public String prepareJson() {
        String[] tables = {DB.DB_TECH_TABLE,
                DB.DB_TYPES_TABLE,
                DB.DB_TASKS_TABLE,
                DB.DB_PROJECTS_TABLE
        };
        JSONObject obj = new JSONObject();
        for (String tableName : tables) {
            JSONArray json = tableToJson(tableName);
            try {
                obj.put(tableName, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj.toString();
    }

    JSONObject parseJson(String json){
        try {
            JSONObject mainObject = new JSONObject(json);
            return mainObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    void insertData(JSONObject tableObject, String tableName) throws JSONException {
        JSONArray jsonArray = tableObject.getJSONArray(tableName);
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Iterator<String> keys = jsonObject.keys();
            ContentValues cv = new ContentValues();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof Integer) {
                    cv.put(key, jsonObject.getInt(key));
                } else if (jsonObject.get(key) instanceof String) {
                    cv.put(key, jsonObject.getString(key));
                } else {
                    Log.i(LOG_TAG_NAME, "insertData: bad type in JSON");
                }
            }
            mDB.insert(tableName, null, cv);
        }
    }

    boolean restore(String json) {
        clearAll(mDB);
        createNew(mDB);
        JSONObject mainObject = parseJson(json);
        if (mainObject != null) {
            Iterator<String> tables = mainObject.keys();
            while (tables.hasNext()) {
                try {
                    insertData(mainObject, tables.next());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            createNew(db);
    	}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                db.execSQL("drop table " + DB_TASKS_TABLE);
                db.execSQL(DB_TASKS_CREATE);
            } else if (oldVersion <= 3) { // 2,3
                db.execSQL("drop table " + DB_PROJECTS_TABLE);
                db.execSQL(DB_PROJECTS_CREATE);
            } else if (oldVersion == 4) {
                db.execSQL("drop table " + DB_TECH_TABLE);
                db.execSQL("drop table " + DB_TYPES_TABLE);
                db.execSQL("drop table " + DB_TASKS_TABLE);
                db.execSQL(DB_TECH_CREATE);
                db.execSQL(DB_TYPES_CREATE);
                db.execSQL(DB_TASKS_CREATE);
            } else if (oldVersion == 5) {
                db.execSQL("drop table " + DB_TYPES_TABLE);
                db.execSQL(DB_TYPES_CREATE);
                db.execSQL("insert into " + DB_TYPES_TABLE + " values (\"type 1\");" +
                        "insert into " + DB_TYPES_TABLE + " values (\"type 2\");");
            }
        }
    }
}
