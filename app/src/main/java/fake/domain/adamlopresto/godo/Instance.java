package fake.domain.adamlopresto.godo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class Instance {

    private final DatabaseHelper helper;
    private final Task task;
    private boolean dirty = true;
    private boolean needsRepeat = false;
    private long id = -1L;
    private String notes;
    private Date startDate;
    private boolean hasStartTime;
    private Date planDate;
    private boolean hasPlanTime;
    private Date dueDate;
    private boolean hasDueTime;
    @Nullable
    private Date doneDate;

// --Commented out by Inspection START (4/5/2014 1:24 PM):
//    private Date createDate;
// --Commented out by Inspection STOP (4/5/2014 1:24 PM)

    public Instance(DatabaseHelper helper, @NonNull Context context) {
        this(helper, new Task(helper, context));
    }

    public Instance(DatabaseHelper helper, Task task) {
        this.helper = helper;
        this.task = task;
    }

    private Instance(DatabaseHelper helper, long id, Task task, String notes,
                     Date startDate, boolean hasStartTime, Date planDate, boolean hasPlanTime,
                     Date dueDate, boolean hasDueTime,
                     @Nullable Date doneDate) {
        this.helper = helper;
        this.id = id;
        this.notes = notes;
        this.task = task;
        this.startDate = startDate;
        this.hasStartTime = hasStartTime;
        this.planDate = planDate;
        this.hasPlanTime = hasPlanTime;
        this.dueDate = dueDate;
        this.hasDueTime = hasDueTime;
        this.doneDate = doneDate;
        //this.createDate = createDate;
    }


    public static Instance createFromName(DatabaseHelper helper, Context context, String name){
        name = Character.toTitleCase(name.charAt(0)) + name.substring(1);
        Task task;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TasksTable.TABLE,
                new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES,
                        TasksTable.COLUMN_NOTIFICATION, TasksTable.COLUMN_REPEAT,
                        TasksTable.COLUMN_DUE_NOTIFICATION, TasksTable.COLUMN_ID},
                "? like "+TasksTable.COLUMN_NAME+"||'%' AND "+TasksTable.COLUMN_REPEAT+"=2",
                new String[]{name}, null, null, null
        );
        if (!c.moveToFirst()){
            task = new Task(helper, context, name);
        } else {
            task = new Task(helper, c.getLong(5), c.getString(0), c.getString(1),
                    NotificationLevels.values()[c.getInt(2)], RepeatTypes.values()[c.getInt(3)],
                    NotificationLevels.values()[c.getInt(4)]);
        }
        c.close();

        Instance instance = new Instance(helper, task);

        String taskName = task.getName().toString();
        String lowerCase = taskName.toLowerCase();
        GregorianCalendar cal = new GregorianCalendar();
        if (lowerCase.contains("today"))
            instance.setDueDate(cal.getTime());
        else if (lowerCase.contains("tomorrow")){
            cal.add(Calendar.DATE, 1);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("sunday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.SUNDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("monday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.MONDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("tuesday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.TUESDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("wednesday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.WEDNESDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("thursday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.THURSDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("friday")){
            Utils.advanceCalendarToNextWeekday(cal, Calendar.FRIDAY);
            instance.setDueDate(cal.getTime());
        } else if (lowerCase.contains("saturday")) {
            Utils.advanceCalendarToNextWeekday(cal, Calendar.SATURDAY);
            instance.setDueDate(cal.getTime());
        }
        if (taskName.length() < name.length()){
            name = name.substring(taskName.length()).trim();
            instance.setNotes(name);
        }
        return instance;
    }

    @NonNull
    public static Instance get(@NonNull DatabaseHelper helper, long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(InstancesTable.TABLE,
                new String[]{InstancesTable.COLUMN_TASK, InstancesTable.COLUMN_NOTES,
                        InstancesTable.COLUMN_START_DATE, InstancesTable.COLUMN_PLAN_DATE,
                        InstancesTable.COLUMN_DUE_DATE, InstancesTable.COLUMN_DONE_DATE},
                InstancesTable.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null
        );
        if (!c.moveToFirst())
            throw new IllegalArgumentException("No instance with id " + id);

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

        long taskId = c.getLong(0);
        String notes = c.getString(1);
        c.close();

        return new Instance(helper, id, Task.get(helper, taskId), notes, startDate, hasStartTime,
                planDate, hasPlanTime, dueDate, hasDueTime, doneDate);
    }

    @Nullable
    private static Date getDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            return DatabaseHelper.dateTimeFormatter.parse(dateString);
        } catch (ParseException ignored) {
            try {
                return DatabaseHelper.dateFormatter.parse(dateString);
            } catch (ParseException ignore) {
                return null;
            }
        }
    }

    private static boolean hasTime(@Nullable CharSequence dateString) {
        return dateString != null && dateString.length() > 10;
    }

    private static void putDate(@NonNull ContentValues values, String key, @Nullable Date date, boolean hasTime) {
        if (date == null)
            values.putNull(key);
        else
            values.put(key, hasTime ? DatabaseHelper.dateTimeFormatter.format(date)
                                    : DatabaseHelper.dateFormatter.format(date));
    }

    private boolean different(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == null && o2 == null)
            return false;
        if (o1 == null || !o1.equals(o2)) {
            dirty = true;
            return true;
        }
        return false;
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

    public CharSequence getNotes() {
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

    public void setHasStartTime(boolean b) {
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

    public void setHasPlanTime(boolean b) {
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

    public void setHasDueTime(boolean b) {
        if (b != hasDueTime)
            dirty = true;
        hasDueTime = b;
    }

    @Nullable
    public Date getDoneDate() {
        return doneDate;
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

// --Commented out by Inspection START (9/6/2014 1:03 PM):
//    public void setDoneDate(@Nullable Date doneDate) {
//        if (different(this.doneDate, doneDate))
//            this.doneDate = doneDate;
//    }
// --Commented out by Inspection STOP (9/6/2014 1:03 PM)

    /**
     * Pushes any updates to the database.
     * Happens in the background without blocking the calling thread.
     */
    public void flush() {
        if (dirty) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    flushNow();
                }
            }).start();
        }
    }

    public void flushNow() {
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
            db.update(InstancesTable.TABLE, values, InstancesTable.COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)});

        if (needsRepeat) {
            if (RepeatTypes.AUTOMATIC == task.getRepeat()) {
                task.createRepetition(this);
            }
        }

        GoDoAppWidget.updateAllAppWidgets(helper.getContext());
        helper.notifyChange(GoDoContentProvider.INSTANCES_URI);
        dirty = false;
        needsRepeat = false;
    }

    public void updateDone(boolean checked) {
        if (checked) {
            if (doneDate == null) {
                doneDate = new Date();
                dirty = true;
                needsRepeat = true;
            }
        } else {
            needsRepeat = false;
            if (doneDate != null) {
                doneDate = null;
                dirty = true;
            }
        }
    }
}
