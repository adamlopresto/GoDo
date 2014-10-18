package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import fake.domain.adamlopresto.godo.db.InstancesView;

public class TaskHistoryFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private HistoryAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new HistoryAdapter(getActivity());

    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    private long getTaskId() {
        return ((InstanceHolderActivity) getActivity()).task.getId();
    }

    @Nullable
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = InstancesView.COLUMN_TASK + "=?";

        return new CursorLoader(getActivity(), GoDoContentProvider.INSTANCES_URI,
                new String[]{InstancesView.COLUMN_ID, InstancesView.COLUMN_INSTANCE_NOTES,
                        InstancesView.COLUMN_CREATE_DATE, InstancesView.COLUMN_START_DATE,
                        InstancesView.COLUMN_PLAN_DATE, InstancesView.COLUMN_DUE_DATE,
                        InstancesView.COLUMN_DONE_DATE
                },
                where,
                new String[]{String.valueOf(getTaskId())},
                null
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

    private class HistoryAdapter extends ResourceCursorAdapter {


        @SuppressWarnings("UnusedDeclaration")
        private static final int ID = 0, NOTES = 1, CREATED = 2, START = 3, PLAN = 4, DUE = 5, DONE = 6;

        public HistoryAdapter(@NonNull Context context) {
            super(context, android.R.layout.simple_list_item_2, null, 0);
        }

        @Override
        public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
            String notes = cursor.getString(NOTES);
            TextView noteView = (TextView) view.findViewById(android.R.id.text1);
            if (TextUtils.isEmpty(notes))
                noteView.setVisibility(View.GONE);
            else {
                noteView.setVisibility(View.VISIBLE);
                noteView.setText(notes);
            }

            StringBuilder sb = new StringBuilder();

            appendIfContents(sb, "Created: ", cursor, CREATED);
            appendIfContents(sb, "Start: ", cursor, START);
            appendIfContents(sb, "Plan: ", cursor, PLAN);
            appendIfContents(sb, "Due: ", cursor, DUE);
            appendIfContents(sb, "Done: ", cursor, DONE);

            ((TextView) view.findViewById(android.R.id.text2)).setText(sb);
        }

        /* (non-Javadoc)
         * @see android.widget.ResourceCursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            if (v != null) {
                v.setMinimumHeight(0);
            }
            return v;
        }

        private void appendIfContents(@NonNull StringBuilder sb, String label, @NonNull Cursor cursor, int col) {
            String tmp = cursor.getString(col);
            if (!TextUtils.isEmpty(tmp)) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(label);
                sb.append(Utils.formatShortRelativeDate(tmp));
            }
        }

    }

}
