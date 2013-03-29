package fake.domain.adamlopresto.godo;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

public class TaskRepetitionRuleFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;

	public static TaskRepetitionRuleFragment newInstance() {
		TaskRepetitionRuleFragment fragment = new TaskRepetitionRuleFragment();
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskRepetitionRuleFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[]{RepetitionRulesTable.COLUMN_TYPE},
				new int[]{android.R.id.text1}, 
				0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				((TextView)view).setText(DatabaseUtils.dumpCurrentRowToString(cursor));
				return true;
			}
		});
		
		setListAdapter(adapter);
		getLoaderManager().restartLoader(0, null, this);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.task_repetition, menu);
		super.onCreateOptionsMenu(menu, inflater);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.action_new:
			startActivity(new Intent(getActivity(), TaskRepetitionRuleActivity.class));
			//TODO: start activity
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), GoDoContentProvider.REPETITION_RULES_URI, 
				new String[]{RepetitionRulesTable.COLUMN_ID, RepetitionRulesTable.COLUMN_TASK,
			RepetitionRulesTable.COLUMN_TYPE, RepetitionRulesTable.COLUMN_SUBVALUE, 
			RepetitionRulesTable.COLUMN_FROM, RepetitionRulesTable.COLUMN_TO},
			RepetitionRulesTable.COLUMN_TASK+"=?", new String[]{String.valueOf((((TaskActivity)getActivity()).task).getId())}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}