package fake.domain.adamlopresto.godo;

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
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class TaskDetailsFragment extends Fragment {
	
	private long task_id;
	private long instance_id;
	private CheckBox done;
	private EditText taskName;
	private EditText taskNotes;
	private EditText instanceNotes;
	private EditText startDate;
	private EditText planDate;
	private EditText dueDate;
	
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
				// TODO Auto-generated method stub
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
		if (instance_id == -1)
			return;
		
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
		
		task_id = c.getLong(0);
		taskName.setText(c.getString(1));
		taskNotes.setText(c.getString(2));
		instanceNotes.setText(c.getString(3));
		done.setChecked(!c.isNull(4));
		startDate.setText(c.getString(5));
		planDate.setText(c.getString(6));
		dueDate.setText(c.getString(7));
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
				// TODO Auto-generated catch block
				startDate.setEnabled(false);
			}
		}
		*/
	}
	
	@Override
	public void onPause(){
		super.onPause();
		ContentValues cv = new ContentValues();
		cv.put(TasksTable.COLUMN_NAME, taskName.getText().toString());
		cv.put(TasksTable.COLUMN_NOTES, taskNotes.getText().toString());
		Uri uri = Uri.withAppendedPath(GoDoContentProvider.TASKS_URI, String.valueOf(task_id));
		getActivity().getContentResolver().update(uri, cv, null, null);
		
		cv.clear();
		cv.put(InstancesTable.COLUMN_NOTES, instanceNotes.getText().toString());
		cv.put(InstancesTable.COLUMN_START_DATE, startDate.getText().toString());
		cv.put(InstancesTable.COLUMN_PLAN_DATE, planDate.getText().toString());
		cv.put(InstancesTable.COLUMN_DUE_DATE, dueDate.getText().toString());
		uri = Uri.withAppendedPath(GoDoContentProvider.INSTANCES_URI, String.valueOf(instance_id));
		getActivity().getContentResolver().update(uri, cv, null, null);
		
	}
}
