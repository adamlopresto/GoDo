package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

public class TaskContextTable {

    public static final String TABLE = "task_context";

    //as of version 1
    @SuppressWarnings("WeakerAccess")
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_CONTEXT = "context";


    @SuppressWarnings("SpellCheckingInspection")
    public static void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("create table " + TABLE
                        + "("
                        + COLUMN_ID + " integer primary key autoincrement, "
                        + COLUMN_TASK + " integer not null references " + TasksTable.TABLE + " ON DELETE CASCADE, "
                        + COLUMN_CONTEXT + " integer not null references " + ContextsTable.TABLE
                        + " ON DELETE CASCADE, UNIQUE (" + COLUMN_TASK + "," + COLUMN_CONTEXT + "))"
        );

        db.execSQL("CREATE INDEX taskcontext_task ON " + TABLE + " (" + COLUMN_TASK + ")");
        db.execSQL("CREATE INDEX taskcontext_context ON " + TABLE + " (" + COLUMN_CONTEXT + ")");
    }

    /*
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    */
}

