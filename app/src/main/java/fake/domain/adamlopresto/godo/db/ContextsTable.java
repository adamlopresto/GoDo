package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

public class ContextsTable {
    public static final String TABLE = "contexts";

    //as of version 1
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "context_name";
    public static final String COLUMN_DESC = "description";
    public static final String COLUMN_ACTIVE = "active";

    public static void onCreate(@NotNull SQLiteDatabase db) {
        db.execSQL("create table " + TABLE
                        + "("
                        + COLUMN_ID + " integer primary key autoincrement, "
                        + COLUMN_NAME + " text not null collate nocase unique, "
                        + COLUMN_DESC + " text, "
                        + COLUMN_ACTIVE + " boolean default 0"
                        + ")"
        );

        db.execSQL("CREATE INDEX contexts_name ON " + TABLE + "(" + COLUMN_NAME + ")");
    }

    /*
    //Uncomment if we need to upgrade this
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }
    */
}
