package fake.domain.adamlopresto.godo;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @Nullable
    private final AbsListView.MultiChoiceModeListener mActionModeCallback = new AbsListView.MultiChoiceModeListener() {
        @NonNull
        private MenuItem editItem;

        @Override
        public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position,
                                              long id, boolean checked) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mode.setSubtitle("One item selected");
                    editItem.setVisible(true);
                    break;
                default:
                    editItem.setVisible(false);
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(@NonNull ActionMode mode, @NonNull Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            if (inflater == null)
                inflater = new MenuInflater(MainActivity.this);
            inflater.inflate(R.menu.main_cab, menu);
            editItem = menu.findItem(R.id.edit);
            mode.setTitle("Tasks");
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.edit: {
                    final long id = getListView().getCheckedItemIds()[0];
                    mode.finish(); // Action picked, so close the CAB
                    Intent i = new Intent(MainActivity.this, TaskActivity.class);
                    i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id);
                    startActivity(i);
                    return true;
                }
                case R.id.create_prereq:
                    startActivity(new Intent(MainActivity.this, TaskActivity.class)
                            .putExtra("next", getListView().getCheckedItemIds()));
                    mode.finish();
                    return true;

                case R.id.create_next_step:
                    startActivity(new Intent(MainActivity.this, TaskActivity.class)
                            .putExtra("prereq", getListView().getCheckedItemIds()));
                    mode.finish();
                    return true;

                case R.id.delete: {
                    final long[] ids = getListView().getCheckedItemIds();
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Delete these tasks?")
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContentResolver res = getContentResolver();
                                    String where = TasksTable.COLUMN_ID + "=?";
                                    String[] idArray = new String[1];

                                    for (long id : ids) {
                                        idArray[0] = String.valueOf(id);
                                        res.delete(GoDoContentProvider.INSTANCES_URI, where, idArray);
                                    }
                                    restartLoader();
                                }

                            }).show();

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                }
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };
    private TaskAdapter adapter;
    private boolean paused = false;

    @Nullable
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(mActionModeCallback);

        adapter = new TaskAdapter(this, null, true);
        setListAdapter(adapter);

        handleIntent(getIntent());

        /*
        Resources res = getResources();
        FloatingActionButton fab = new FloatingActionButton.Builder(this)
                .withDrawable( res.getDrawable(R.drawable.content_new))
                .withButtonColor(res.getColor(R.color.accent))
                .withGravity(Gravity.BOTTOM | Gravity.END)
                .withMargins(0, 0, 16, 16)
                .create();
                */

        restartLoader();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            restartLoader();
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {

            @Override
            public void run() {
                getContentResolver().delete(GoDoContentProvider.TASKS_URI, TasksTable.COLUMN_NAME + " IS NULL", null);
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, NotificationService.class).putExtra("max_notify", 0));
        if (paused) {
            restartLoader();
            paused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);


        MenuItem searchItem = menu.findItem(R.id.action_search);
        assert searchItem != null;
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query = newText;
                restartLoader();
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                query = null;
                restartLoader();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_contexts:
                startActivity(new Intent(this, ContextsActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_new_task:
                startActivity(new Intent(this, TaskActivity.class));
                return true;
            case R.id.action_new_from_template: {
                final Cursor cursor = getContentResolver().query(GoDoContentProvider.TASKS_URI,
                        new String[]{TasksTable.COLUMN_ID, TasksTable.COLUMN_NAME,
                                TasksTable.COLUMN_NOTES},
                        TasksTable.COLUMN_REPEAT + "=2", null, TasksTable.COLUMN_NAME
                );
                new AlertDialog.Builder(this)
                        .setAdapter(
                                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                                        cursor, new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES},
                                        new int[]{android.R.id.text1, android.R.id.text2}, 0),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        assert cursor != null;
                                        cursor.moveToPosition(which);
                                        long id = cursor.getLong(0);
                                        Intent i = new Intent(MainActivity.this, TaskActivity.class);
                                        i.putExtra(InstanceHolderActivity.EXTRA_TASK, id);
                                        startActivity(i);
                                    }

                                }
                        )
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();

                return true;
            }
            case R.id.action_notify:
                startService(new Intent(this, NotificationService.class));
                return true;
            case R.id.action_search:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
        } else {
            startActivity(intent);
        }
    }

    @SuppressWarnings ("NonBooleanMethodNameMayNotStartWithQuestion")
    public void checkBoxClick(View v) {
        ListView lv = getListView();
        Checkable cb = (Checkable) v;
        Instance inst = Instance.get(DatabaseHelper.getInstance(this), lv.getItemIdAtPosition(lv.getPositionForView(v)));
        inst.updateDone(cb.isChecked());
        inst.flush();
        getContentResolver().notifyChange(GoDoContentProvider.INSTANCES_URI, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Nullable
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri;
        String sort;

        String where = null;
        //noinspection IfStatementWithNegatedCondition
        if (!TextUtils.isEmpty(query)) {
            where = "v1." + InstancesView.COLUMN_TASK_NAME + " LIKE " +
                    DatabaseUtils.sqlEscapeString("%" + query + "%");
            uri = GoDoContentProvider.INSTANCE_SEARCH_URI;
            sort = "v1.task_name LIKE " + DatabaseUtils.sqlEscapeString(query + "%") + " DESC" +
                    ", v1.task_name LIKE " + DatabaseUtils.sqlEscapeString("% " + query + "%") + " DESC" +
                    ", v1.task_name";
        } else {
            uri = GoDoContentProvider.INSTANCES_URI;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, false))
                where = "NOT blocked_by_context";

            if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, false))
                where = DatabaseUtils.concatenateWhere(where, "NOT blocked_by_task");
            if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_FUTURE, false))
                where = DatabaseUtils.concatenateWhere(where, "coalesce(start_date,0) < DATETIME('now', 'localtime')");

            where = (where == null) ? "" : "(" + where + ") or ";
            where += "(length(due_date) > 10 and due_date <= DATETIME('now', 'localtime'))";

            if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_DONE, false))
                where = DatabaseUtils.concatenateWhere(where, "done_date is null or done_date > DATETIME('now', '-1 hours', 'localtime')");

            where = DatabaseUtils.concatenateWhere(where, "task_name is not null");
            sort = TaskAdapter.SORT;
        }

        return new CursorLoader(this, uri,
                TaskAdapter.PROJECTION,
                where, null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
