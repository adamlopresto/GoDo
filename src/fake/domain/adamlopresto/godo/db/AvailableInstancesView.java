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
	public static final String COLUMN_TASK_NOTES = TasksTable.COLUMN_NOTES;
	public static final String COLUMN_INSTANCE_NOTES = InstancesTable.COLUMN_NOTES;
	public static final String COLUMN_NOTIFICATION = TasksTable.COLUMN_NOTIFICATION;
	public static final String COLUMN_REPEAT = TasksTable.COLUMN_REPEAT;
	public static final String COLUMN_DONE_DATE = InstancesTable.COLUMN_DONE_DATE;
	public static final String COLUMN_DUE_DATE = InstancesTable.COLUMN_DUE_DATE;
	public static final String COLUMN_START_DATE = InstancesTable.COLUMN_START_DATE;
	public static final String COLUMN_PLAN_DATE = InstancesTable.COLUMN_PLAN_DATE;
	public static final String COLUMN_CREATE_DATE = InstancesTable.COLUMN_CREATE_DATE;
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE VIEW "+VIEW
				+ " AS SELECT " 
				+ "i._id AS "+COLUMN_ID + ", "
				+ COLUMN_TASK + ", "
				+ COLUMN_TASK_NAME + ", "
				+ COLUMN_TASK_NOTES + ", "
				+ COLUMN_INSTANCE_NOTES + ", "
				+ COLUMN_NOTIFICATION + ", "
				+ COLUMN_REPEAT + ", "
				+ COLUMN_DONE_DATE + ", "
				+ COLUMN_DUE_DATE + ", "
				+ COLUMN_START_DATE + ", "
				+ COLUMN_PLAN_DATE + ", "
				+ COLUMN_CREATE_DATE
				+ " from instances i inner join tasks t on i.task=t._id " +
				"where not exists " +
				"(select * from task_context tc inner join contexts c " +
				" on tc.context = c._id where tc.task=i.task and c.active <> 1)"
				);
		
		/*
		select * from instances i inner join tasks t on i.task=t._id where not exists (select * from task_context tc inner join contexts c on tc.context = c._id where tc.task=i.task and c.active <> 1);
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

