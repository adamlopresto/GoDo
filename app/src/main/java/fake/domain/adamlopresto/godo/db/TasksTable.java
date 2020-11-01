package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

public class TasksTable {
    public static final String TABLE = "tasks";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "task_name";
    public static final String COLUMN_NOTES = "task_notes";

    //drawn from NotificationLevels
    public static final String COLUMN_NOTIFICATION = "notification";

    //as of version 2
    //drawn from RepeatTypes
    public static final String COLUMN_REPEAT = "repeat";

    //as of version 3
    public static final String COLUMN_DUE_NOTIFICATION = "due_notification";

    //as of version 6
    public static final String COLUMN_TASKER_LABEL = "tasker_label";
    public static final String COLUMN_TASKER_COMMAND = "tasker_command";

    public static void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE
                        + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_NAME + " TEXT COLLATE NOCASE, "
                        + COLUMN_NOTES + " TEXT, "
                        + COLUMN_NOTIFICATION + " INTEGER, "
                        + COLUMN_REPEAT + " INTEGER, "
                        + COLUMN_DUE_NOTIFICATION + " INTEGER, "
                        + COLUMN_TASKER_LABEL + " TEXT, "
                        + COLUMN_TASKER_COMMAND + " TEXT, "
                + ")"
        );
    }

    public static void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE
                            + " ADD COLUMN " + COLUMN_DUE_NOTIFICATION + " INTEGER"
            );
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE
                    + " ADD COLUMN " + COLUMN_TASKER_LABEL + " TEXT"
            );
            db.execSQL("ALTER TABLE " + TABLE
                    + " ADD COLUMN " + COLUMN_TASKER_COMMAND + " TEXT"
            );
        }
    }

}
