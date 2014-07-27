package fake.domain.adamlopresto.godo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public class TaskDetailsFragment extends Fragment implements DateTimePicker.OnDateChangeListener {

    private CheckBox done;
    private EditText taskName;
    private EditText taskNotes;
    private TextView repetitionSummary;
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

    private boolean showRepetitionCollapsed = true;
    private View repetitionHeader;
    private View repetitionDivider;
    private View viewHistoryButton;

    private TextView contexts;

    private View.OnClickListener showRepetitionsActivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getActivity(), RepetitionRulesListActivity.class);
            i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
            startActivity(i);
        }
    };
    private View.OnClickListener expandContractRepetitionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showRepetitionCollapsed = !showRepetitionCollapsed;
            if (showRepetitionCollapsed) {
                repetitionHeader.setVisibility(View.GONE);
                repetitionRuleList.setVisibility(View.GONE);
                instanceNotes.setVisibility(View.GONE);
                hideUnless(instanceNotesRo, !TextUtils.isEmpty(instanceNotesRo.getText()));
                repetitionDivider.setVisibility(View.GONE);
                viewHistoryButton.setVisibility(View.GONE);
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);
            } else {
                repetitionHeader.setVisibility(View.VISIBLE);
                repetitionRuleList.setVisibility(View.VISIBLE);
                instanceNotes.setVisibility(View.VISIBLE);
                instanceNotesRo.setVisibility(View.GONE);
                repetitionDivider.setVisibility(View.VISIBLE);
                viewHistoryButton.setVisibility(View.VISIBLE);
                repetitionSummary.setOnClickListener(showRepetitionsActivityListener);
            }
        }
    };

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

        //v.findViewById(R.id.repetition_card).setOnClickListener(expandContractRepetitionsListener);

        repetitionHeader   =           v.findViewById(R.id.repetition_header);
        repetitionHeader.setOnClickListener(expandContractRepetitionsListener);
        repetitionSummary  = (TextView)v.findViewById(R.id.repetition_summary);
        repetitionRuleList = (TextView)v.findViewById(R.id.repetition_list);
        repetitionDivider  =           v.findViewById(R.id.repetition_divider);


        repetitionRuleList.setOnClickListener(showRepetitionsActivityListener);

        instanceNotesRo = (TextView)v.findViewById(R.id.instance_notes_ro);
        viewHistoryButton = v.findViewById(R.id.view_history_button);
        View.OnClickListener showHistoryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), HistoryActivity.class);
                i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
                startActivity(i);
            }
        };
        viewHistoryButton.setOnClickListener(showHistoryListener);
        repetitionDivider.setOnClickListener(showHistoryListener);

        contexts = (TextView) v.findViewById(R.id.contexts);
        contexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TaskActivity)getActivity()).showContextsDialog();
            }
        });

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
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);
                break;
            case TEMPLATE:
                repetitionSummary.setText("Repeats manually");
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);
                templateRW = true;
                break;
            case NONE:
                repetitionSummary.setText("Does not repeat");
                repetitionSummary.setOnClickListener(showRepetitionsActivityListener);
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
                Cursor cursor = getActivity().getContentResolver().query(GoDoContentProvider.CONTEXTS_URI,
                        new String[]{ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_ACTIVE},
                        ContextsTable.COLUMN_ID + "=?", Utils.idToSelectionArgs(params[0]),null);

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
                repetitionRuleList.setVisibility(s == null || showRepetitionCollapsed
                                                 ? View.GONE
                                                 : View.VISIBLE);
                repetitionRuleList.setText(s);
            }
        }.execute(task.getId());

        loadContexts();
    }

    public void loadContexts(){
        new AsyncTask<Void, Void, CharSequence>(){

            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected CharSequence doInBackground(Void... ignored) {
                long task_id = getTask().getId();
                SQLiteDatabase db = DatabaseHelper.getInstance(getActivity()).getReadableDatabase();
                Cursor cursor = db.query(ContextsTable.TABLE, new String[]{ContextsTable.COLUMN_NAME,
                                ContextsTable.COLUMN_ACTIVE},
                        "exists (select * from " + TaskContextTable.TABLE + " where "
                                + TaskContextTable.COLUMN_TASK + "=" + task_id
                                + " and context=contexts._id)",
                        null, null, null, ContextsTable.COLUMN_NAME);
                cursor.moveToFirst();
                if (cursor.isAfterLast())
                    return "No contexts";
                SpannableStringBuilder b = new SpannableStringBuilder();
                int start = 0;
                while (!cursor.isAfterLast()){
                    b.append(cursor.getString(0));
                    boolean active = cursor.getInt(1) != 0;
                    cursor.moveToNext();
                    if (!cursor.isAfterLast())
                        b.append(", ");
                    b.setSpan(new ForegroundColorSpan(active ? Color.BLACK : Color.GRAY),
                            start, b.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = b.length();
                }
                return b;
            }

            @Override
            protected void onPostExecute(CharSequence s) {
                contexts.setText(s);
            }
        }.execute();
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