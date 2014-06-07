package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public class TaskDetailsFragment extends Fragment implements DateTimePicker.OnDateChangeListener {

    private CheckBox done;
    private EditText taskName;
    private EditText taskNotes;
    private EditText instanceNotes;
    private DateTimePicker start;
    private DateTimePicker plan;
    private DateTimePicker due;
    private Spinner notification;
    private Spinner dueNotification;
    private View startAfterPlan;
    private View startAfterDue;
    private View planAfterDue;

    /**
     * Return true iff the first date is strictly after the second date
     * If either is null, return false
     *
     * @param first  first date
     * @param second second date
     * @return true only if both dates are defined and the second is after the first
     */
    private static boolean isAfter(@Nullable Date first, @Nullable Date second) {
        return first != null && second != null && first.after(second);
    }

    private static void hideUnless(View view, boolean b) {
        view.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_details, group, false);
        assert v != null;
        done = (CheckBox) v.findViewById(R.id.check);
        taskName = (EditText) v.findViewById(R.id.task_name);
        taskNotes = (EditText) v.findViewById(R.id.task_notes);
        instanceNotes = (EditText) v.findViewById(R.id.instance_notes);
        notification = (Spinner) v.findViewById(R.id.notification);
        dueNotification = (Spinner) v.findViewById(R.id.due_notification);

        start = (DateTimePicker) v.findViewById(R.id.start);
        start.setColumn(RepetitionRuleColumns.NEW_START);
        start.setOnDateDateChangeListener(this);

        plan = (DateTimePicker) v.findViewById(R.id.plan);
        plan.setColumn(RepetitionRuleColumns.NEW_PLAN);
        plan.setOnDateDateChangeListener(this);

        due = (DateTimePicker) v.findViewById(R.id.due);
        due.setColumn(RepetitionRuleColumns.NEW_DUE);
        due.setOnDateDateChangeListener(this);

        startAfterPlan = v.findViewById(R.id.startAfterPlan);
        startAfterDue = v.findViewById(R.id.startAfterDue);
        planAfterDue = v.findViewById(R.id.planAfterDue);

        fillData();

        return v;
    }

    @NotNull
    private Task getTask() {
        return ((TaskActivity) getActivity()).task;
    }

    @NotNull
    private Instance getInstance() {
        return ((TaskActivity) getActivity()).instance;
    }

    private void fillData() {
        extractTaskDetails();
        extractInstanceDetails();
    }

    private void extractTaskDetails() {
        Task task = getTask();
        taskName.setText(task.getName());
        taskNotes.setText(task.getNotes());
        notification.setSelection(task.getNotification().ordinal());
        dueNotification.setSelection(task.getDueNotification().ordinal());
    }

    private void extractInstanceDetails() {
        Instance instance = getInstance();
        instanceNotes.setText(instance.getNotes());
        done.setChecked(instance.getDoneDate() != null);

        Date startDate = instance.getStartDate();
        start.setDate(startDate, instance.hasStartTime());

        Date planDate = instance.getPlanDate();
        plan.setDate(planDate, instance.hasPlanTime());

        Date dueDate = instance.getDueDate();
        due.setDate(dueDate, instance.hasDueTime());

        hideUnless(startAfterPlan, isAfter(startDate, planDate));
        hideUnless(startAfterDue, isAfter(startDate, dueDate));
        hideUnless(planAfterDue, isAfter(planDate, dueDate));
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
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

    @Override
    public void onDateChanged(Date newDate, boolean hasTime, RepetitionRuleColumns column) {
        Instance instance = getInstance();
        switch (column) {
            case NEW_START:
                instance.setStartDate(newDate);
                instance.setHasStartTime(hasTime);
                hideUnless(startAfterPlan, isAfter(newDate, instance.getPlanDate()));
                hideUnless(startAfterDue, isAfter(newDate, instance.getDueDate()));
                break;
            case NEW_PLAN:
                instance.setPlanDate(newDate);
                instance.setHasPlanTime(hasTime);
                hideUnless(startAfterPlan, isAfter(instance.getStartDate(), newDate));
                hideUnless(planAfterDue, isAfter(newDate, instance.getDueDate()));
                break;
            case NEW_DUE:
                instance.setDueDate(newDate);
                instance.setHasDueTime(hasTime);
                hideUnless(startAfterDue, isAfter(instance.getStartDate(), newDate));
                hideUnless(planAfterDue, isAfter(instance.getPlanDate(), newDate));
                break;
            default:
                //no-op
        }
    }
}