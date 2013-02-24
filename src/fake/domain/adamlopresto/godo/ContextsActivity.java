package fake.domain.adamlopresto.godo;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import fake.domain.adamlopresto.godo.db.ContextsTable;

public class ContextsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private SimpleCursorAdapter adapter;
	
	private String[] projection = new String[]{ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC, ContextsTable.COLUMN_ACTIVE};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contexts);
		// Show the Up button in the action bar.
		setupActionBar();
		
		adapter = new SimpleCursorAdapter(this, R.layout.item_active_contexts, null,
				projection,
				new int[]{android.R.id.text1, android.R.id.text2, R.id.active}, 
				0);
		adapter.setViewBinder(new ViewBinder(){

			@Override
			public boolean setViewValue(View v, Cursor c, int column) {
				if (column == 3){
					((CheckBox)v).setChecked(c.getInt(column) != 0);
					return true;
				}
				return false;
			}
			
		});
		
		setListAdapter(adapter);
		getLoaderManager().restartLoader(0, null, this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contexts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri uri = GoDoContentProvider.CONTEXTS_URI;
		CursorLoader cursorLoader = new CursorLoader(this,
				uri, 
				new String[]{"_id", ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC, 
				             ContextsTable.COLUMN_ACTIVE}, 
				null, null, null);
		return cursorLoader;
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
