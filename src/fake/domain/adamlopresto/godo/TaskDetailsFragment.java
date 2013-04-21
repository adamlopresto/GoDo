package fake.domain.adamlopresto.godo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class TaskDetailsFragment extends Fragment {
	
	private CheckBox done;
	private EditText taskName;
	private EditText taskNotes;
	private EditText instanceNotes;
	private TextView startDate;
	private TextView planDate;
	private TextView dueDate;
	private Spinner notification;
	private Date doneDate;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_task_details, group, false);
		
		done          = (CheckBox) v.findViewById(R.id.check);
		taskName      = (EditText) v.findViewById(R.id.task_name);
		taskNotes     = (EditText) v.findViewById(R.id.task_notes);
		instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
		startDate     = (TextView) v.findViewById(R.id.start_date);
		planDate      = (TextView) v.findViewById(R.id.plan_date);
		dueDate       = (TextView) v.findViewById(R.id.due_date);
		notification  = (Spinner)  v.findViewById(R.id.notification);
		
		startDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_START));
		planDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_PLAN));
		dueDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_DUE));
		fillData();
		
		return v;
	}
	private Task getTask(){
		return ((TaskActivity)getActivity()).task;
	}
	
	private Instance getInstance(){
		return ((TaskActivity)getActivity()).instance;
	}
	
	
	private void fillData(){
		extractTaskDetails();
		extractInstanceDetails();
	}
	
	private void extractTaskDetails(){
		Task task = getTask();
		if (task != null){
			taskName.setText(task.getName());
			taskNotes.setText(task.getNotes());
			notification.setSelection(task.getNotification().ordinal());
		}
	}
	
	private void extractInstanceDetails(){
		Instance instance = getInstance();
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
			return "None";
		else {
			return DatabaseHelper.dateFormatter.format(date);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveData();
	}
		
	public void saveData(){
		Task task = getTask();
		
		task.setName(nullString(taskName));
		task.setNotes(nullString(taskNotes));
		task.setNotification(NotificationLevels.values()[notification.getSelectedItemPosition()]);
		task.flushNow();
		
		Instance instance = getInstance();
		
		instance.setNotes(nullString(instanceNotes));
		instance.updateDone(done.isChecked());
		
		instance.flushNow();
		
	}
	
	private String nullString(EditText in){
		String out;
		if ("".equals(out = in.getText().toString()))
			return null;
		return out;
	}
	
	private class DateOnClickListener implements View.OnClickListener {

		private RepetitionRuleColumns col;
		private Calendar cal = GregorianCalendar.getInstance();
		private boolean confirm = false;
		
		DateOnClickListener(RepetitionRuleColumns col){
			this.col = col;
		}
		
		@Override
		public void onClick(View v) {
			Date date = null;
			confirm = false;
			switch (col){
			case NEW_START:
				date = getInstance().getStartDate();
				break;
			case NEW_PLAN:
				date = getInstance().getPlanDate();
				break;
			case NEW_DUE:
				date = getInstance().getDueDate();
				break;
			default:
			}
			
			if (date != null)
				cal.setTime(date);
			
			DatePickerDialog dlg = new DatePickerDialog(getActivity(), 
					new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
					Log.e("GoDo", "onDateSet: confirm "+confirm);
					if (confirm){
						cal.set(year,  monthOfYear, dayOfMonth);
						switch(col){			
						case NEW_START:
							getInstance().setStartDate(cal.getTime());
							break;
						case NEW_PLAN:
							getInstance().setPlanDate(cal.getTime());
							break;
						case NEW_DUE:
							getInstance().setDueDate(cal.getTime());
							break;
						default:
						}
					}
				}
			}, 
			cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			DatePicker dp = dlg.getDatePicker();
			dp.setSpinnersShown(false);
			dp.setCalendarViewShown(true);
			dp.getCalendarView().setShowWeekNumber(false);
			dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//this is called *BEFORE* the onDateSet, meaning we can't get the current date here.
					//Who thought that was a good idea?
					Log.e("GoDo", "Done");
					confirm = true;
				}
			});
			dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener)null);
			dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "None", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.e("GoDo", "None");
					switch(col){			
					case NEW_START:
						getInstance().setStartDate(null);
						break;
					case NEW_PLAN:
						getInstance().setPlanDate(null);
						break;
					case NEW_DUE:
						getInstance().setDueDate(null);
						break;
					default:
					}
				}
			});
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					extractInstanceDetails();
				}
			});
			dlg.show();
		}
	}
}