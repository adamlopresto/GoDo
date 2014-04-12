package fake.domain.adamlopresto.godo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TaskDetailsFragment extends Fragment {

    private CheckBox done;
    private EditText taskName;
    private EditText taskNotes;
    private EditText instanceNotes;
    private TextView startDate;
    private TextView startTime;
    private TextView planDate;
    private TextView planTime;
    private TextView dueDate;
    private TextView dueTime;
    private Spinner notification;
    private Spinner dueNotification;

    @SuppressWarnings("FieldCanBeLocal")
    private Date doneDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_details, group, false);
        assert v != null;
        done = (CheckBox) v.findViewById(R.id.check);
        taskName = (EditText) v.findViewById(R.id.task_name);
        taskNotes = (EditText) v.findViewById(R.id.task_notes);
        instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
        startDate = (TextView) v.findViewById(R.id.start_date);
        startTime = (TextView) v.findViewById(R.id.start_time);
        planDate = (TextView) v.findViewById(R.id.plan_date);
        planTime = (TextView) v.findViewById(R.id.plan_time);
        dueDate = (TextView) v.findViewById(R.id.due_date);
        dueTime = (TextView) v.findViewById(R.id.due_time);
        notification = (Spinner) v.findViewById(R.id.notification);
        dueNotification = (Spinner) v.findViewById(R.id.due_notification);

        startDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_START));
        planDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_PLAN));
        dueDate.setOnClickListener(new DateOnClickListener(RepetitionRuleColumns.NEW_DUE));
        startTime.setOnClickListener(new TimeOnClickListener(RepetitionRuleColumns.NEW_START));
        planTime.setOnClickListener(new TimeOnClickListener(RepetitionRuleColumns.NEW_PLAN));
        dueTime.setOnClickListener(new TimeOnClickListener(RepetitionRuleColumns.NEW_DUE));
        fillData();

        return v;
    }

    private Task getTask() {
        return ((TaskActivity) getActivity()).task;
    }

    private Instance getInstance() {
        return ((TaskActivity) getActivity()).instance;
    }


    private void fillData() {
        extractTaskDetails();
        extractInstanceDetails();
    }

    private void extractTaskDetails() {
        Task task = getTask();
        if (task != null) {
            taskName.setText(task.getName());
            taskNotes.setText(task.getNotes());
            notification.setSelection(task.getNotification().ordinal());
            dueNotification.setSelection(task.getDueNotification().ordinal());
        }
    }

    private void extractInstanceDetails() {
        Instance instance = getInstance();
        if (instance != null) {
            instanceNotes.setText(instance.getNotes());
            doneDate = instance.getDoneDate();
            done.setChecked(doneDate != null);

            Date date = instance.getStartDate();
            startDate.setText(dateString(date));
            if (date == null) {
                startTime.setVisibility(View.GONE);
            } else {
                startTime.setVisibility(View.VISIBLE);
                startTime.setText(timeString(instance.hasStartTime(), date));
            }

            date = instance.getPlanDate();
            planDate.setText(dateString(date));
            if (date == null) {
                planTime.setVisibility(View.GONE);
            } else {
                planTime.setVisibility(View.VISIBLE);
                planTime.setText(timeString(instance.hasPlanTime(), date));
            }

            date = instance.getDueDate();
            dueDate.setText(dateString(date));
            if (date == null) {
                dueTime.setVisibility(View.GONE);
            } else {
                dueTime.setVisibility(View.VISIBLE);
                dueTime.setText(timeString(instance.hasDueTime(), date));
            }
        }
    }

    private String dateString(Date date) {
        if (date == null)
            return "No date";
        else
            return Utils.formatLongRelativeDate(date);
    }

    private String timeString(boolean hasTime, Date date) {
        if (!hasTime || date == null)
            return "No time";
        else
            return Utils.SHORT_TIME.format(date);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    public void saveData() {
        Task task = getTask();

        task.setName(nullString(taskName));
        task.setNotes(nullString(taskNotes));
        task.setNotification(NotificationLevels.values()[notification.getSelectedItemPosition()]);
        task.setDueNotification(NotificationLevels.values()[dueNotification.getSelectedItemPosition()]);
        task.flushNow();

        Instance instance = getInstance();

        instance.setNotes(nullString(instanceNotes));
        instance.updateDone(done.isChecked());

        instance.flushNow();

    }

    @Nullable
    private String nullString(@Nullable EditText in) {
        if (in == null)
            return null;
        Editable edit = in.getText();
        if (edit == null)
            return null;
        String out = edit.toString();
        if (TextUtils.isEmpty(out))
            return null;
        return out;
    }

    private class DateOnClickListener implements View.OnClickListener {

        private final RepetitionRuleColumns col;
        private Calendar cal = GregorianCalendar.getInstance();

        DateOnClickListener(RepetitionRuleColumns col) {
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            Date date = null;
            switch (col) {
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

            if (date == null)
                cal = GregorianCalendar.getInstance();
            else
                cal.setTime(date);

            DatePickerDialog dlg = new DatePickerDialog(getActivity(), null,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            final DatePicker dp = dlg.getDatePicker();
            assert dp != null;
            dp.setSpinnersShown(false);
            dp.setCalendarViewShown(true);
            dlg.setTitle(null);
            CalendarView cv = dp.getCalendarView();
            if (cv != null) cv.setShowWeekNumber(false);
            dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //noinspection MagicConstant
                    cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                    switch (col) {
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
            });
            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);
            dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "None", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (col) {
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

    private class TimeOnClickListener implements View.OnClickListener {

        private final RepetitionRuleColumns col;
        private Calendar cal = GregorianCalendar.getInstance();
        private boolean confirm = true;

        TimeOnClickListener(RepetitionRuleColumns col) {
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            Date date = null;
            confirm = true;
            Boolean hasTime = true;
            switch (col) {
                case NEW_START:
                    date = getInstance().getStartDate();
                    hasTime = getInstance().hasStartTime();
                    break;
                case NEW_PLAN:
                    date = getInstance().getPlanDate();
                    hasTime = getInstance().hasPlanTime();
                    break;
                case NEW_DUE:
                    date = getInstance().getDueDate();
                    hasTime = getInstance().hasDueTime();
                    break;
                default:
            }

            if (date == null)
                cal = GregorianCalendar.getInstance();
            else
                cal.setTime(date);

            if (!hasTime) {
                //Hard coded to Carrie's preference.
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
            }

            TimePickerDialog dlg = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            if (confirm) {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);
                                switch (col) {
                                    case NEW_START:
                                        getInstance().setStartDate(cal.getTime());
                                        getInstance().setHasStartTime(true);
                                        break;
                                    case NEW_PLAN:
                                        getInstance().setPlanDate(cal.getTime());
                                        getInstance().setHasPlanTime(true);
                                        break;
                                    case NEW_DUE:
                                        getInstance().setDueDate(cal.getTime());
                                        getInstance().setHasDueTime(true);
                                        break;
                                    default:
                                }
                            }
                        }
                    },
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
            );

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Set", (DialogInterface.OnClickListener) null);
            }
            dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    confirm = false;
                }
            });
            dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "None", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirm = false;
                    switch (col) {
                        case NEW_START:
                            getInstance().setHasStartTime(false);
                            break;
                        case NEW_PLAN:
                            getInstance().setHasPlanTime(false);
                            break;
                        case NEW_DUE:
                            getInstance().setHasDueTime(false);
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