package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;

public class AllDependenciesFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    @Nullable
    private final AbsListView.MultiChoiceModeListener mActionModeCallback = new AbsListView.MultiChoiceModeListener() {
        @NotNull
        private MenuItem editItem;

        @Override
        public void onItemCheckedStateChanged(@NotNull ActionMode mode, int position,
                                              long id, boolean checked) {
            final int checkedCount = getListView().getCheckedItemCount();
            mode.setSubtitle("" + checkedCount + " items selected");
            editItem.setVisible(checkedCount == 1);
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(@NotNull ActionMode mode, @NotNull Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            if (inflater == null)
                inflater = new MenuInflater(getActivity());
            inflater.inflate(R.menu.context_edit_delete, menu);
            editItem = menu.findItem(R.id.edit);
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
        public boolean onActionItemClicked(@NotNull ActionMode mode, @NotNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete: {
                    SparseBooleanArray positions = getListView().getCheckedItemPositions();
                    ContentResolver res = getActivity().getContentResolver();
                    Cursor c = adapter.getCursor();
                    String where = InstanceDependencyTable.COLUMN_FIRST + "=? AND "
                                   + InstanceDependencyTable.COLUMN_SECOND + "=?";
                    String[] idArray = new String[2];
                    String id = String.valueOf(getInstanceId());

                    for (int i = 0 ; i < positions.size() ; i++){
                        if (positions.valueAt(i)){
                            int pos = positions.keyAt(i) - 1; // shift down to accommodate header
                            c.moveToPosition(pos);
                            Log.e("GoDo", "Moving cursor to position "+pos +" for i="+i);
                            switch (c.getInt(c.getColumnIndexOrThrow("item_type"))){
                                case 0:
                                    idArray[0] = String.valueOf(c.getLong(0));
                                    idArray[1] = id;
                                    break;
                                case 2:
                                    idArray[0] = id;
                                    idArray[1] = String.valueOf(c.getLong(0));
                            }
                            int numDeleted = res.delete(GoDoContentProvider.DEPENDENCY_URI, where, idArray);
                            Log.e("GoDo", "Deleted " + numDeleted + " items, first=" + idArray[0] + " second=" + idArray[1]);
                        }
                    }

                    getLoaderManager().restartLoader(0, null, AllDependenciesFragment.this);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                }
                case R.id.edit:
                    startActivity(new Intent(getActivity(), TaskActivity.class).putExtra(InstanceHolderActivity.EXTRA_INSTANCE,
                            getListView().getCheckedItemIds()[0]));
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    private DependencyAdapter adapter;
    private TextView header;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new DependencyAdapter(getActivity(),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newDependency(true);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickDependencyFromList(true);
                    }
                });
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = getListView();
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(mActionModeCallback);
        Context context = getActivity();
        LayoutInflater inflater = LayoutInflater.from(context);
        View headerFull = inflater.inflate(R.layout.dependency_header, lv, false);
        header = (TextView)headerFull.findViewById(R.id.text);
        lv.addHeaderView(headerFull, null, false);
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.dependency_footer, lv, false);
        footer.findViewById(R.id.create_next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDependency(false);
            }
        });
        footer.findViewById(R.id.pick_next_step_from_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDependencyFromList(false);
            }
        });
        lv.addFooterView(footer, null, false);
        setListAdapter(adapter);
        setEmptyText("None");
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * Creates a new instance of a new task, with a given relationship to this one
     * @param prereq If true, create a prereq. Else, create a next step.
     */
    private void newDependency(boolean prereq){
        String otherSide = prereq ? "next" : "prereq";
        startActivity(new Intent(getActivity(), TaskActivity.class)
                .putExtra(otherSide, new long[]{forceInstanceId()}));
    }

    /**
     * Shows a dialog with a list of existing (incomplete) instances. User chooses one, which is
     * associated with the current instance.
     * @param prereq If true, pick a prereq. Else, pick a next step.
     */
    private void pickDependencyFromList(final boolean prereq) {
        final Cursor c = getActivity().getContentResolver()
                .query(GoDoContentProvider.INSTANCES_URI, TaskAdapter.PROJECTION, "done_date is null", null, "task_name ASC");
        new AlertDialog.Builder(getActivity())
                .setAdapter(new TaskAdapter(getActivity(), c, false),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                assert c != null;
                                c.moveToPosition(which);
                                ContentValues cv = new ContentValues(2);
                                if (prereq) {
                                    cv.put(InstanceDependencyTable.COLUMN_FIRST, c.getLong(0));
                                    cv.put(InstanceDependencyTable.COLUMN_SECOND, forceInstanceId());
                                } else {
                                    cv.put(InstanceDependencyTable.COLUMN_FIRST, forceInstanceId());
                                    cv.put(InstanceDependencyTable.COLUMN_SECOND, c.getLong(0));
                                }
                                getActivity().getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
                                getLoaderManager().restartLoader(0, null, AllDependenciesFragment.this);
                            }
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private long forceInstanceId() {
        return ((InstanceHolderActivity) getActivity()).instance.forceId();
    }

    private long getInstanceId() {
        return ((InstanceHolderActivity) getActivity()).instance.getId();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        getListView().setItemChecked(position, true);
        //getActivity().startActionMode(mActionModeCallback);
    }

    @NotNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                Uri.withAppendedPath(GoDoContentProvider.DEPENDANT_INSTANCES_URI, String.valueOf(getInstanceId())),
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        header.setText(adapter.getItemViewType(0) == 1
                       ? "No prerequisites"
                       : "Prerequisites");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
