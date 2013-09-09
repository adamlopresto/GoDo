package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.godo.db.InstancesView;

public class TaskHistoryFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private HistoryAdapter adapter;

	public static TaskHistoryFragment newInstance() {
		TaskHistoryFragment fragment = new TaskHistoryFragment();
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskHistoryFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new HistoryAdapter(getActivity(), null);
		
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
	public void onResume(){
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}
	
	private long getTaskId() {
		return ((TaskActivity)getActivity()).task.getId();
	}

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
				null);
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


		@SuppressWarnings("unused")
		private static final int ID = 0, NOTES = 1, CREATED=2, START=3, PLAN=4, DUE=5, DONE=6;

		public HistoryAdapter(Context context, Cursor c) {
			super(context, android.R.layout.simple_list_item_2, c, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String notes = cursor.getString(NOTES);
			TextView noteView = (TextView)view.findViewById(android.R.id.text1);
			if (TextUtils.isEmpty(notes))
				noteView.setVisibility(View.GONE);
			else {
				noteView.setVisibility(View.VISIBLE);
				noteView.setText(notes);
			}
			
			StringBuilder sb = new StringBuilder();
			String tmp = cursor.getString(CREATED);
			if (!TextUtils.isEmpty(tmp)){
				sb.append("Created: ");
				sb.append(DateCalc.formatShortRelativeDate(tmp));
			}

			tmp = cursor.getString(START);
			if (!TextUtils.isEmpty(tmp)){
				sb.append("\nStart: ");
				sb.append(DateCalc.formatShortRelativeDate(tmp));
			}

			tmp = cursor.getString(PLAN);
			if (!TextUtils.isEmpty(tmp)){
				sb.append("\nPlan: ");
				sb.append(DateCalc.formatShortRelativeDate(tmp));
			}

			tmp = cursor.getString(DUE);
			if (!TextUtils.isEmpty(tmp)){
				sb.append("\nDue: ");
				sb.append(DateCalc.formatShortRelativeDate(tmp));
			}

			tmp = cursor.getString(DONE);
			if (!TextUtils.isEmpty(tmp)){
				sb.append("\nCompleted: ");
				sb.append(DateCalc.formatShortRelativeDate(tmp));
			}
			
			((TextView)view.findViewById(android.R.id.text2)).setText(sb);
		}
		
	}

}
