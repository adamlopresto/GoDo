package fake.domain.adamlopresto.godo.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class AvailableInstancesView {

	public static final String VIEW = "available_instances";

	//as of version 1
	public static final String COLUMN_ID = InstancesTable.COLUMN_ID;
	public static final String COLUMN_TASK = InstancesTable.COLUMN_TASK;
	public static final String COLUMN_TASK_NAME = TasksTable.COLUMN_NAME;
	public static final String COLUMN_TASK_NOTES = "task_notes";
	public static final String COLUMN_INSTANCE_NOTES = "instance_notes";
	//TODO
	
	
	public static void onCreate(SQLiteDatabase db) {
		/*
		db.execSQL("CREATE VIEW "+VIEW
				+ " AS SELECT " 
				+ TaskContextTable.TABLE + "."+ TaskContextTable.COLUMN_ID + " AS " + COLUMN_ID+", "
				+ COLUMN_ITEM + ", "
				+ TasksTable.TABLE + "." + TasksTable.COLUMN_NAME + " AS " + COLUMN_ITEM_NAME + ", "
				+ TasksTable.TABLE + "." + TasksTable.COLUMN_LIST + " AS " + COLUMN_LIST + ", "
				+ COLUMN_QUANTITY + ", "
				+ COLUMN_UNITS + ", "
				+ COLUMN_NOTES + ", "
				+ COLUMN_PRICE + ", "
				+ COLUMN_STATUS + ", "
				+ COLUMN_CATEGORY + ", "
				+ COLUMN_AISLE + ", "
				+ COLUMN_STORE + ", "
				+ StoresTable.TABLE + "." + StoresTable.COLUMN_NAME + " AS "+COLUMN_STORE_NAME + ", "
				+ ContextsTable.TABLE + "." + ContextsTable.COLUMN_NAME + " AS "+COLUMN_AISLE_NAME + ", "
				+ COLUMN_DESC + ", "
				+ COLUMN_SORT
				+ " FROM " + TasksTable.TABLE + " INNER JOIN " + TaskContextTable.TABLE 
				+ " ON " + TasksTable.TABLE + "." + TasksTable.COLUMN_ID + "=" + TaskContextTable.COLUMN_ITEM
				+ " INNER JOIN " + ContextsTable.TABLE 
				+ " ON " + TaskContextTable.COLUMN_AISLE + "=" + ContextsTable.TABLE +"."+ContextsTable.COLUMN_ID
				+ " INNER JOIN " + StoresTable.TABLE 
				+ " ON " + ContextsTable.COLUMN_STORE + "=" + StoresTable.TABLE+"."+StoresTable.COLUMN_ID
				);
				*/
		
		/*
		select * from instances i inner join tasks t on i.task=t._id where not exists 
				(select * from task_context tc inner join contexts c on tc.context = c._id 
				where tc.task=i.task and c.active <> 1);
		 */
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = {
				COLUMN_ID,
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

