package fake.domain.adamlopresto.godo.db;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "GoDo";
	private static final int CURRENT_VERSION = 2;
	/*
	 * Version history:
	 * 1: initial release
	 * 2: add repetition rules
	 */	
	
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	
	private static DatabaseHelper mInstance;
	
	public static DatabaseHelper getInstance(Context ctx) {
		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (mInstance == null) {
			mInstance = new DatabaseHelper(ctx.getApplicationContext());
		}
		return mInstance;
	}	
	
	
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, CURRENT_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		TasksTable.onCreate(db);
		InstancesTable.onCreate(db);
		ContextsTable.onCreate(db);
		TaskContextTable.onCreate(db);
		InstanceDependencyTable.onCreate(db);
		InstancesView.onCreate(db);
		RepetitionRulesTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		TasksTable.onUpgrade(db, oldVersion, newVersion);
		InstancesTable.onUpgrade(db, oldVersion, newVersion);
		ContextsTable.onUpgrade(db, oldVersion, newVersion);
		TaskContextTable.onUpgrade(db, oldVersion, newVersion);
		InstanceDependencyTable.onUpgrade(db, oldVersion, newVersion);
		InstancesView.onUpgrade(db, oldVersion, newVersion);
		RepetitionRulesTable.onUpgrade(db, oldVersion, newVersion);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		db.execSQL("PRAGMA foreign_keys = ON;");
	}

}
