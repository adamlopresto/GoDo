package fake.domain.adamlopresto.godo;

import java.text.ParseException;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class TaskDetailsFragment extends Fragment {
	
	private CheckBox done;
	private EditText taskName;
	private EditText taskNotes;
	private EditText instanceNotes;
	private EditText startDate;
	private EditText planDate;
	private EditText dueDate;
	private Spinner notification;
	private Date doneDate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_task_details, group, false);
		
		done          = (CheckBox) v.findViewById(R.id.check);
		taskName      = (EditText) v.findViewById(R.id.task_name);
		taskNotes     = (EditText) v.findViewById(R.id.task_notes);
		instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
		startDate     = (EditText) v.findViewById(R.id.start_date);
		planDate      = (EditText) v.findViewById(R.id.plan_date);
		dueDate       = (EditText) v.findViewById(R.id.due_date);
		notification  = (Spinner)  v.findViewById(R.id.notification);
		
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
		extractTaskDetails();
		extractInstanceDetails();
	}
	
	private void extractTaskDetails(){
		Task task = ((TaskActivity)getActivity()).task;
		if (task != null){
			taskName.setText(task.getName());
			taskNotes.setText(task.getNotes());
			notification.setSelection(task.getNotification().ordinal());
		}
	}
	
	private void extractInstanceDetails(){
		Instance instance = ((TaskActivity)getActivity()).instance;
		if (instance != null){
			instanceNotes.setText(instance.getNotes());
			doneDate = instance.getDoneDate();
			done.setChecked(doneDate != null);
			
			startDate.setText(dateString(instance.getStartDate()));
			planDate.setText(dateString(instance.getPlanDate()));
			dueDate.setText(dateString(instance.getDueDate()));
		}
	}
		
	private String dateString(Date date){
		if (date == null)
			return "";
		else 
			return DatabaseHelper.dateFormatter.format(date);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveData();
	}
		
	public void saveData(){
		DatabaseHelper helper = null;
		Task task = ((TaskActivity)getActivity()).task;
		if (task == null){
			helper = new DatabaseHelper(getActivity());
			task = ((TaskActivity)getActivity()).task = new Task(helper);
		}
		
		task.setName(nullString(taskName));
		task.setNotes(nullString(taskNotes));
		task.setNotification(NotificationLevels.values()[notification.getSelectedItemPosition()]);
		task.flushNow();
		
		Instance instance = ((TaskActivity)getActivity()).instance;
		if (instance == null){
			if (helper == null)
				helper = new DatabaseHelper(getActivity());
			instance = ((TaskActivity)getActivity()).instance = new Instance(helper, task);
		}
		
		instance.setNotes(nullString(instanceNotes));
		instance.setStartDate(nullDate(startDate));
		instance.setPlanDate(nullDate(planDate));
		instance.setDueDate(nullDate(dueDate));
		instance.updateDone(done.isChecked());
		
		instance.flush();
		
	}
	
	private String nullString(EditText in){
		String out;
		if ("".equals(out = in.getText().toString()))
			return null;
		return out;
	}
	
	private Date nullDate(EditText in){
		String s;
		if ("".equals(s = in.getText().toString()))
			return null;
		try {
			return DatabaseHelper.dateFormatter.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}
	
}