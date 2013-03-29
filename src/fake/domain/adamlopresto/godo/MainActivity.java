package fake.domain.adamlopresto.godo;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{

	private SimpleCursorAdapter adapter;
	
	private String[] projection = new String[]{"task_name", "task_notes"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null,
				projection,
				new int[]{android.R.id.text1, android.R.id.text2}, 
				0);
		
		setListAdapter(adapter);
	}
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		case R.id.action_notify:
			TasksNotification.notify(this, "Main notification", 5);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent i = new Intent(this, TaskActivity.class);
		i.putExtra("instance", id);
		startActivity(i);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = GoDoContentProvider.INSTANCES_URI;
		
		String where = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_CONTEXT, false))
			where = "NOT blocked_by_context";
		
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_BLOCKED_BY_TASK, false))
			where = DatabaseUtils.concatenateWhere(where, "NOT blocked_by_task");
		
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_DONE, false))
			where = DatabaseUtils.concatenateWhere(where, "done_date IS NULL");
		
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_FUTURE, false))
			where = DatabaseUtils.concatenateWhere(where, "COALESCE(plan_date, start_date, '') < current_timestamp");
		
		CursorLoader cursorLoader = new CursorLoader(this,
				uri, new String[]{"_id", "task_name", "task_notes"}, where, null, null);
		return cursorLoader;
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
