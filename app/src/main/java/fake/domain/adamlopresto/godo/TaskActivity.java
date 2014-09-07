package fake.domain.adamlopresto.godo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

public class TaskActivity extends InstanceHolderActivity {

    private TaskDetailsFragment taskDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repetition_rules_list);

        if (!extractTaskAndOrInstanceFromBundle(savedInstanceState)) {
            if (!extractTaskAndOrInstanceFromBundle(getIntent().getExtras())) {
                instance = new Instance(DatabaseHelper.getInstance(this), this);
                task = instance.getTask();
            }
        }

        long[] tmp = getIntent().getLongArrayExtra("prereq");
        if (tmp != null) {
            ContentValues cv = new ContentValues(2);
            cv.put(InstanceDependencyTable.COLUMN_SECOND, instance.forceId());
            for (long id : tmp) {
                cv.put(InstanceDependencyTable.COLUMN_FIRST, id);
                getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
            }
        }

        tmp = getIntent().getLongArrayExtra("next");
        if (tmp != null) {
            ContentValues cv = new ContentValues(2);
            cv.put(InstanceDependencyTable.COLUMN_FIRST, instance.forceId());
            for (long id : tmp) {
                cv.put(InstanceDependencyTable.COLUMN_SECOND, id);
                getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, taskDetailsFragment = new TaskDetailsFragment())
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                FragmentManager manager = getSupportFragmentManager();
                if (manager.getBackStackEntryCount() > 0)
                    manager.popBackStack();
                else
                    NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showContextsDialog() {
        final long task_id = task.forceId();
        final SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableDatabase();
        Cursor cursor = db.query(ContextsTable.TABLE, new String[]{ContextsTable.COLUMN_ID, ContextsTable.COLUMN_NAME,
                "exists (select * from " + TaskContextTable.TABLE + " where " + TaskContextTable.COLUMN_TASK + "=" + task_id + " and context=contexts._id) AS selected"}, null, null, null, null, null);
        final List<Long> orig = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            orig.add(cursor.getLong(0));
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        final Collection<Long> toAdd = new HashSet<>();
        final Collection<Long> toDel = new HashSet<>();

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(cursor, "selected", ContextsTable.COLUMN_NAME,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                Long id = orig.get(which);
                                if (isChecked) {
                                    if (!toDel.remove(id))
                                        toAdd.add(id);
                                } else {
                                    if (!toAdd.remove(id))
                                        toDel.add(id);
                                }
                            }
                        }
                )
                .setTitle(R.string.title_contexts)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] whereArgs = {String.valueOf(task_id), null};
                        for (Long id : toDel) {
                            whereArgs[1] = String.valueOf(id);
                            db.delete(TaskContextTable.TABLE,
                                    TaskContextTable.COLUMN_TASK + "=? AND " + TaskContextTable.COLUMN_CONTEXT + "=?",
                                    whereArgs);
                        }

                        ContentValues cv = new ContentValues(2);
                        cv.put(TaskContextTable.COLUMN_TASK, task_id);
                        for (Long id : toAdd) {
                            cv.put(TaskContextTable.COLUMN_CONTEXT, id);
                            db.insert(TaskContextTable.TABLE, null, cv);
                        }

                        if (taskDetailsFragment != null)
                            taskDetailsFragment.loadContexts();
                    }
                })
                .show();
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(InstanceHolderActivity.EXTRA_TASK, task.forceId());
        outState.putLong(InstanceHolderActivity.EXTRA_INSTANCE, instance.forceId());
    }
}
