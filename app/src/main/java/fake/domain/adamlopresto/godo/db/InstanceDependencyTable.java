package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

public class InstanceDependencyTable {

    public static final String TABLE = "instance_dependency";

    //as of version 1
    @SuppressWarnings("WeakerAccess")
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRST = "first";
    public static final String COLUMN_SECOND = "second";


    public static void onCreate(@NotNull SQLiteDatabase db) {
        db.execSQL("create table " + TABLE
                        + "("
                        + COLUMN_ID + " integer primary key autoincrement, "
                        + COLUMN_FIRST + " integer not null references " + InstancesTable.TABLE + " ON DELETE CASCADE, "
                        + COLUMN_SECOND + " integer not null references " + InstancesTable.TABLE + " ON DELETE CASCADE, "
                        + "UNIQUE (" + COLUMN_FIRST + "," + COLUMN_SECOND + "))"
        );

        db.execSQL("CREATE INDEX instance_dependency_first ON " + TABLE + " (" + COLUMN_FIRST + ")");
        db.execSQL("CREATE INDEX instance_dependency_second ON " + TABLE + " (" + COLUMN_SECOND + ")");
    }

    /*
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    */
}

