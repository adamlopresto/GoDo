package fake.domain.adamlopresto.godo;

import java.text.ParseException;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesTable;

public class Instance {

	private DatabaseHelper helper;
	private boolean dirty = false;
	private long id=-1L;
	private Task task;
	private String notes;
	private Date startDate;
	private Date planDate;
	private Date dueDate;
	private Date doneDate;
	private Date createDate;
	
	public static Instance get(DatabaseHelper helper, long id){
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.query(InstancesTable.TABLE, 
				new String[]{InstancesTable.COLUMN_TASK, InstancesTable.COLUMN_NOTES, 
				             InstancesTable.COLUMN_START_DATE, InstancesTable.COLUMN_PLAN_DATE,
				             InstancesTable.COLUMN_DUE_DATE, InstancesTable.COLUMN_DONE_DATE,
				             InstancesTable.COLUMN_CREATE_DATE}, 
				InstancesTable.COLUMN_ID+"=?", new String[]{String.valueOf(id)}, null, null, null);
		if (!c.moveToFirst()){
			return null;
		}
		return new Instance(helper, id, Task.get(helper, c.getLong(0)), c.getString(1),
				getDate(c, 2), getDate(c, 3), getDate(c, 4), getDate(c, 5), getDate(c, 6));
	}
	
	private static Date getDate(Cursor c, int col){
		if (c.isNull(col))
			return null;
		try {
			return DatabaseHelper.dateFormatter.parse(c.getString(col));
		} catch (ParseException e) {
			return null;
		}
	}
	
	private boolean different(Object o1, Object o2){
		if (o1 == null && o2 == null)
			return false;
		if (o1 == null || !o1.equals(o2)){
			dirty = true;
			return true;
		}
		return false;
	}
	
	public Instance(DatabaseHelper helper) {
		this.helper=helper;
	}
	
	public Instance(DatabaseHelper helper, Task task){
		this.helper=helper;
		this.task = task;
	}
	
	private Instance(DatabaseHelper helper, long id, Task task, String notes, Date startDate, 
			Date planDate, Date dueDate, Date doneDate, Date createDate){
		this.helper = helper;
		this.id = id;
		this.notes = notes;
		this.task = task;
		this.startDate = startDate;
		this.planDate = planDate;
		this.dueDate=dueDate;
		this.doneDate=doneDate;
		this.createDate=createDate;
	}

	public long getId() {
		return id;
	}

	public Task getTask() {
		return task;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		if (different(this.notes, notes))
			this.notes = notes;
	}

	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		if (different(this.startDate, startDate))
			this.startDate = startDate;
	}

	public Date getPlanDate() {
		return planDate;
	}

	public void setPlanDate(Date planDate) {
		if (different(this.startDate, startDate))
			this.planDate = planDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		if (different(this.startDate, startDate))
			this.dueDate = dueDate;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		if (different(this.startDate, startDate))
			this.doneDate = doneDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		if (different(this.startDate, startDate))
			this.createDate = createDate;
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
					SQLiteDatabase db = helper.getWritableDatabase();
					ContentValues values = new ContentValues(7);
					values.put(InstancesTable.COLUMN_TASK, task.getId());
					values.put(InstancesTable.COLUMN_NOTES, notes);
					putDate(values, InstancesTable.COLUMN_START_DATE, startDate);
					putDate(values, InstancesTable.COLUMN_PLAN_DATE, planDate);
					putDate(values, InstancesTable.COLUMN_DUE_DATE, dueDate);
					putDate(values, InstancesTable.COLUMN_DONE_DATE, doneDate);
					putDate(values, InstancesTable.COLUMN_CREATE_DATE, createDate);
					
					if (id == -1L)
						id = db.insert(InstancesTable.TABLE, null, values);
					else
						db.update(InstancesTable.TABLE, values, InstancesTable.COLUMN_ID+"=?", 
								new String[]{String.valueOf(id)});
					db.close();
					dirty=false;
				}
			}).start();
		}
	}
	
	private static void putDate(ContentValues values, String key, Date date){
		if (date == null)
			values.putNull(key);
		else 
			values.put(key, DatabaseHelper.dateFormatter.format(date));
	}

	public void updateDone(boolean checked) {
		if (checked){
			if (doneDate == null){
				doneDate = new Date();
				dirty = true;
			}
		} else {
			if (doneDate != null){
				doneDate = null;
				dirty = true;
			}
		}
	}
}
