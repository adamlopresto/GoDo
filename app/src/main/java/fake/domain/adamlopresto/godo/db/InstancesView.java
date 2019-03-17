package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public class InstancesView {

    public static final String VIEW = "instances_view";

    //as of version 1
    public static final String COLUMN_ID = InstancesTable.COLUMN_ID;
    public static final String COLUMN_TASK = InstancesTable.COLUMN_TASK;
    public static final String COLUMN_TASK_NAME = TasksTable.COLUMN_NAME;
    public static final String COLUMN_TASK_NOTES = TasksTable.COLUMN_NOTES;
    public static final String COLUMN_INSTANCE_NOTES = InstancesTable.COLUMN_NOTES;
    public static final String COLUMN_NOTIFICATION = TasksTable.COLUMN_NOTIFICATION;
    public static final String COLUMN_DUE_NOTIFICATION = TasksTable.COLUMN_DUE_NOTIFICATION;
    public static final String COLUMN_REPEAT = TasksTable.COLUMN_REPEAT;
    public static final String COLUMN_DONE_DATE = InstancesTable.COLUMN_DONE_DATE;
    public static final String COLUMN_DUE_DATE = InstancesTable.COLUMN_DUE_DATE;
    public static final String COLUMN_START_DATE = InstancesTable.COLUMN_START_DATE;
    public static final String COLUMN_PLAN_DATE = InstancesTable.COLUMN_PLAN_DATE;
    public static final String COLUMN_CREATE_DATE = InstancesTable.COLUMN_CREATE_DATE;
    public static final String COLUMN_BLOCKED_BY_CONTEXT = "blocked_by_context";
    public static final String COLUMN_BLOCKED_BY_TASK = "blocked_by_task";
    public static final String COLUMN_NEXT_STEPS = "next_steps";

    public static void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE VIEW " + VIEW
                        + " AS SELECT "
                        + "i._id AS " + COLUMN_ID + ", "
                        + COLUMN_TASK + ", "
                        + COLUMN_TASK_NAME + ", "
                        + COLUMN_TASK_NOTES + ", "
                        + COLUMN_INSTANCE_NOTES + ", "
                        + COLUMN_NOTIFICATION + ", "
                        + COLUMN_DUE_NOTIFICATION + ", "
                        + COLUMN_REPEAT + ", "
                        + COLUMN_DONE_DATE + ", "
                        + COLUMN_DUE_DATE + ", "
                        + COLUMN_START_DATE + ", "
                        + COLUMN_PLAN_DATE + ", "
                        + COLUMN_CREATE_DATE + ", "
                        + "EXISTS (SELECT * FROM task_context tc INNER JOIN contexts c " +
                        " ON tc.context = c._id WHERE tc.task=i.task AND c.active <> 1) " +
                        "AS " + COLUMN_BLOCKED_BY_CONTEXT + ", " +
                        "EXISTS " +
                        "(SELECT * FROM instance_dependency AS dep INNER JOIN instances AS prereq " +
                        " ON dep.first = prereq._id WHERE dep.second=i._id and prereq.done_date IS NULL) " +
                        "AS " + COLUMN_BLOCKED_BY_TASK + ", " +
                        "(SELECT COUNT(*) FROM instance_dependency WHERE first=i._id) AS "
                        + COLUMN_NEXT_STEPS
                        + " from instances i inner join tasks t on i.task=t._id "
        );
    }

    public static void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 5) {
            db.execSQL("DROP VIEW " + VIEW);
            onCreate(db);
        }
    }
}

