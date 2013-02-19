package fake.domain.adamlopresto.godo.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class TasksTable {
	public static final String TABLE = "tasks";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "task_name";
	public static final String COLUMN_NOTES = "task_notes";
	public static final String COLUMN_NOTIFY = "notify";
	public static final String COLUMN_NOTIFY_SOUND = "notify_sound";
	

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE
				+ "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
				+ COLUMN_NOTES + " TEXT, "
				+ COLUMN_NOTIFY + " BOOLEAN, "
				+ COLUMN_NOTIFY_SOUND + " BOOLEAN "
				+ ")"
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { 
				COLUMN_ID,
				COLUMN_NAME,
				COLUMN_NOTES,
				COLUMN_NOTIFY,
				COLUMN_NOTIFY_SOUND,
		};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			requestedColumns.removeAll(availableColumns);
			// Check if all columns which are requested are available
			if (!requestedColumns.isEmpty()) {
				throw new IllegalArgumentException("Unknown columns in projection: "+requestedColumns);
			}
		}
	}
}
