package fake.domain.adamlopresto.godo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

public class Instance {

	private DatabaseHelper helper;
	private boolean dirty = true;
	private boolean needsRepeat = false;
	private long id=-1L;
	private Task task;
	private String notes;
	private Date startDate;
	private boolean hasStartTime;
	private Date planDate;
	private boolean hasPlanTime;
	private Date dueDate;
	private boolean hasDueTime;
	private Date doneDate;
	
	@SuppressWarnings("unused")
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
		String s = c.getString(2);
		Date startDate = getDate(s);
		boolean hasStartTime = hasTime(s);
		
		s = c.getString(3);
		Date planDate = getDate(s);
		boolean hasPlanTime = hasTime(s);
		
		s = c.getString(4);
		Date dueDate = getDate(s);
		boolean hasDueTime = hasTime(s);
		
		s = c.getString(5);
		Date doneDate = getDate(s);
		
		s = c.getString(6);
		Date createDate = getDate(s);
		
		return new Instance(helper, id, Task.get(helper, c.getLong(0)), c.getString(1),
				startDate, hasStartTime, planDate, hasPlanTime, dueDate, hasDueTime,
				doneDate, createDate);
	}
	
	private static Date getDate(String dateString){
		try {
			return DatabaseHelper.dateTimeFormatter.parse(dateString);
		} catch (Exception e) {
			try {
				return DatabaseHelper.dateFormatter.parse(dateString);
			} catch (Exception e2) {
				return null;
			}
		}
	}
	
	private static boolean hasTime(String dateString){
		return dateString != null && dateString.length() > 10;
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
	
	public Instance(DatabaseHelper helper, Context context) {
		this(helper, new Task(helper, context));
	}
	
	public Instance(DatabaseHelper helper, Task task){
		this.helper=helper;
		this.task = task;
	}
	
	private Instance(DatabaseHelper helper, long id, Task task, String notes, 
			Date startDate, boolean hasStartTime, Date planDate, boolean hasPlanTime, 
			Date dueDate, boolean hasDueTime, 
			Date doneDate, Date createDate){
		this.helper = helper;
		this.id = id;
		this.notes = notes;
		this.task = task;
		this.startDate = startDate;
		this.hasStartTime = hasStartTime;
		this.planDate = planDate;
		this.hasPlanTime = hasPlanTime;
		this.dueDate=dueDate;
		this.hasDueTime = hasDueTime;
		this.doneDate=doneDate;
		this.createDate=createDate;
	}

	public long getId() {
		return id;
	}
	
	public long forceId() {
		if (id == -1L)
			flushNow();
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
	
	public boolean hasStartTime() {
		return hasStartTime;
	}
	
	public void setHasStartTime(boolean b){
		if (b != hasStartTime)
			dirty = true;
		hasStartTime = b;
	}

	public Date getPlanDate() {
		return planDate;
	}

	public void setPlanDate(Date planDate) {
		if (different(this.planDate, planDate))
			this.planDate = planDate;
	}
	
	public boolean hasPlanTime() {
		return hasPlanTime;
	}
	
	public void setHasPlanTime(boolean b){
		if (b != hasPlanTime)
			dirty = true;
		hasPlanTime = b;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		if (different(this.dueDate, dueDate))
			this.dueDate = dueDate;
	}
	
	public boolean hasDueTime() {
		return hasDueTime;
	}
	
	public void setHasDueTime(boolean b){
		if (b != hasDueTime)
			dirty = true;
		hasDueTime = b;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		if (different(this.doneDate, doneDate))
			this.doneDate = doneDate;
	}
	
/*
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		if (different(this.createDate, createDate))
			this.createDate = createDate;
	}
 */
	
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
	
	public void flushNow(){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues(7);
		long taskId = task.forceId();
		if (taskId == -1L)
			return; //Can't save task; abort
		values.put(InstancesTable.COLUMN_TASK, taskId);
		values.put(InstancesTable.COLUMN_NOTES, notes);
		putDate(values, InstancesTable.COLUMN_START_DATE, startDate, hasStartTime);
		putDate(values, InstancesTable.COLUMN_PLAN_DATE, planDate, hasPlanTime);
		putDate(values, InstancesTable.COLUMN_DUE_DATE, dueDate, hasDueTime);
		putDate(values, InstancesTable.COLUMN_DONE_DATE, doneDate, true);
		//putDate(values, InstancesTable.COLUMN_CREATE_DATE, createDate, true);

		if (id == -1L)
			id = db.insert(InstancesTable.TABLE, null, values);
		else
			db.update(InstancesTable.TABLE, values, InstancesTable.COLUMN_ID+"=?", 
					new String[]{String.valueOf(id)});
		
		if (needsRepeat){
			if (RepeatTypes.AUTOMATIC.equals(task.getRepeat())){
				Instance next = new Instance(helper, task);
				Cursor rules = db.query(RepetitionRulesTable.TABLE, 
						new String[]{
							RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO,
							RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE
						}, 
						RepetitionRulesTable.COLUMN_TASK+"=?", 
						new String[]{String.valueOf(task.forceId())}, null, null, null 
						);
				rules.moveToFirst();
				RepetitionRuleColumns[] cols = RepetitionRuleColumns.values();
				RepetitionRuleTypes[] types = RepetitionRuleTypes.values();
				while (!rules.isAfterLast()){
					RepetitionRuleColumns from = cols[rules.getInt(0)];
					RepetitionRuleColumns to = cols[rules.getInt(1)];
					RepetitionRuleTypes type = types[rules.getInt(2)];
					Date date = null;
					boolean hasTime = false;
					switch(from){
					case NEW_DUE:
						date = next.getDueDate();
						hasTime = next.hasDueTime();
						break;
					case NEW_PLAN:
						date = next.getPlanDate();
						hasTime = next.hasPlanTime();
						break;
					case NEW_START:
						date = next.getStartDate();
						hasTime = next.hasStartTime();
						break;
					case NOW:
						hasTime = false;
						break;
					case OLD_DUE:
						date = getDueDate();
						hasTime = hasDueTime();
						break;
					case OLD_PLAN:
						date = getPlanDate();
						hasTime = hasPlanTime();
						break;
					case OLD_START:
						date = getStartDate();
						hasTime = hasStartTime();
						break;
					default:
						break;
					}
					if (date == null){
						date = new Date();
						hasTime = false;
					}
					
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(date);
					
					switch(type){
					case ADD_DAY:
						cal.add(Calendar.DAY_OF_MONTH, rules.getInt(3));
						break;
					case ADD_MONTH:
						cal.add(Calendar.MONTH, rules.getInt(3));
						break;
					case WEEKDAY:
						int step = 1;
						String days = rules.getString(3);
						if (days.startsWith("-"))
							step = -1;
						
						boolean done = false;
						for (int failsafe = 0; failsafe < 7 && !done; failsafe++){
							cal.add(Calendar.DAY_OF_WEEK, step);
							switch (cal.get(Calendar.DAY_OF_WEEK)){
							case Calendar.SUNDAY:
								if (days.contains("Su"))
									done = true;
								break;
							case Calendar.MONDAY:
								if (days.contains("M"))
									done = true;
								break;
							case Calendar.TUESDAY:
								if (days.contains("Tu"))
									done = true;
								break;
							case Calendar.WEDNESDAY:
								if (days.contains("W"))
									done = true;
								break;
							case Calendar.THURSDAY:
								if (days.contains("Th"))
									done = true;
								break;
							case Calendar.FRIDAY:
								if (days.contains("F"))
									done = true;
								break;
							case Calendar.SATURDAY:
								if (days.contains("Sa"))
									done = true;
								break;
							}
						}
						break;
					case ADD_WEEK:
						cal.add(Calendar.DAY_OF_MONTH, 7*rules.getInt(3));
						break;
					case ADD_YEAR:
						cal.add(Calendar.MONTH, 12*rules.getInt(3));
						break;
					default:
						break;
					}
					
					date = cal.getTime();
					
					switch(to){
					case NEW_DUE:
						next.setDueDate(date);
						next.setHasDueTime(hasTime);
						break;
					case NEW_PLAN:
						next.setPlanDate(date);
						next.setHasPlanTime(hasTime);
						break;
					case NEW_START:
						next.setStartDate(date);
						next.setHasStartTime(hasTime);
						break;
					default:
						break;
					}
					rules.moveToNext();
				}
				rules.close();
				next.flushNow();
			}
		}
		
		helper.notifyChange(GoDoContentProvider.INSTANCES_URI);
		dirty=false;
		needsRepeat = false;
	}
				
	
	
	private static void putDate(ContentValues values, String key, Date date, boolean hasTime){
		if (date == null)
			values.putNull(key);
		else if (hasTime)
			values.put(key, DatabaseHelper.dateTimeFormatter.format(date));
		else
			values.put(key, DatabaseHelper.dateFormatter.format(date));
	}

	public void updateDone(boolean checked) {
		if (checked){
			if (doneDate == null){
				doneDate = new Date();
				dirty = true;
				needsRepeat = true;
			}
		} else {
			needsRepeat = false;
			if (doneDate != null){
				doneDate = null;
				dirty = true;
			}
		}
	}
}
