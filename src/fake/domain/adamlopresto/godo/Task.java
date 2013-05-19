package fake.domain.adamlopresto.godo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.TasksTable;

/**
 * Abstract object to represent a task in the database. Provides a handle that can be passed around.
 */
public class Task {

	private static LruCache<Long, Task> cache = new LruCache<Long, Task>(10);
	
	private DatabaseHelper helper;
	
	private boolean dirty = false;
	private long id = -1L;
	private String name;
	private String notes;
	private NotificationLevels notification = NotificationLevels.SILENT;
	private RepeatTypes repeat = RepeatTypes.NONE;
	
	public static Task get(DatabaseHelper helper, long id){
		Task task = cache.get(Long.valueOf(id));
		if (task != null)
			return task;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.query(TasksTable.TABLE, 
				new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES, 
				             TasksTable.COLUMN_NOTIFICATION, TasksTable.COLUMN_REPEAT}, 
				TasksTable.COLUMN_ID+"=?", new String[]{String.valueOf(id)}, null, null, null);
		if (!c.moveToFirst()){
			return null;
		}
		task = new Task(helper, id, c.getString(0), c.getString(1), 
				NotificationLevels.values()[c.getInt(2)], RepeatTypes.values()[c.getInt(3)]);
		cache.put(Long.valueOf(id), task);
		return task;
	}
	
	/*
	 * Constructor to create a new, empty task
	 */
	public Task(DatabaseHelper helper) {
		this.helper=helper;
	}
	
	private Task(DatabaseHelper helper, long id, String name, String notes, 
			NotificationLevels notification, RepeatTypes repeat){
		this.helper = helper;
		this.id=id;
		this.name=name;
		this.notes=notes;
		this.notification=notification;
		this.repeat=repeat;
	}
	
	/**
	 * Gets the current id, as recorded in the database. Returns -1 for a new Task not yet saved
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Gets the current id. If there isn't one yet, it first attempts to write the task to the 
	 * database, and only returns -1 if the task cannot be written (usually empty name).
	 * @return the id;
	 */
	public long forceId() {
		if (id == -1L)
			flushNow();
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		dirty = true;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
		dirty = true;
	}

	public NotificationLevels getNotification() {
		return notification;
	}

	public void setNotification(NotificationLevels notification) {
		this.notification = notification;
		dirty = true;
	}

	public RepeatTypes getRepeat() {
		return repeat;
	}

	public void setRepeat(RepeatTypes repeat) {
		this.repeat = repeat;
		dirty = true;
	}
	
	/**
	 * Pushes any updates to the database. 
	 * Happens in the background without blocking the calling thread.
	 */
	public void flush(){
		if (dirty){
			new Thread(new Runnable(){
				@Override
				public void run() {
					flushNow();
				}
			}).start();
		}
	}
	
	/**
	 * Write changes to database immediately.
	 */
	public void flushNow(){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues(4);
		values.put(TasksTable.COLUMN_NAME, name);
		values.put(TasksTable.COLUMN_NOTES, notes);
		values.put(TasksTable.COLUMN_NOTIFICATION, notification.ordinal());
		values.put(TasksTable.COLUMN_REPEAT, repeat.ordinal());

		if (id == -1L)
			id = db.insert(TasksTable.TABLE, null, values);
		else
			db.update(TasksTable.TABLE, values, TasksTable.COLUMN_ID+"=?", 
					new String[]{String.valueOf(id)});
		dirty=false;
	}

}
