package fake.domain.adamlopresto.godo;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class TaskDetailsFragment extends Fragment {
	
	private long task_id = -1L;
	private long instance_id = -1L;
	private CheckBox done;
	private EditText taskName;
	private EditText taskNotes;
	private EditText instanceNotes;
	private EditText startDate;
	private EditText planDate;
	private EditText dueDate;
	private String doneDate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null){
			task_id = args.getLong("task_id", -1L);
			instance_id = args.getLong("instance_id", -1L);
		}
		View v = inflater.inflate(R.layout.fragment_task_details, group, false);
		
		done          = (CheckBox) v.findViewById(R.id.check);
		taskName      = (EditText) v.findViewById(R.id.task_name);
		taskNotes     = (EditText) v.findViewById(R.id.task_notes);
		instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
		startDate     = (EditText) v.findViewById(R.id.start_date);
		planDate     = (EditText) v.findViewById(R.id.plan_date);
		dueDate     = (EditText) v.findViewById(R.id.due_date);
		
		/*
		final Calendar cal = Calendar.getInstance();
		startDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				DatePickerDialog dlg = new DatePickerDialog(getActivity(), 
						new DatePickerDialog.OnDateSetListener() {
							
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear,
									int dayOfMonth) {
								startDate.setText(DatabaseHelper.dateFormatter.format(new Date(year-1900, monthOfYear, dayOfMonth)));
								
							}
						}, 
						cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				DatePicker dp = dlg.getDatePicker();
				dp.setSpinnersShown(false);
				dp.setCalendarViewShown(true);
				dp.getCalendarView().setShowWeekNumber(false);
				dlg.show();
			}
			
		});
		*/
		
		fillData();
		
		return v;
	}
	
	private void fillData(){
		if (instance_id == -1L) {
			if (task_id == -1L){
				//no task, no instance, no data to fill
				return;
			}
			//We have a task, but are creating a new instance for it.
			Uri uri = Uri.withAppendedPath(GoDoContentProvider.TASKS_URI, String.valueOf(task_id));
			String[] projection = new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES};
			
			Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);
			if (c == null || !c.moveToFirst()){
				return;
			}
			
			extractTaskDetails(c);
		} else {
			//have an instance, whether or not we have a task
		
		Uri uri = Uri.withAppendedPath(GoDoContentProvider.INSTANCES_URI, String.valueOf(instance_id));
		Log.e("GoDo", uri.toString());
		String[] projection = new String[]{InstancesView.COLUMN_TASK,
				InstancesView.COLUMN_TASK_NAME, InstancesView.COLUMN_TASK_NOTES,
				InstancesView.COLUMN_INSTANCE_NOTES, InstancesView.COLUMN_DONE_DATE, 
				InstancesView.COLUMN_START_DATE, InstancesView.COLUMN_PLAN_DATE, 
				InstancesView.COLUMN_DUE_DATE};
		Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);
		if (c == null || !c.moveToFirst()){
			return;
		}
		
		extractTaskDetails(c);
		extractInstanceDetails(c);
		/*
		if (c.isNull(5)){
			startDate.setEnabled(false);
		} else {
			Date date;
			try {
				startDate.setEnabled(true);
				date = DatabaseHelper.dateFormatter.parse(c.getString(5));
				GregorianCalendar cal = new GregorianCalendar(); cal.setTime(date);
				startDate.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			} catch (ParseException e) {
				startDate.setEnabled(false);
			}
		}
		*/
		}
	}
	
	private void extractTaskDetails(Cursor c){
		taskName.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_TASK_NAME)));
		taskNotes.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_TASK_NOTES)));
	}
	
	private void extractInstanceDetails(Cursor c){
		task_id = c.getLong(c.getColumnIndexOrThrow(InstancesView.COLUMN_TASK));
		instanceNotes.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_INSTANCE_NOTES)));
		int doneCol = c.getColumnIndexOrThrow(InstancesView.COLUMN_DONE_DATE);
		if (c.isNull(doneCol)){
			done.setChecked(false);
			doneDate = null;
		} else {
			doneDate = c.getString(doneCol);
			done.setChecked(true);
		}
		startDate.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_START_DATE)));
		planDate.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_PLAN_DATE)));
		dueDate.setText(c.getString(c.getColumnIndexOrThrow(InstancesView.COLUMN_DUE_DATE)));
	}
	
	@Override
	public void onPause(){
		ContentValues cv = new ContentValues();
		cv.put(TasksTable.COLUMN_NAME, taskName.getText().toString());
		cv.put(TasksTable.COLUMN_NOTES, taskNotes.getText().toString());
		
		Uri uri;
		
		if (task_id == -1L){
			uri = GoDoContentProvider.TASKS_URI;
			((TaskActivity)getActivity()).task_id = task_id = Long.valueOf(getActivity().getContentResolver().insert(uri, cv).getLastPathSegment());
		} else {
			uri = Uri.withAppendedPath(GoDoContentProvider.TASKS_URI, String.valueOf(task_id));
			getActivity().getContentResolver().update(uri, cv, null, null);
		}
		
		cv.clear();
		cv.put(InstancesTable.COLUMN_NOTES, instanceNotes.getText().toString());
		cv.put(InstancesTable.COLUMN_START_DATE, startDate.getText().toString());
		cv.put(InstancesTable.COLUMN_PLAN_DATE, planDate.getText().toString());
		cv.put(InstancesTable.COLUMN_DUE_DATE, dueDate.getText().toString());
		if (done.isChecked() && doneDate == null){
			doneDate = DatabaseHelper.dateFormatter.format(new Date());
			cv.put(InstancesTable.COLUMN_DONE_DATE, doneDate);
		} else if (!done.isChecked() && doneDate != null){
			doneDate = null;
			cv.putNull(InstancesTable.COLUMN_DONE_DATE);
		}
		
		if (instance_id == -1L){
			cv.put(InstancesTable.COLUMN_TASK, task_id);
			uri = GoDoContentProvider.INSTANCES_URI;
			((TaskActivity)getActivity()).instance_id = instance_id = Long.valueOf(getActivity().getContentResolver().insert(uri, cv).getLastPathSegment());
		} else {
			uri = Uri.withAppendedPath(GoDoContentProvider.INSTANCES_URI, String.valueOf(instance_id));
			getActivity().getContentResolver().update(uri, cv, null, null);
		}
		super.onPause();
	}
}
