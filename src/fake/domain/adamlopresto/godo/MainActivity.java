package fake.domain.adamlopresto.godo;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SimpleCursorAdapter;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{

	private SimpleCursorAdapter adapter;
	
	private String[] projection = new String[]{"task_name", "task_notes"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, null,
				projection,
				new int[]{android.R.id.text1, android.R.id.text2}, 
				0);
		
		setListAdapter(adapter);
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri uri = GoDoContentProvider.AVAILABLE_INSTANCES_URI;
		CursorLoader cursorLoader = new CursorLoader(this,
				uri, new String[]{"_id", "task_name", "task_notes"}, null, null, null);
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
