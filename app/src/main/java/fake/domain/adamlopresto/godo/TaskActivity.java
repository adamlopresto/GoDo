package fake.domain.adamlopresto.godo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

public class TaskActivity extends InstanceHolderActivity implements
        ActionBar.TabListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    TaskDetailsFragment taskDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        if (!extractTaskAndOrInstanceFromBundle(savedInstanceState)) {
            if (!extractTaskAndOrInstanceFromBundle(getIntent().getExtras())) {
                instance = new Instance(DatabaseHelper.getInstance(this), this);
                task = instance.getTask();
            }
        }

        long[] tmp = getIntent().getLongArrayExtra("prereq");
        if (tmp != null) {
            Log.e("GoDo", "Create prerequisites");
            ContentValues cv = new ContentValues(2);
            cv.put(InstanceDependencyTable.COLUMN_SECOND, instance.forceId());
            for (long id : tmp) {
                Log.e("GoDo", "Create " + id);
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
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (actionBar == null) {
            return;
        }
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task, menu);
        return true;
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_contexts:
                showContextsDialog();
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
                .setTitle(R.string.title_activity_contexts)
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

    @Override
    public void onTabSelected(@NotNull ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0: {
                    return taskDetailsFragment = new TaskDetailsFragment();
                }
                case 1: {
                    Fragment f = new DependencyFragment();
                    Bundle b = new Bundle();
                    b.putBoolean("prereq", true);
                    f.setArguments(b);
                    return f;
                }
                case 2: {
                    return new DependencyFragment();
                }
                default:
                    throw new IllegalArgumentException("Expected fragment in range 0-4, got "+position);
            }
        }

        @Override
        public int getCount() {
            // total number of pages
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_task_details).toUpperCase(l);
                case 1:
                    return "Prerequisites";
                case 2:
                    return "Next steps";
            }
            return null;
        }
    }
}
