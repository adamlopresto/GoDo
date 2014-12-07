package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import fake.domain.adamlopresto.godo.db.ContextsTable;

public class ContextsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ContextFragment fragment = new ContextFragment();

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }


    public static class ContextFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String[] projection = {ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC, ContextsTable.COLUMN_ACTIVE};
        @NonNull
        private SimpleCursorAdapter adapter;
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
                inflater.inflate(R.menu.context_edit_delete, menu);
                editItem = menu.findItem(R.id.edit);
                mode.setTitle("Contexts");
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
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                        @SuppressLint ("InflateParams") View inner = LayoutInflater.from(b.getContext()).inflate(R.layout.context_details, null);
                        assert inner != null;
                        final EditText name = (EditText) inner.findViewById(R.id.context_name);
                        final EditText desc = (EditText) inner.findViewById(R.id.context_description);

                        final long id = getListView().getCheckedItemIds()[0];
                        Cursor c = getActivity().getContentResolver().query(GoDoContentProvider.CONTEXTS_URI,
                                new String[]{ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC},
                                ContextsTable.COLUMN_ID + "=?",
                                new String[]{String.valueOf(id)}, null);
                        if (c != null && c.moveToFirst()) {
                            name.setText(c.getString(0));
                            desc.setText(c.getString(1));
                        }

                        b.setView(inner).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues values = new ContentValues(2);
                                values.put(ContextsTable.COLUMN_NAME, Utils.getString(name));
                                values.put(ContextsTable.COLUMN_DESC, Utils.getString(desc));

                                getActivity().getContentResolver().update(GoDoContentProvider.CONTEXTS_URI, values,
                                        ContextsTable.COLUMN_ID + "=?",
                                        new String[]{String.valueOf(id)});
                            }
                        })
                                .setNegativeButton(android.R.string.cancel, null).show();


                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    }
                    case R.id.delete: {
                        final long[] ids = getListView().getCheckedItemIds();
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Delete these contexts?")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ContentResolver res = getActivity().getContentResolver();
                                        String where = ContextsTable.COLUMN_ID + "=?";
                                        String[] idArray = new String[1];
                                        //
                                        for (long id : ids) {
                                            idArray[0] = String.valueOf(id);
                                            res.delete(GoDoContentProvider.CONTEXTS_URI, where, idArray);
                                        }
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_contexts, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_active_contexts, null,
                    projection,
                    new int[]{android.R.id.text1, android.R.id.text2, R.id.active},
                    0);
            adapter.setViewBinder(new ViewBinder() {

                @Override
                public boolean setViewValue(@NonNull View v, @NonNull Cursor c, int column) {
                    if (column == 3) {
                        ((Checkable) v).setChecked(c.getInt(3) != 0);
                        return true;
                    }
                    return false;
                }

            });

            setListAdapter(adapter);
            getLoaderManager().restartLoader(0, null, this);

            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

            getListView().setMultiChoiceModeListener(mActionModeCallback);

            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.contexts, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. Use NavUtils to allow users
                    // to navigate up one level in the application structure. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                    //
                    NavUtils.navigateUpFromSameTask(getActivity());
                    return true;
                case R.id.action_new_context:
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    @SuppressLint ("InflateParams") View inner = LayoutInflater.from(b.getContext()).inflate(R.layout.context_details, null);
                    assert inner != null;
                    final EditText name = (EditText) inner.findViewById(R.id.context_name);
                    final EditText desc = (EditText) inner.findViewById(R.id.context_description);

                    b.setView(inner).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContentValues values = new ContentValues(2);
                            values.put(ContextsTable.COLUMN_NAME, Utils.getString(name));
                            values.put(ContextsTable.COLUMN_DESC, Utils.getString(desc));

                            getActivity().getContentResolver().insert(GoDoContentProvider.CONTEXTS_URI, values);
                        }
                    })
                            .setNegativeButton(android.R.string.cancel, null).show();
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            getActivity().getContentResolver().update(GoDoContentProvider.TOGGLE_CONTEXT_URI, null, ContextsTable.COLUMN_ID + "=?", new String[]{Long.toString(id)});
        }

        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
            return new CursorLoader(getActivity(),
                    GoDoContentProvider.CONTEXTS_URI,
                    new String[]{"_id", ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC,
                            ContextsTable.COLUMN_ACTIVE},
                    null, null, null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
            adapter.swapCursor(c);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            adapter.swapCursor(null);
        }

    }
}
