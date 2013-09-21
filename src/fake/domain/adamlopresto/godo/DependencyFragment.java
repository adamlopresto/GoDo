package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;

public class DependencyFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private AbsListView.MultiChoiceModeListener mActionModeCallback = new AbsListView.MultiChoiceModeListener() {
	
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			final int checkedCount = getListView().getCheckedItemCount();
			mode.setSubtitle("" + checkedCount + " items selected");
		}
			
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_delete, menu);
	        mode.setTitle(prereq() ? "Prerequesites" : "Next Steps");
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
	            case R.id.delete:{
	            	final long[] ids = getListView().getCheckedItemIds();
	            	ContentResolver res = getActivity().getContentResolver();
	            	
	            	String where = prereq() 
            			? InstanceDependencyTable.COLUMN_FIRST  + "=? AND "
	            			+ InstanceDependencyTable.COLUMN_SECOND + "=?"
            			: InstanceDependencyTable.COLUMN_SECOND  + "=? AND "
	            			+InstanceDependencyTable.COLUMN_FIRST + "=?";
	            	
	            	String[] idArray = new String[2];
	            	idArray[1] = String.valueOf(getInstanceId());
	            	
	            	for (long id : ids){
	            		idArray[0] = String.valueOf(id);
	            		res.delete(GoDoContentProvider.DEPENDENCY_URI, where, idArray);
	            	}
	            	getLoaderManager().restartLoader(0, null, DependencyFragment.this);
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
	
	private TaskAdapter adapter;

	public static DependencyFragment newInstance() {
		DependencyFragment fragment = new DependencyFragment();
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DependencyFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new TaskAdapter(getActivity(), R.layout.main_list_item, null, false);
		
		
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
		setListAdapter(adapter);
		setHasOptionsMenu(true);
		setEmptyText("None");
	}

	@Override
	public void onResume(){
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dependency, menu);
		super.onCreateOptionsMenu(menu, inflater);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_pick_from_list:
			final Cursor c = getActivity().getContentResolver()
				.query(GoDoContentProvider.INSTANCES_URI, TaskAdapter.PROJECTION, "done_date is null", null, null);
			new AlertDialog.Builder(getActivity())
				.setAdapter(new TaskAdapter(getActivity(), R.layout.main_list_item, c, false), 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								c.moveToPosition(which);
								Toast.makeText(getActivity(), c.getString(1), Toast.LENGTH_LONG).show();
								ContentValues cv = new ContentValues(2);
								if (prereq()){
									cv.put(InstanceDependencyTable.COLUMN_FIRST, c.getLong(0));
									cv.put(InstanceDependencyTable.COLUMN_SECOND, forceInstanceId());
								} else {
									cv.put(InstanceDependencyTable.COLUMN_FIRST, forceInstanceId());
									cv.put(InstanceDependencyTable.COLUMN_SECOND, c.getLong(0));
								}
								getActivity().getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
								getLoaderManager().restartLoader(0, null, DependencyFragment.this);
							}
						})
				.setNegativeButton("Cancel", null)
				.show();
			return true;

		case R.id.action_new:{
			String otherside = prereq() ? "next" : "prereq";
			startActivity(new Intent(getActivity(), TaskActivity.class)
				.putExtra(otherside, new long[]{forceInstanceId()}));
			return true;
			}
		}
		return false;
	}
	
	private boolean prereq() {
		Bundle arguments = getArguments();
		return arguments != null && arguments.getBoolean("prereq");
	}
	
	private long forceInstanceId() {
		return ((TaskActivity)getActivity()).instance.forceId();
	}
	
	private long getInstanceId() {
		return ((TaskActivity)getActivity()).instance.getId();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		getListView().setItemChecked(position, true);
		//getActivity().startActionMode(mActionModeCallback);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where;
		
		if (getArguments() != null && getArguments().getBoolean("prereq"))
			where = "_id in (SELECT "+InstanceDependencyTable.COLUMN_FIRST +" FROM "+InstanceDependencyTable.TABLE 
					+" WHERE " + InstanceDependencyTable.COLUMN_SECOND + " = ? )";
		else
			where = "_id in (SELECT "+InstanceDependencyTable.COLUMN_SECOND +" FROM "+InstanceDependencyTable.TABLE 
					+" WHERE " + InstanceDependencyTable.COLUMN_FIRST + " = ? )";
			
		return new CursorLoader(getActivity(), GoDoContentProvider.INSTANCES_URI, 
				TaskAdapter.PROJECTION,
				where, 
				new String[]{String.valueOf(getInstanceId())}, 
				TaskAdapter.SORT);
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
