package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class TaskAdapter extends ResourceCursorAdapter {

	@SuppressWarnings("unused")
	private static final int ID = 0;
	private static final int TASK_NAME = 1;
	private static final int TASK_NOTES = 2;
	private static final int INSTANCE_NOTES = 3;
	private static final int DUE_DATE = 4;
	private static final int PLAN_DATE = 5;
	private static final int DONE_DATE = 6;
	
	public TaskAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = super.newView(context, cursor, parent);
		TaskHolder holder = new TaskHolder();
		holder.done = (CheckBox)v.findViewById(R.id.check);
		holder.name = (TextView)v.findViewById(R.id.task_name);
		holder.taskNotes = (TextView)v.findViewById(R.id.task_notes);
		holder.instanceNotes = (TextView)v.findViewById(R.id.instance_notes);
		holder.planDate = (TextView)v.findViewById(R.id.plan_date);
		holder.dueDate = (TextView)v.findViewById(R.id.due_date);
		v.setTag(holder);
		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		TaskHolder holder = (TaskHolder)v.getTag();
		boolean done = !cursor.isNull(DONE_DATE);
		String dueDate = cursor.getString(DUE_DATE);
		boolean overdue = DateCalc.isBeforeNow(dueDate);
		String planDate = cursor.getString(PLAN_DATE);
		boolean future = DateCalc.isAfterNow(planDate);
		holder.done.setChecked(done);
		setTextView(holder.name, cursor.getString(TASK_NAME), done, overdue, future);
		setTextView(holder.taskNotes, cursor.getString(TASK_NOTES), done, overdue, future);
		setTextView(holder.instanceNotes, cursor.getString(INSTANCE_NOTES), done, overdue, future);
		setTextViewDate(holder.dueDate, "D: ",cursor.getString(DUE_DATE), done, overdue, future);
		setTextViewDate(holder.planDate, "P: ",cursor.getString(PLAN_DATE), done, overdue, future);
	}
	
	private void setTextViewDate(TextView v, String prefix, String s, boolean done, boolean overdue,
			boolean future){
		if (!hideView(v, s))
			setTextViewInner(v, prefix+DateCalc.formatShortRelativeDate(s), done, overdue, future);
	}
	
	private void setTextView(TextView v, String s, boolean done, boolean overdue,
			boolean future){
		if (!hideView(v, s))
			setTextViewInner(v, s, done, overdue, future);
		
	}
	private void setTextViewInner(TextView v, String s, boolean done, boolean overdue,
			boolean future){
		
		v.setText(s);
		if (done)
			v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else  
			v.setPaintFlags(v.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG); 
		
		if (overdue)
			v.setTextColor(Color.RED);
		else if (future)
			v.setTextColor(Color.GRAY);
		else
			v.setTextColor(Color.BLACK);
	}
	
	private boolean hideView(TextView v, String s){
		if (TextUtils.isEmpty(s)){
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
