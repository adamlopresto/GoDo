package fake.domain.adamlopresto.godo.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class InstanceDependencyTable {

	public static final String TABLE = "instance_dependency";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FIRST = "first";
	public static final String COLUMN_SECOND = "second";
	
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID      + " integer primary key autoincrement, "
				+ COLUMN_FIRST   + " integer not null references "+InstancesTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_SECOND  + " integer not null references "+InstancesTable.TABLE+" ON DELETE CASCADE, "
				+ "UNIQUE ("+COLUMN_FIRST+","+COLUMN_SECOND+"))"
		);
		
		db.execSQL("CREATE INDEX instance_dependency_first ON "+TABLE+" ("+COLUMN_FIRST+")");
		db.execSQL("CREATE INDEX instance_dependency_second ON "+TABLE+" ("+COLUMN_SECOND+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_FIRST, COLUMN_SECOND};
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

