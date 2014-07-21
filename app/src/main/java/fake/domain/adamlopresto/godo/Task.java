package fake.domain.adamlopresto.godo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.LruCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;
import fake.domain.adamlopresto.godo.db.TasksTable;

/**
 * Abstract object to represent a task in the database. Provides a handle that can be passed around.
 */
public class Task {

    private static final LruCache<Long, Task> cache = new LruCache<>(10);

    private final DatabaseHelper helper;

    private boolean dirty = false;
    private long id = -1L;
    private String name;
    private String notes;
    private NotificationLevels notification;
    private NotificationLevels dueNotification;
    private RepeatTypes repeat = RepeatTypes.NONE;

    /*
     * Constructor to create a new, empty task
     */
    public Task(DatabaseHelper helper, @NotNull Context context) {
        this.helper = helper;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        notification = NotificationLevels.valueOf(prefs.getString(SettingsActivity.PREF_DEFAULT_NOTIFICATION, "NONE"));
        dueNotification = NotificationLevels.valueOf(prefs.getString(SettingsActivity.PREF_DEFAULT_DUE_NOTIFICATION, "NONE"));
    }

    public Task(DatabaseHelper helper, @NotNull Context context, String name) {
        this(helper, context);
        this.name = name;
    }

    private Task(DatabaseHelper helper, long id, String name, String notes,
                 NotificationLevels notification, RepeatTypes repeat,
                 NotificationLevels dueNotification) {
        this.helper = helper;
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.notification = notification;
        this.repeat = repeat;
        this.dueNotification = dueNotification;
    }

    @NotNull
    public static Task get(@NotNull DatabaseHelper helper, long id) {
        Task task = cache.get(id);
        if (task != null)
            return task;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TasksTable.TABLE,
                new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES,
                        TasksTable.COLUMN_NOTIFICATION, TasksTable.COLUMN_REPEAT,
                        TasksTable.COLUMN_DUE_NOTIFICATION},
                TasksTable.COLUMN_ID + "=?", Utils.idToSelectionArgs(id), null, null, null
        );
        if (!c.moveToFirst())
            throw new IllegalArgumentException("No task with id " + id);
        task = new Task(helper, id, c.getString(0), c.getString(1),
                NotificationLevels.values()[c.getInt(2)], RepeatTypes.values()[c.getInt(3)],
                NotificationLevels.values()[c.getInt(4)]);
        cache.put(id, task);
        return task;
    }

    /**
     * Gets the current id, as recorded in the database. Returns -1 for a new Task not yet saved
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the current id. If there isn't one yet, it first attempts to write the task to the
     * database, and only returns -1 if the task cannot be written (usually empty name).
     *
     * @return the id;
     */
    public long forceId() {
        if (id == -1L)
            flushNow();
        return id;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        dirty = true;
    }

    public CharSequence getNotes() {
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

    public NotificationLevels getDueNotification() {
        return dueNotification;
    }

    public void setDueNotification(NotificationLevels dueNotification) {
        this.dueNotification = dueNotification;
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
     * Creates a new repetition instance for this task.
     *
     * @param old If not null, then provides values for old dates
     */
    @NotNull
    public Instance createRepetition(@Nullable Instance old) {
        Instance next = new Instance(helper, this);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor rules = db.query(RepetitionRulesTable.TABLE,
                new String[]{
                        RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO,
                        RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE
                },
                RepetitionRulesTable.COLUMN_TASK + "=?",
                Utils.idToSelectionArgs(forceId()), null, null, null
        );
        rules.moveToFirst();
        RepetitionRuleColumns[] cols = RepetitionRuleColumns.values();
        RepetitionRuleTypes[] types = RepetitionRuleTypes.values();
        while (!rules.isAfterLast()) {
            RepetitionRuleColumns from = cols[rules.getInt(0)];
            RepetitionRuleColumns to = cols[rules.getInt(1)];
            RepetitionRuleTypes type = types[rules.getInt(2)];
            @Nullable Date date = null;
            boolean hasTime = false;
            switch (from) {
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
                    date = old == null ? null : old.getDueDate();
                    hasTime = old != null && old.hasDueTime();
                    break;
                case OLD_PLAN:
                    date = old == null ? null : old.getPlanDate();
                    hasTime = old != null && old.hasPlanTime();
                    break;
                case OLD_START:
                    date = old == null ? null : old.getStartDate();
                    hasTime = old != null && old.hasStartTime();
                    break;
                default:
                    break;
            }
            if (date == null) {
                date = new Date();
                hasTime = false;
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);

            switch (type) {
                case ADD_DAY:
                    cal.add(Calendar.DAY_OF_MONTH, rules.getInt(3));
                    break;
                case ADD_MONTH:
                    cal.add(Calendar.MONTH, rules.getInt(3));
                    break;
                case WEEKDAY:
                    int step = 1;
                    String days = rules.getString(3);
                    if (days == null)
                        //noinspection BreakStatement
                        break;

                    if (days.startsWith("-"))
                        step = -1;

                    @SuppressWarnings("BooleanVariableAlwaysNegated")
                    boolean done = false;
                    for (int failsafe = 0; failsafe < 7 && !done; failsafe++) {
                        cal.add(Calendar.DAY_OF_WEEK, step);
                        switch (cal.get(Calendar.DAY_OF_WEEK)) {
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
                    cal.add(Calendar.DAY_OF_MONTH, 7 * rules.getInt(3));
                    break;
                case ADD_YEAR:
                    //noinspection MagicNumber
                    cal.add(Calendar.MONTH, 12 * rules.getInt(3));
                    break;
                default:
                    break;
            }

            date = cal.getTime();

            switch (to) {
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

        //If there's an old instance, then we need to also recreate any dependencies it had
        if (old != null) {
            db.execSQL("insert into instance_dependency (first, second) " +
                    "select max(i1._id) as first, ? as second " +
                    "from instances_view i1 inner join instances i2 on i1.task = i2.task " +
                    "inner join instance_dependency d on i2._id = d.first " +
                    "where d.second = ? group by i2.task", new Long[]{next.getId(), old.getId()});
        }
        return next;
    }

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

    /**
     * Write changes to database immediately.
     */
    public void flushNow() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues(4);
        values.put(TasksTable.COLUMN_NAME, name);
        values.put(TasksTable.COLUMN_NOTES, notes);
        values.put(TasksTable.COLUMN_NOTIFICATION, notification.ordinal());
        values.put(TasksTable.COLUMN_REPEAT, repeat.ordinal());
        values.put(TasksTable.COLUMN_DUE_NOTIFICATION, dueNotification.ordinal());

        if (id == -1L)
            id = db.insert(TasksTable.TABLE, null, values);
        else
            db.update(TasksTable.TABLE, values, TasksTable.COLUMN_ID + "=?",
                    Utils.idToSelectionArgs(id));
        dirty = false;
    }

}
