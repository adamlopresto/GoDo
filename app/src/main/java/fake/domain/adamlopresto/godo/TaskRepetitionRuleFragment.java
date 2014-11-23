package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

public class TaskRepetitionRuleFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

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
            if (inflater == null) {
                inflater = new MenuInflater(getActivity());
            }
            inflater.inflate(R.menu.context_edit_delete, menu);
            editItem = menu.findItem(R.id.edit);
            mode.setTitle("Rules");
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
                    Intent i = new Intent(getActivity(), TaskRepetitionRuleActivity.class);
                    i.putExtra("rule", id);
                    i.putExtra("template", template());
                    startActivity(i);
                    return true;
                }
                case R.id.delete: {
                    final long[] ids = getListView().getCheckedItemIds();
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Delete these rules?")
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContentResolver res = getActivity().getContentResolver();
                                    String where = RepetitionRulesTable.COLUMN_ID + "=?";
                                    String[] idArray = new String[1];
                                    //
                                    for (long id : ids) {
                                        idArray[0] = String.valueOf(id);
                                        res.delete(GoDoContentProvider.REPETITION_RULES_URI, where, idArray);
                                    }
                                    getLoaderManager().restartLoader(0, null, TaskRepetitionRuleFragment.this);
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

    private SimpleCursorAdapter adapter;
    @NonNull
    private Spinner header;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_1, null,
                new String[]{RepetitionRulesTable.COLUMN_TYPE},
                new int[]{android.R.id.text1}, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(@NonNull View view, @NonNull Cursor cursor, int columnIndex) {
                String full = Utils.repetitionRuleTextFromCursor(cursor, template());
                ((TextView) view).setText(full);
                return true;
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
        setListAdapter(null);
        header = (Spinner) getLayoutInflater(savedInstanceState)
                .inflate(R.layout.fragment_task_repetition_header, lv, false);
        header.setSelection(((InstanceHolderActivity) getActivity()).task.getRepeat().ordinal());
        lv.addHeaderView(header, null, false);
        header.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> ignored, View view,
                                       int position, long id) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //no-op;
            }
        });
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        Task task = ((InstanceHolderActivity) getActivity()).task;
        task.setRepeat(RepeatTypes.values()[header.getSelectedItemPosition()]);
        task.flushNow();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_repetition, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                long taskId = ((InstanceHolderActivity) getActivity()).task.forceId();
                if (taskId == -1L)
                    Toast.makeText(getActivity(), "Enter a task name first", Toast.LENGTH_LONG).show();
                else {
                    if (header.getSelectedItemPosition() == 0)
                        header.setSelection(1);
                    startActivity(new Intent(getActivity(), TaskRepetitionRuleActivity.class)
                            .putExtra("task", taskId).putExtra("template", template()));
                }
                return true;
        }
        return false;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(getActivity(), TaskRepetitionRuleActivity.class);
        i.putExtra("rule", id);
        i.putExtra("template", template());
        startActivity(i);
    }

    @Nullable
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GoDoContentProvider.REPETITION_RULES_URI,
                new String[]{RepetitionRulesTable.COLUMN_ID, RepetitionRulesTable.COLUMN_TASK,
                        RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE,
                        RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO},
                RepetitionRulesTable.COLUMN_TASK + "=?", new String[]{String.valueOf((((InstanceHolderActivity) getActivity()).task).forceId())}, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private boolean template() {
        return header.getSelectedItemPosition() == 2;
    }


}
