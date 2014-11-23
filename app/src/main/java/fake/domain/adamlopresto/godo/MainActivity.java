package fake.domain.adamlopresto.godo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Set;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class MainActivity extends ActionBarActivity {

    MainListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new MainListFragment();

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public void checkBoxClick(View v) {
        fragment.checkBoxClick(v);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        fragment.handleIntent(intent);
    }

    public void startActivityWithTransitions(Intent intent){
        //Bundle options =  ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
        ActivityCompat.startActivity(this, intent, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        fragment.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fragment.drawerToggle.onConfigurationChanged(newConfig);
    }

    public static class MainListFragment extends ListFragment  implements LoaderManager.LoaderCallbacks<Cursor> {

        private TaskAdapter adapter;
        private boolean paused = false;
        public ActionBarDrawerToggle drawerToggle;

        @Nullable
        private String query;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_main, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            getListView().setMultiChoiceModeListener(mActionModeCallback);

            final Activity activity = getActivity();

            adapter = new TaskAdapter(activity, null, true);
            setListAdapter(adapter);

            setHasOptionsMenu(true);

            handleIntent(activity.getIntent());

            final FloatingActionButton fab = (FloatingActionButton)activity.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), TaskActivity.class));
                }
            });

            fab.attachToListView(getListView());

            ListView drawerList = (ListView) activity.findViewById(R.id.left_drawer);
            drawerList.setAdapter(new ArrayAdapter<String>(activity,
                    android.R.layout.simple_list_item_1, new String[]{
                    "Active", "Plan", "Archive", "Contexts", "Settings"
            }));


            final DrawerLayout drawerLayout = (DrawerLayout)activity.findViewById(R.id.drawer_layout);

            drawerList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            drawerLayout.closeDrawers();
                            switch (position) {
                                case 0: {
                                    //Active
                                    SharedPreferences.Editor editor =
                                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                    .edit();
                                    editor.putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_DONE, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_FUTURE, false)
                                            .commit();
                                    restartLoader();
                                    break;
                                }
                                case 1: {
                                    //Plan
                                    SharedPreferences.Editor editor =
                                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                    .edit();
                                    editor.putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, true)
                                            .putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, true)
                                            .putBoolean(SettingsActivity.PREF_SHOW_DONE, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_FUTURE, true)
                                            .commit();
                                    restartLoader();
                                    break;
                                }
                                case 2: {
                                    //Archive
                                    SharedPreferences.Editor editor =
                                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                    .edit();
                                    editor.putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, false)
                                            .putBoolean(SettingsActivity.PREF_SHOW_DONE, true)
                                            .putBoolean(SettingsActivity.PREF_SHOW_FUTURE, false)
                                            .commit();
                                    restartLoader();
                                    break;
                                }
                                case 3:
                                    startActivity(new Intent(getActivity(), ContextsActivity.class));
                                    break;
                                case 4:
                                    //Settings
                                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                                    break;
                            }
                        }
                    });

            drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout,
                    R.string.drawer_opened, R.string.drawer_closed){

                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    //activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    fab.show();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    //activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    fab.hide();
                }
            };

            drawerLayout.setDrawerListener(drawerToggle);

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
        public void onStart() {
            super.onStart();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    getActivity().getContentResolver().delete(GoDoContentProvider.TASKS_URI, TasksTable.COLUMN_NAME + " IS NULL", null);
                }
            }).start();
        }


        @Override
        public void onResume() {
            super.onResume();
            if (paused) {
                restartLoader();
                paused = false;
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            paused = true;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.main, menu);


            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);


            MenuItem searchItem = menu.findItem(R.id.action_search);
            assert searchItem != null;
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            assert searchView != null;
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
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
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (drawerToggle.onOptionsItemSelected(item))
                return true;

            switch (item.getItemId()) {
                /*
                case R.id.action_contexts:
                    startActivity(new Intent(getActivity(), ContextsActivity.class));
                    return true;
                case R.id.action_settings:
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    return true;
                case R.id.action_new_task:
                    startActivity(new Intent(getActivity(), TaskActivity.class));
                    return true;
                    */
                case R.id.action_new_from_template: {
                    final Cursor cursor = getActivity().getContentResolver().query(GoDoContentProvider.TASKS_URI,
                            new String[]{TasksTable.COLUMN_ID, TasksTable.COLUMN_NAME,
                                    TasksTable.COLUMN_NOTES},
                            TasksTable.COLUMN_REPEAT + "=2", null, TasksTable.COLUMN_NAME
                    );
                    new AlertDialog.Builder(getActivity())
                            .setAdapter(
                                    new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2,
                                            cursor, new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES},
                                            new int[]{android.R.id.text1, android.R.id.text2}, 0),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            assert cursor != null;
                                            cursor.moveToPosition(which);
                                            long id = cursor.getLong(0);
                                            Intent i = new Intent(getActivity(), TaskActivity.class);
                                            i.putExtra(InstanceHolderActivity.EXTRA_TASK, id);
                                            startActivity(i);
                                        }

                                    }
                            )
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();

                    return true;
                }
                /*
                case R.id.action_notify:
                    getActivity().startService(new Intent(getActivity(), NotificationService.class));
                    return true;
                    */
                case R.id.action_search:
                    return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onListItemClick(ListView listView, View v, int position, long id) {
            Intent intent = new Intent(getActivity(), TaskActivity.class);
            intent.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ArrayList<Pair<View, String>> list = new ArrayList<>(5);
                addViewIfFound(list, v, R.id.task_name, "taskName");
                addViewIfFound(list, v, R.id.task_notes, "taskNotes");
                addViewIfFound(list, v, R.id.instance_notes, "instanceNotes");
                addViewIfFound(list, v, R.id.plan_date, "planDate");
                addViewIfFound(list, v, R.id.due_date, "dueDate");

                Pair[] array = new Pair[list.size()];
                list.toArray(array);

                ActivityCompat.startActivity(getActivity(), intent,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                array
                        ).toBundle());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.startActivity(getActivity(), intent,
                        ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
            } else {
                startActivity(intent);
            }
        }

        private void addViewIfFound(ArrayList<Pair<View, String>> list, View parent, @IdRes int id, String transitionName){
            View view = parent.findViewById(id);
            if (view != null && view.getVisibility() == View.VISIBLE){
                list.add(new Pair<>(view, transitionName));
            }
        }

        @SuppressWarnings ("NonBooleanMethodNameMayNotStartWithQuestion")
        public void checkBoxClick(View v) {
            ListView lv = getListView();
            Checkable cb = (Checkable) v;
            Instance inst = Instance.get(DatabaseHelper.getInstance(getActivity()), lv.getItemIdAtPosition(lv.getPositionForView(v)));
            inst.updateDone(cb.isChecked());
            inst.flush();
            getActivity().getContentResolver().notifyChange(GoDoContentProvider.INSTANCES_URI, null);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, false))
                    where = "NOT blocked_by_context";

                if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, false))
                    where = DatabaseUtils.concatenateWhere(where, "NOT blocked_by_task");
                if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_FUTURE, false))
                    where = DatabaseUtils.concatenateWhere(where, "coalesce(start_date,0) < DATETIME('now', 'localtime')");

                String overdue = "(length(due_date) > 10 and due_date <= DATETIME('now', 'localtime'))";
                where = (where == null) ? "" : "(" + where + ") or "+overdue;

                if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_DONE, false))
                    where = DatabaseUtils.concatenateWhere(where, "done_date is null or done_date > DATETIME('now', '-1 hours', 'localtime')");

                where = DatabaseUtils.concatenateWhere(where, "task_name is not null");
                Log.e("GoDo", "where clause in full: " + where);
                sort = TaskAdapter.SORT;
            }

            return new CursorLoader(getActivity(), uri,
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
                    inflater = new MenuInflater(getActivity());
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
                MainActivity activity = (MainActivity)getActivity();
                switch (item.getItemId()) {
                    case R.id.edit: {
                        final long id = getListView().getCheckedItemIds()[0];
                        mode.finish(); // Action picked, so close the CAB
                        Intent i = new Intent(getActivity(), TaskActivity.class);
                        i.putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id);
                        activity.startActivityWithTransitions(i);
                        return true;
                    }
                    case R.id.create_prereq:
                        startActivity(new Intent(getActivity(), TaskActivity.class)
                                .putExtra("next", getListView().getCheckedItemIds()));
                        mode.finish();
                        return true;

                    case R.id.create_next_step:
                        startActivity(new Intent(getActivity(), TaskActivity.class)
                                .putExtra("prereq", getListView().getCheckedItemIds()));
                        mode.finish();
                        return true;

                    case R.id.delete: {
                        final long[] ids = getListView().getCheckedItemIds();
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Delete these tasks?")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ContentResolver res = getActivity().getContentResolver();
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
    }
}
