package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;

public class InstancesTable {

    public static final String TABLE = "instances";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_NOTES = "instance_notes";
    public static final String COLUMN_DONE_DATE = "done_date";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_PLAN_DATE = "plan_date";
    public static final String COLUMN_CREATE_DATE = "create_date";


    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                        + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_TASK + " INTEGER NOT NULL REFERENCES " + TasksTable.TABLE + " ON DELETE CASCADE, "
                        + COLUMN_NOTES + " TEXT, "
                        + COLUMN_DONE_DATE + " DATETIME, "
                        + COLUMN_DUE_DATE + " DATETIME, "
                        + COLUMN_START_DATE + " DATETIME, "
                        + COLUMN_PLAN_DATE + " DATETIME, "
                        + COLUMN_CREATE_DATE + " DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'localtime'))"
                        + ")"
        );

        db.execSQL("CREATE INDEX instance_task ON " + TABLE + " (" + COLUMN_TASK + ")");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 4) {
            db.execSQL("UPDATE instances SET done_date = datetime(done_date, 'localtime') where length(done_date) > 10");
            db.execSQL("UPDATE instances SET due_date = datetime(due_date, 'localtime') where length(due_date) > 10");
            db.execSQL("UPDATE instances SET start_date = datetime(start_date, 'localtime') where length(start_date) > 10");
            db.execSQL("UPDATE instances SET plan_date = datetime(plan_date, 'localtime') where length(plan_date) > 10");
        }
    }
}
