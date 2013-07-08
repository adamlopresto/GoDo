package fake.domain.adamlopresto.godo.db;

import java.util.Arrays;
import java.util.HashSet;

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
		db.execSQL("CREATE TABLE "+TABLE
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
		
		db.execSQL("CREATE INDEX instance_task ON "+TABLE+" ("+COLUMN_TASK+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, 
				COLUMN_TASK,
				COLUMN_NOTES,
				COLUMN_DONE_DATE,
				COLUMN_DUE_DATE,
				COLUMN_START_DATE,
				COLUMN_PLAN_DATE,
				COLUMN_CREATE_DATE,
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
