package fake.domain.adamlopresto.godo;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Date;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

@SuppressWarnings ("InstanceVariableMayNotBeInitialized")
public class TaskDetailsFragment extends Fragment implements DateTimePicker.OnDateChangeListener {

//    public static final int REQUEST_PICK_SHORTCUT = 10;
//    private static final int REQUEST_FINISH_SHORTCUT = 20;
    private CheckBox done;
    private TextView taskName;
    private TextView taskNotes;
    private TextView repetitionSummary;
    private TextView repetitionRuleList;
    private TextView instanceNotes;
    private DateTimePicker start;
    private DateTimePicker plan;
    private DateTimePicker due;
    private Spinner notification;
    private View dueNotificationLabel;
    private Spinner dueNotification;
    private View startAfterPlan;
    private View startAfterDue;
    private View planAfterDue;

    /*
    private Button button;
    private Intent buttonIntent;
    */

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
                //collapse the repetitions section
                repetitionHeader.setVisibility(View.GONE);
                repetitionRuleList.setVisibility(View.GONE);
                hideUnless(instanceNotes,
                        !TextUtils.isEmpty(instanceNotes.getText())
                                || getTask().getRepeat() != RepeatTypes.NONE
                       );
                repetitionDivider.setVisibility(View.GONE);
                viewHistoryButton.setVisibility(View.GONE);
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);

            } else {
                repetitionHeader.setVisibility(View.VISIBLE);
                repetitionRuleList.setVisibility(View.VISIBLE);
                instanceNotes.setVisibility(View.VISIBLE);
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
        done = v.findViewById(R.id.check);
        taskName = v.findViewById(R.id.task_name);
        taskNotes = v.findViewById(R.id.task_notes);
        instanceNotes = v.findViewById(R.id.instance_notes);
        notification = v.findViewById(R.id.notification);
        dueNotification = v.findViewById(R.id.due_notification);
        dueNotificationLabel = v.findViewById(R.id.due_label);

        start = v.findViewById(R.id.start);
        start.setColumn(RepetitionRuleColumns.NEW_START);
        start.setOnDateChangeListener(this);

        plan = v.findViewById(R.id.plan);
        plan.setColumn(RepetitionRuleColumns.NEW_PLAN);
        plan.setOnDateChangeListener(this);

        due = v.findViewById(R.id.due);
        due.setColumn(RepetitionRuleColumns.NEW_DUE);
        due.setOnDateChangeListener(this);

        startAfterPlan = v.findViewById(R.id.startAfterPlan);
        startAfterDue = v.findViewById(R.id.startAfterDue);
        planAfterDue = v.findViewById(R.id.planAfterDue);

        ((ViewGroup)v.findViewById(R.id.layout)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        repetitionHeader   = v.findViewById(R.id.repetition_header);
        repetitionHeader.setOnClickListener(expandContractRepetitionsListener);
        repetitionSummary  = v.findViewById(R.id.repetition_summary);
        repetitionRuleList = v.findViewById(R.id.repetition_list);
        repetitionDivider  = v.findViewById(R.id.repetition_divider);


        repetitionRuleList.setOnClickListener(showRepetitionsActivityListener);

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
                Intent i = new Intent(getActivity(), DependenciesActivity.class);
                i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, getInstance().getId());
                startActivity(i);
                /*
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AllDependenciesFragment())
                        .addToBackStack(null).commit();
                        */
            }
        });

        relationships = v.findViewById(R.id.relationships_label);

        contexts = v.findViewById(R.id.contexts);
        contexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TaskActivity) getActivity()).showContextsDialog();
            }
        });

        Toolbar toolbar = v.findViewById(R.id.header);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar =  activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_task);
        }

        /*
        button = (Button) v.findViewById(R.id.shortcut);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (buttonIntent != null) {
                            startActivity(buttonIntent);
                        }
                        else {
                            Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                            intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
                            intent.putExtra(Intent.EXTRA_TITLE, "Pick an action");

                            startActivityForResult(intent, REQUEST_PICK_SHORTCUT);
                        }
                    }
                }
        );
        */

        return v;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case REQUEST_PICK_SHORTCUT:
