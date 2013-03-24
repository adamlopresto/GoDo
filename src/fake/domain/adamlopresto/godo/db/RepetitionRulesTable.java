package fake.domain.adamlopresto.godo.db;

import android.database.sqlite.SQLiteDatabase;

public class RepetitionRulesTable {
	
	public static final String TABLE = "repetition_rules";

	//as of version 2
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TASK = "task";
	public static final String COLUMN_TYPE = "rule_type";
	public static final String COLUMN_SUBVALUE = "subvalue";
	public static final String COLUMN_FROM = "from_column";
	public static final String COLUMN_TO = "to_column";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE
				+ "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_TASK + " INTEGER NOT NULL REFERENCES " + TasksTable.TABLE + " ON DELETE CASCADE, "
				+ COLUMN_TYPE + " INTEGER NOT NULL, "
				+ COLUMN_SUBVALUE + " TEXT NOT NULL, "
				+ COLUMN_FROM + " INTEGER NOT NULL, "
				+ COLUMN_TO + " INTEGER NOT NULL "
				+ ")"
		);
		
		db.execSQL("CREATE INDEX repetition_task ON "+TABLE+" ("+COLUMN_TASK+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if (oldVersion < 2)
			onCreate(db);
	}

	public static void checkColumns(String[] projection) {
		throw new UnsupportedOperationException("Not yet implemented");
		/*
		String[] available = { COLUMN_ID, 
				COLUMN_TASK,
				
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
		*/
	}
}
