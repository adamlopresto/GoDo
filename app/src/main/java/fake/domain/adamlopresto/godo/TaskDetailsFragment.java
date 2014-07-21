package fake.domain.adamlopresto.godo;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public class TaskDetailsFragment extends Fragment implements DateTimePicker.OnDateChangeListener {

    private CheckBox done;
    private EditText taskName;
    private EditText taskNotes;
    private TextView repetitionSummary;
    private TextView repetitionSummary2;
    private TextView repetitionRuleList;
    private EditText instanceNotes;
    private TextView instanceNotesRo;
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

        v.findViewById(R.id.repetition_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewAnimator) v).showNext();
            }
        });

        repetitionSummary = (TextView)v.findViewById(R.id.repetition_summary);
        repetitionSummary2 = (TextView)v.findViewById(R.id.repetition_summary2);
        repetitionRuleList = (TextView)v.findViewById(R.id.repetition_list);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), RepetitionRulesListActivity.class);
                i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
                startActivity(i);
            }
        };

        repetitionSummary2.setOnClickListener(listener);
        repetitionRuleList.setOnClickListener(listener);

        instanceNotesRo = (TextView)v.findViewById(R.id.instance_notes_ro);

        fillData();

        return v;
    }

    @NotNull
    private Task getTask() {
        return ((InstanceHolderActivity) getActivity()).task;
    }

    @NotNull
    private Instance getInstance() {
        return ((InstanceHolderActivity) getActivity()).instance;
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

        boolean templateRW = false;
        switch (task.getRepeat()) {
            case AUTOMATIC:
                repetitionSummary.setText("Repeats automatically");
                repetitionSummary2.setText("Repeats automatically");
                break;
            case TEMPLATE:
                repetitionSummary.setText("Repeats manually");
                repetitionSummary2.setText("Repeats manually");
                templateRW = true;
                break;
            case NONE:
                repetitionSummary.setText("Does not repeat");
                repetitionSummary2.setText("Does not repeat");
        }

        final boolean template = templateRW;

        new AsyncTask<Long, Void, String>(){

            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected String doInBackground(Long... params) {
                StringBuilder b = new StringBuilder();
                Cursor cursor = getActivity().getContentResolver().query(GoDoContentProvider.REPETITION_RULES_URI,
                        new String[]{RepetitionRulesTable.COLUMN_ID, RepetitionRulesTable.COLUMN_TASK,
                                RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE,
                                RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO},
                        RepetitionRulesTable.COLUMN_TASK + "=?", Utils.idToSelectionArgs(params[0]),null);

                if (!cursor.moveToFirst()) {
                    return null;
                }

                while (!cursor.isAfterLast()){
                    b.append(Utils.repetitionRuleTextFromCursor(cursor, template));
                    cursor.moveToNext();
                    if (!cursor.isAfterLast())
                        b.append('\n');
                }

                return b.toString();
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null){
                    repetitionRuleList.setVisibility(View.GONE);
                } else {
                    repetitionRuleList.setVisibility(View.VISIBLE);
                    repetitionRuleList.setText(s);
                }
            }
        }.execute(task.getId());

    }

    private void extractInstanceDetails() {
        Instance instance = getInstance();
        CharSequence notes = instance.getNotes();
        instanceNotes.setText(notes);
        instanceNotesRo.setText(notes);
        hideUnless(instanceNotesRo, !TextUtils.isEmpty(notes));
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