//                    startActivityForResult(data, REQUEST_FINISH_SHORTCUT);
//                    return;
//                case REQUEST_FINISH_SHORTCUT:
//                    button.setText(data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
//
//                    /*
//                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                            new BitmapDrawable(getResources(),
//                                    (Bitmap) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)),
//                    null, null, null);
//
//                    */
//                    Bitmap bmp = null;
//                    Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
//                    if (extra != null && extra instanceof Bitmap)
//                        bmp = (Bitmap) extra;
//                    if (bmp == null) {
//                        extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
//                        if (extra != null && extra instanceof Intent.ShortcutIconResource) {
//                            try {
//                                Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) extra;
//                                final PackageManager packageManager = getContext().getPackageManager();
//                                Resources resources = packageManager.getResourcesForApplication(iconResource.packageName);
//                                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
//                                bmp = BitmapFactory.decodeResource(resources, id);
//                            } catch (Exception e) {
//                                Log.w("GoDo", "Could not load shortcut icon: " + extra);
//                            }
//                        }
//                    }
//
//                    if (bmp != null)
//                        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                                new BitmapDrawable(getResources(), bmp),
//                                null, null, null);
//
//                    buttonIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
//                    try {
//                        String uri = buttonIntent.toUri(Intent.URI_INTENT_SCHEME);
//                        Log.e("GoDo", uri);
//                        buttonIntent = Intent.parseUri(uri,
//                                Intent.URI_INTENT_SCHEME);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//
//                    //TODO
//                    return;
//            }
//        }
//    }

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
        CharSequence name = task.getName();
        taskName.setText(name);
        if (TextUtils.isEmpty(name)){
            //noinspection MagicNumber
            taskName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInputFromWindow(taskName.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                    }
                }
            }, 500L);
        }
        taskNotes.setText(task.getNotes());
        notification.setSelection(task.getNotification().ordinal());
        dueNotification.setSelection(task.getDueNotification().ordinal());

        boolean templateRW = false;
        switch (task.getRepeat()) {
            case AUTOMATIC:
                repetitionSummary.setText(R.string.repeats_automatically);
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);
                break;
            case TEMPLATE:
                repetitionSummary.setText(R.string.repeats_manually);
                repetitionSummary.setOnClickListener(expandContractRepetitionsListener);
                templateRW = true;
                break;
            case NONE:
                repetitionSummary.setText(R.string.no_repetition);
                repetitionSummary.setOnClickListener(showRepetitionsActivityListener);
        }

        final boolean template = templateRW;
        new RepetitionRuleListAsyncTask(template, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, task.getId());

        loadContexts();
    }

    public void loadContexts(){
        new ContextsAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void extractInstanceDetails() {
        final Instance instance = getInstance();
        CharSequence notes = instance.getNotes();
        instanceNotes.setText(notes);
        hideUnless(instanceNotes,
                !showRepetitionCollapsed || !TextUtils.isEmpty(notes)
                || getTask().getRepeat() != RepeatTypes.NONE
        );
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

        new RelationshipsAsyncTask(this).execute();
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
                plan.update();
                due.update();
                break;
            case NEW_PLAN:
                instance.setPlanDate(newDate);
                instance.setHasPlanTime(hasTime);
                hideUnless(startAfterPlan, isAfter(instance.getStartDate(), newDate));
                hideUnless(planAfterDue, isAfter(newDate, instance.getDueDate()));
                start.update();
                due.update();
                break;
            case NEW_DUE:
                instance.setDueDate(newDate);
                instance.setHasDueTime(hasTime);
                hideUnless(startAfterDue, isAfter(instance.getStartDate(), newDate));
                hideUnless(planAfterDue, isAfter(instance.getPlanDate(), newDate));
                boolean hasDueDate = newDate != null && !Utils.SOMEDAY.equals(newDate);
                hideUnless(dueNotification, hasDueDate);
                hideUnless(dueNotificationLabel, hasDueDate);
                start.update();
                plan.update();
                break;
            default:
                //no-op
        }
    }

    @Override
    public DateTimePicker getStart() {
        return start;
    }

    @Override
    public DateTimePicker getPlan() {
        return plan;
    }

    @Override
    public DateTimePicker getDue() {
        return due;
    }

    private static class RepetitionRuleListAsyncTask extends AsyncTask<Long, Void, String> {

        private final boolean template;
        private final WeakReference<TaskDetailsFragment> fragmentWeakReference;

        public RepetitionRuleListAsyncTask(boolean template, TaskDetailsFragment fragment) {
            this.template = template;
            fragmentWeakReference = new WeakReference<>(fragment);
        }

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
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return null;
            }
            Cursor cursor = fragment.getActivity().getContentResolver().query(GoDoContentProvider.REPETITION_RULES_URI,
                    new String[]{RepetitionRulesTable.COLUMN_ID, RepetitionRulesTable.COLUMN_TASK,
                            RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE,
                            RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO},
                    RepetitionRulesTable.COLUMN_TASK + "=?", Utils.idToSelectionArgs(params[0]), null);
            //noinspection UnusedAssignment,AssignmentToNull
            fragment = null;

            if (cursor == null)
                return null;

            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }

            while (!cursor.isAfterLast()) {
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
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return;
            }

            fragment.repetitionRuleList.setVisibility(s == null || fragment.showRepetitionCollapsed
                                             ? View.GONE
                                             : View.VISIBLE);
            fragment.repetitionRuleList.setText(s);
            //noinspection UnusedAssignment,AssignmentToNull
            fragment = null;
            fragmentWeakReference.clear();
        }
    }

    private static class ContextsAsyncTask extends AsyncTask<Void, Void, CharSequence> {
        private final WeakReference<TaskDetailsFragment> fragmentWeakReference;

        private ContextsAsyncTask(TaskDetailsFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }


        @Override
        protected CharSequence doInBackground(Void... ignored) {
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return null;
            }
            long task_id = fragment.getTask().getId();
            SQLiteDatabase db = DatabaseHelper.getInstance(fragment.getActivity()).getReadableDatabase();
            //noinspection UnusedAssignment,AssignmentToNull
            fragment=null;
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
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return;
            }
            fragment.contexts.setText(s);
            fragmentWeakReference.clear();
        }
    }

    private static class RelationshipsAsyncTask extends AsyncTask<Void, Void, CharSequence> {
        private final WeakReference<TaskDetailsFragment> fragmentWeakReference;

        public RelationshipsAsyncTask(TaskDetailsFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected CharSequence doInBackground(Void... ignored) {
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return null;
            }
            Context context = fragment.getActivity();
            Cursor cursor = context.getContentResolver().query(
                    Uri.withAppendedPath(GoDoContentProvider.DEPENDANT_INSTANCES_URI,
                            String.valueOf(fragment.getInstance().forceId())),
                    null, null, null, null);
            //noinspection UnusedAssignment,AssignmentToNull
            fragment = null;

            if (cursor == null)
                return context.getString(R.string.no_dependencies);
            if (cursor.getCount() == 1) {
                cursor.close();
                return context.getString(R.string.no_dependencies);
            }
            //noinspection UnusedAssignment,AssignmentToNull
            context = null;

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
            TaskDetailsFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return;
            }

            fragment.relationships.setText(s);
        }
    }
}