package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

public class TaskRepetitionRuleFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
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
	        inflater.inflate(R.menu.context_context, menu);
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
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.edit:{
	            	final long id = getListView().getCheckedItemIds()[0];
	                mode.finish(); // Action picked, so close the CAB
	                Intent i = new Intent(getActivity(), TaskRepetitionRuleActivity.class);
	                i.putExtra("rule", id);
	                startActivity(i);
	                return true;
	            }
	            case R.id.delete:{
	            	final long[] ids = getListView().getCheckedItemIds();
	            	new AlertDialog.Builder(getActivity())
	            	.setMessage("Delete these rules?")
	            	.setNegativeButton(android.R.string.cancel, null)
	            	.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentResolver res = getActivity().getContentResolver();
							String where = RepetitionRulesTable.COLUMN_ID + "=?";
							String[] idArray = new String[1];
							//
							for (long id : ids){
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

		adapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_activated_1, null,
				new String[] { RepetitionRulesTable.COLUMN_TYPE },
				new int[] { android.R.id.text1 }, 0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				((TextView)view).setText(DatabaseUtils.dumpCurrentRowToString(cursor));
				return true;
			}
		});
		
		setListAdapter(adapter);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(mActionModeCallback);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume(){
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.task_repetition, menu);
		super.onCreateOptionsMenu(menu, inflater);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			startActivity(new Intent(getActivity(), TaskRepetitionRuleActivity.class)
				.putExtra("task", ((TaskActivity) getActivity()).task.getId()));
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(getActivity(), TaskRepetitionRuleActivity.class);
		i.putExtra("rule", id);
		startActivity(i);
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
