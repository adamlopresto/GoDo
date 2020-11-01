package fake.domain.adamlopresto.godo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fake.domain.adamlopresto.godo.SettingsActivity;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final String DATABASE_NAME = "GoDo";
    /*
     * Version history:
     * 1: initial release
     * 2: add repetition rules
     * 3: add due notification
     * 4: all times are stored as local
     * 5: sort by dependencies
     * 6: Tasker integration
     */
    private static final int CURRENT_VERSION = 6;
    @Nullable
    private static DatabaseHelper mInstance;
    @NonNull
    private final Context context;

    private DatabaseHelper(@NonNull Context context) {
        super(context, getDatabaseName(context), null, CURRENT_VERSION);
        this.context = context;
    }

    @NonNull
    private static String getDatabaseName(@NonNull Context context) {
        File path = context.getExternalFilesDir(null);
        String suffix = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_SHOWOFF, false)
                ? "-showoff" : "";
        if (path == null)
            return DATABASE_NAME + suffix;
        return path + "/" + DATABASE_NAME + suffix;
    }

    @NonNull
    public static DatabaseHelper getInstance(@NonNull Context ctx) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null)
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        return mInstance;
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        TasksTable.onCreate(db);
        InstancesTable.onCreate(db);
        ContextsTable.onCreate(db);
        TaskContextTable.onCreate(db);
        InstanceDependencyTable.onCreate(db);
        InstancesView.onCreate(db);
        RepetitionRulesTable.onCreate(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        TasksTable.onUpgrade(db, oldVersion);
        InstancesTable.onUpgrade(db, oldVersion);
        //ContextsTable.onUpgrade(db, oldVersion, newVersion);
        //TaskContextTable.onUpgrade(db, oldVersion, newVersion);
        //InstanceDependencyTable.onUpgrade(db, oldVersion, newVersion);
        InstancesView.onUpgrade(db, oldVersion);
        RepetitionRulesTable.onUpgrade(db, oldVersion);
    }

    @NonNull
    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        if (db == null) {
            throw new SQLiteException("getReadableDatabase returned null");
        }
        return db;
    }

    @NonNull
    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        if (db == null) {
            throw new SQLiteException("getWritableDatabase returned null");
        }
        return db;
    }

    @Override
    public void onOpen(@NonNull SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public void notifyChange(Uri uri) {
        context.getContentResolver().notifyChange(uri, null);
    }

    @NonNull
    public Context getContext() {
        return context;
    }
}
