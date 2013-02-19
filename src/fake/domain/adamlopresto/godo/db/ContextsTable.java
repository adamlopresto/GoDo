package fake.domain.adamlopresto.godo.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class ContextsTable {
	public static final String TABLE = "contexts";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "context_name";
	public static final String COLUMN_DESC = "description"; 
	public static final String COLUMN_ACTIVE = "active";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID    + " integer primary key autoincrement, "
				+ COLUMN_NAME  + " text not null collate nocase unique, "
				+ COLUMN_DESC  + " text, "
				+ COLUMN_ACTIVE + " boolean"
				+ ")"
		);
		
		db.execSQL("CREATE INDEX contexts_name ON "+TABLE+"("+COLUMN_NAME+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_NAME, COLUMN_DESC, COLUMN_ACTIVE
		};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
