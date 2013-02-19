package fake.domain.adamlopresto.godo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "GoDo";
	private static final int CURRENT_VERSION = 1;
	/*
	 * Version history:
	 * 1: initial release
	 */	

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, CURRENT_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		TasksTable.onCreate(db);
		InstancesTable.onCreate(db);
		ContextsTable.onCreate(db);
		TaskContextTable.onCreate(db);
		InstanceDependencyTable.onCreate(db);
		AvailableInstancesView.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		TasksTable.onUpgrade(db, oldVersion, newVersion);
		InstancesTable.onUpgrade(db, oldVersion, newVersion);
		ContextsTable.onUpgrade(db, oldVersion, newVersion);
		TaskContextTable.onUpgrade(db, oldVersion, newVersion);
		InstanceDependencyTable.onUpgrade(db, oldVersion, newVersion);
		AvailableInstancesView.onUpgrade(db, oldVersion, newVersion);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		db.execSQL("PRAGMA foreign_keys = ON;");
	}

}
