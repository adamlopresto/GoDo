package fake.domain.adamlopresto.godo;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public class TaskDetailsFragment extends Fragment implements DateTimePicker.OnDateChangeListener {

    private CheckBox done;
    private TextView taskName;
    private TextView taskNotes;
    private TextView repetitionSummary;
    private TextView repetitionRuleList;
    private TextView instanceNotes;
    private TextView instanceNotesRo;
    private DateTimePicker start;
    private DateTimePicker plan;
    private DateTimePicker due;
    private Spinner notification;
    private View dueNotificationLabel;
    private Spinner dueNotification;
    private View startAfterPlan;
    private View startAfterDue;
    private View planAfterDue;

    private boolean showRepetitionCollapsed = true;
    private View repetitionHeader;
    private View repetitionDivider;
    private View viewHistoryButton;

    private TextView contexts;
    private TextView relationships;

    private final View.OnClickListener showRepetitionsActivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getActivity(), RepetitionRulesListActivity.class);
            i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
            startActivity(i);
        }
    };
    private final View.OnClickListener expandContractRepetitionsListener = new View.OnClickListener() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_details, group, false);
        assert v != null;
        done = (CheckBox) v.findViewById(R.id.check);
        taskName = (TextView) v.findViewById(R.id.task_name);
        taskNotes = (TextView) v.findViewById(R.id.task_notes);
        instanceNotes = (TextView) v.findViewById(R.id.instance_notes);
        notification = (Spinner) v.findViewById(R.id.notification);
        dueNotification = (Spinner) v.findViewById(R.id.due_notification);
        dueNotificationLabel = v.findViewById(R.id.due_label);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            ((ViewGroup)v.findViewById(R.id.layout)).getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGING);

        repetitionHeader   =           v.findViewById(R.id.repetition_header);
        repetitionHeader.setOnClickListener(expandContractRepetitionsListener);
        repetitionSummary  = (TextView)v.findViewById(R.id.repetition_summary);
        repetitionRuleList = (TextView)v.findViewById(R.id.repetition_list);
        repetitionDivider  =           v.findViewById(R.id.repetition_divider);


        repetitionRuleList.setOnClickListener(showRepetitionsActivityListener);

        instanceNotesRo = (TextView)v.findViewById(R.id.instance_notes_ro);
        instanceNotesRo.setOnClickListener(expandContractRepetitionsListener);


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

        v.findViewById(R.id.relationships_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent i = new Intent(getActivity(), DependenciesActivity.class);
                i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
                startActivity(i);
                */
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AllDependenciesFragment())
                        .addToBackStack(null).commit();
            }
        });

        relationships = (TextView)v.findViewById(R.id.relationships_label);

        contexts = (TextView) v.findViewById(R.id.contexts);
        contexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TaskActivity) getActivity()).showContextsDialog();
            }
        });

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.header);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar =  activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_activity_task);

        return v;
    }

    @Override
    public void onResume() {
        fillData();
        super.onResume();
    }

    @NonNull
    private Task getTask() {
        return ((InstanceHolderActivity) getActivity()).task;
    }

    @NonNull
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
                Cursor cursor = getActivity().getContentResolver().query(GoDoContentProvider.REPETITION_RULES_URI,
                        new String[]{RepetitionRulesTable.COLUMN_ID, RepetitionRulesTable.COLUMN_TASK,
                                RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE,
                                RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO},
                        RepetitionRulesTable.COLUMN_TASK + "=?", Utils.idToSelectionArgs(params[0]),null);

                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return null;
                }

                while (!cursor.isAfterLast()){
                    b.append(Utils.repetitionRuleTextFromCursor(cursor, template));
                    cursor.moveToNext();
                    if (!cursor.isAfterLast())
                        b.append('\n');
                }

                cursor.close();
                return b.toString();
            }

            @Override
            protected void onPostExecute(String s) {
                repetitionRuleList.setVisibility(s == null || showRepetitionCollapsed
                                                 ? View.GONE
                                                 : View.VISIBLE);
                repetitionRuleList.setText(s);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, task.getId());

        loadContexts();
    }

    public void loadContexts(){
        new AsyncTask<Void, Void, CharSequence>(){
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
                if (cursor.isAfterLast()) {
                    cursor.close();
                    return "No contexts";
                }
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
                cursor.close();
                return b;
            }

            @Override
            protected void onPostExecute(CharSequence s) {
                contexts.setText(s);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void extractInstanceDetails() {
        final Instance instance = getInstance();
        CharSequence notes = instance.getNotes();
        instanceNotes.setText(notes);
        instanceNotesRo.setText(notes);
        hideUnless(instanceNotesRo, showRepetitionCollapsed && !TextUtils.isEmpty(notes));
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

        new AsyncTask<Void, Void, CharSequence>(){
            @Override
            protected CharSequence doInBackground(Void... ignored) {
                Cursor cursor = getActivity().getContentResolver().query(
                        Uri.withAppendedPath(GoDoContentProvider.DEPENDANT_INSTANCES_URI,
                                String.valueOf(instance.forceId())),
                        null, null, null, null);

                if (cursor.getCount() == 1) {
                    cursor.close();
                    return "No dependencies";
                }

                cursor.moveToFirst();
                SpannableStringBuilder builder = new SpannableStringBuilder();
                int itemColumn = cursor.getColumnIndexOrThrow("item_type");
                if (cursor.getInt(itemColumn) == 0) {
                    //At least one prereq.
                    builder.append("Requires ");
                    int start = builder.length();
                    builder.append(cursor.getString(cursor.getColumnIndexOrThrow(InstancesView.COLUMN_TASK_NAME)));
                    if (!cursor.isNull(cursor.getColumnIndexOrThrow(InstancesView.COLUMN_DONE_DATE)))
                        builder.setSpan(new StrikethroughSpan(),
                                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    do {
                        cursor.moveToNext();
                    } while (cursor.getInt(itemColumn) != 1);

                    if (cursor.getPosition() != 1) {
                        //More than one prereq
                        builder.clear();
                        builder.append("Requires ");
                        builder.append(String.valueOf(cursor.getPosition()));
                        builder.append(" tasks");
                    }
                }

                //Now at divider. Do it all again to find out about next steps.

                if (!cursor.moveToNext()) {
                    //At end, so no next steps.
                    cursor.close();
                    return builder;
                }

                if (builder.length() != 0)
                    builder.append('\n');
                builder.append("Blocks ");

                int numberOfNextSteps = cursor.getCount() - cursor.getPosition();
                if (numberOfNextSteps == 1) {
                    int start = builder.length();
                    builder.append(cursor.getString(cursor.getColumnIndexOrThrow(InstancesView.COLUMN_TASK_NAME)));
                    if (!cursor.isNull(cursor.getColumnIndexOrThrow(InstancesView.COLUMN_DONE_DATE)))
                        builder.setSpan(new StrikethroughSpan(),
                                start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    builder.append(String.valueOf(numberOfNextSteps));
                    builder.append(" tasks");

                }
                cursor.close();
                return builder;
            }

            @Override
            protected void onPostExecute(CharSequence s) {
                relationships.setText(s);
            }
        }.execute();
    }

    @Override
    public void onPause() {
        saveData();
        super.onPause();
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
    private String nullString(@Nullable TextView in) {
        if (in == null)
            return null;
        CharSequence edit = in.getText();
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
                boolean hasDueDate = newDate != null && !Utils.SOMEDAY.equals(newDate);
                hideUnless(dueNotification, hasDueDate);
                hideUnless(dueNotificationLabel, hasDueDate);
                break;
            default:
                //no-op
        }
    }
}