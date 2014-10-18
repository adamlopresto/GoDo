package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class TaskAdapter extends ResourceCursorAdapter {

    public static final String[] PROJECTION = {"_id", "task_name",
            "task_notes", "instance_notes", "due_date", "plan_date", "done_date"};
    public static final String SORT = "done_date is not null, " +
            "case when due_date <= DATETIME('now', 'localtime') then due_date || ' 23:59:59' else '9999-99-99' end, " +
            "coalesce(plan_date || ' 00:00:00', DATETIME('now', 'localtime')), " +
            "due_date || ' 23:59:59', notification DESC, next_steps DESC, random()";

    @SuppressWarnings("UnusedDeclaration")
    private static final int ID = 0;
    private static final int TASK_NAME = 1;
    private static final int TASK_NOTES = 2;
    private static final int INSTANCE_NOTES = 3;
    private static final int DUE_DATE = 4;
    private static final int PLAN_DATE = 5;
    private static final int DONE_DATE = 6;
    private final boolean showCheckBox;

    public TaskAdapter(@NonNull Context context, Cursor c, boolean showCheckBox) {
        super(context, R.layout.main_list_item, c, 0);
        this.showCheckBox = showCheckBox;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = super.newView(context, cursor, parent);
        assert v != null;
        TaskHolder holder = new TaskHolder();
        holder.done = (CheckBox) v.findViewById(R.id.check);
        holder.done.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
        holder.name = (TextView) v.findViewById(R.id.task_name);
        holder.taskNotes = (TextView) v.findViewById(R.id.task_notes);
        holder.instanceNotes = (TextView) v.findViewById(R.id.instance_notes);
        holder.planDate = (TextView) v.findViewById(R.id.plan_date);
        holder.dueDate = (TextView) v.findViewById(R.id.due_date);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(@NonNull View v, Context context, @NonNull Cursor cursor) {
        TaskHolder holder = (TaskHolder) v.getTag();
        boolean done = !cursor.isNull(DONE_DATE);
        String dueDate = cursor.getString(DUE_DATE);
        boolean overdue = Utils.isBeforeNow(dueDate);
        String planDate = cursor.getString(PLAN_DATE);
        boolean future = Utils.isAfterNow(planDate);
        holder.done.setChecked(done);
        setTextView(holder.name, cursor.getString(TASK_NAME), done, overdue, future);
        setTextView(holder.taskNotes, cursor.getString(TASK_NOTES), done, overdue, future);
        setTextView(holder.instanceNotes, cursor.getString(INSTANCE_NOTES), done, overdue, future);
        setTextViewDate(holder.dueDate, "D: ", cursor.getString(DUE_DATE), done, overdue, future);
        setTextViewDate(holder.planDate, "P: ", cursor.getString(PLAN_DATE), done, overdue, future);
    }

    private void setTextViewDate(@NonNull TextView v, String prefix, @Nullable String s, boolean done, boolean overdue,
                                 boolean future) {
        if (!hideView(v, s))
            setTextViewInner(v, prefix + Utils.formatShortRelativeDate(s), done, overdue, future);
    }

    private void setTextView(@NonNull TextView v, CharSequence s, boolean done, boolean overdue,
                             boolean future) {
        if (!hideView(v, s))
            setTextViewInner(v, s, done, overdue, future);

    }

    private void setTextViewInner(@NonNull TextView v, CharSequence s, boolean done, boolean overdue,
                                  boolean future) {

        v.setText(s);
        //noinspection IfMayBeConditional
        if (done)
            v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            v.setPaintFlags(v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        v.setTextColor(overdue ? Color.RED   :
                       future  ? Color.GRAY  :
                                 Color.BLACK);
    }

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    //@Contract("_, null -> true")
    private static boolean hideView(@NonNull TextView v, CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            v.setVisibility(View.GONE);
            return true;
        }
        v.setVisibility(View.VISIBLE);
        return false;
    }

    private static class TaskHolder {
        public CheckBox done;
        public TextView name;
        public TextView taskNotes;
        public TextView instanceNotes;
        public TextView planDate;
        public TextView dueDate;
    }

}
