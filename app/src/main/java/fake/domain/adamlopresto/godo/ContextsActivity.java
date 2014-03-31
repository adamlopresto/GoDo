package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import fake.domain.adamlopresto.godo.db.ContextsTable;

public class ContextsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private SimpleCursorAdapter adapter;
	
	private String[] projection = new String[]{ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC, ContextsTable.COLUMN_ACTIVE};
	
	private AbsListView.MultiChoiceModeListener mActionModeCallback = new AbsListView.MultiChoiceModeListener() {
		private MenuItem editItem;
	
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
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
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.edit:{
	            	AlertDialog.Builder b = new AlertDialog.Builder(ContextsActivity.this);
	            	View inner = LayoutInflater.from(b.getContext()).inflate(R.layout.context_details, null);
	            	final EditText name = (EditText)inner.findViewById(R.id.context_name);
	            	final EditText desc = (EditText)inner.findViewById(R.id.context_desciption);
	            	
	            	final long id = getListView().getCheckedItemIds()[0];
	            	Cursor c = getContentResolver().query(GoDoContentProvider.CONTEXTS_URI,
	            			new String[]{ContextsTable.COLUMN_NAME, ContextsTable.COLUMN_DESC}, 
	            			ContextsTable.COLUMN_ID+"=?", 
	            			new String[]{String.valueOf(id)}, null);
	            	c.moveToFirst();
	            	name.setText(c.getString(0));
	            	desc.setText(c.getString(1));
	            	
	            	b.setView(inner).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentValues values = new ContentValues(2);
							values.put(ContextsTable.COLUMN_NAME, name.getText().toString());
							values.put(ContextsTable.COLUMN_DESC, desc.getText().toString());
							
							getContentResolver().update(GoDoContentProvider.CONTEXTS_URI, values, 
	            			ContextsTable.COLUMN_ID+"=?", 
	            			new String[]{String.valueOf(id)});
						}
					})
	            	 .setNegativeButton(android.R.string.cancel, null).show();
	            	
	            	
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            }
	            case R.id.delete:{
	            	final long[] ids = getListView().getCheckedItemIds();
	            	new AlertDialog.Builder(ContextsActivity.this)
	            	.setMessage("Delete these contexts?")
	            	.setNegativeButton(android.R.string.cancel, null)
	            	.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentResolver res = getContentResolver();
							String where = ContextsTable.COLUMN_ID + "=?";
							String[] idArray = new String[1];
							//
							for (long id : ids){
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contexts);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
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
		
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		getListView().setMultiChoiceModeListener(mActionModeCallback);
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
		case R.id.action_new_context:
			AlertDialog.Builder b = new AlertDialog.Builder(ContextsActivity.this);
        	View inner = LayoutInflater.from(b.getContext()).inflate(R.layout.context_details, null);
        	final EditText name = (EditText)inner.findViewById(R.id.context_name);
        	final EditText desc = (EditText)inner.findViewById(R.id.context_desciption);
        	
        	b.setView(inner).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContentValues values = new ContentValues(2);
					values.put(ContextsTable.COLUMN_NAME, name.getText().toString());
					values.put(ContextsTable.COLUMN_DESC, desc.getText().toString());
					
					getContentResolver().insert(GoDoContentProvider.CONTEXTS_URI, values);
				}
			})
        	 .setNegativeButton(android.R.string.cancel, null).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id){
		getContentResolver().update(GoDoContentProvider.TOGGLE_CONTEXT_URI, null, ContextsTable.COLUMN_ID + "=?", new String[]{Long.toString(id)});
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
