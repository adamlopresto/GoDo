package fake.domain.adamlopresto.godo;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{

	private TaskAdapter adapter;
	
	private boolean paused = false;
	
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
	        mode.setTitle("Tasks");
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
	                Intent i = new Intent(MainActivity.this, TaskActivity.class);
	                i.putExtra("instance", id);
	                startActivity(i);
	                return true;
	            }
	            case R.id.delete:{
	            	final long[] ids = getListView().getCheckedItemIds();
	            	new AlertDialog.Builder(MainActivity.this)
	            	.setMessage("Delete these tasks?")
	            	.setNegativeButton(android.R.string.cancel, null)
	            	.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentResolver res = getContentResolver();
							String where = TasksTable.COLUMN_ID + "=?";
							String[] idArray = new String[1];
							
							for (long id : ids){
								idArray[0] = String.valueOf(id);
								res.delete(GoDoContentProvider.INSTANCES_URI, where, idArray);
							}
							getLoaderManager().restartLoader(0, null, MainActivity.this);
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
		setContentView(R.layout.activity_main);
		
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(mActionModeCallback);
		
		adapter = new TaskAdapter(this, R.layout.main_list_item, null, true);
		/*
		adapter = new SimpleCursorAdapter(this, R.layout.main_list_item, null,
				new String[]{ "task_name",    "task_notes",    "instance_notes",    "due_date",    "plan_date", "done_date"},
				new int[]{R.id.task_name, R.id.task_notes, R.id.instance_notes, R.id.due_date, R.id.plan_date, R.id.check}, 
				0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){ 
			
			@SuppressWarnings("unused")
			private static final int ID = 0;
			@SuppressWarnings("unused")
			private static final int TASK_NAME = 1;
			@SuppressWarnings("unused")
			private static final int TASK_NOTES = 2;
			@SuppressWarnings("unused")
			private static final int INSTANCE_NOTES = 3;
			private static final int DUE_DATE = 4;
			private static final int PLAN_DATE = 5;
			private static final int DONE_DATE = 6;
			
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (cursor.isNull(columnIndex) && columnIndex != DONE_DATE){
					view.setVisibility(View.GONE);
					return true;
				}
				TextView tv = (TextView)view;
				view.setVisibility(View.VISIBLE);
				boolean done = !cursor.isNull(DONE_DATE);
				if (!done){
					tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG); 
				} else { 
					tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
				
				if (DateCalc.isBeforeNow(cursor.getString(DUE_DATE)))
					tv.setTextColor(Color.RED);
				else if (DateCalc.isAfterNow(cursor.getString(PLAN_DATE)))
					tv.setTextColor(Color.GRAY);
				else
					tv.setTextColor(Color.BLACK);
				
				switch (columnIndex){
				case DUE_DATE:
					tv.setText("D: "+DateCalc.formatShortRelativeDate(cursor.getString(columnIndex)));
					return true;
				case PLAN_DATE:
					tv.setText("P: "+DateCalc.formatShortRelativeDate(cursor.getString(columnIndex)));
					return true;
				case DONE_DATE:
					((CheckBox)view).setChecked(done);
					return true;
				}
					
				return false;
			}
			
		});
		*/
		
		setListAdapter(adapter);
		getLoaderManager().restartLoader(0, null, this);
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		new Thread(new Runnable(){

			@Override
			public void run() {
				getContentResolver().delete(GoDoContentProvider.TASKS_URI, TasksTable.COLUMN_NAME+" IS NULL", null);
			}
		}).start();
	}


	@Override
	protected void onResume() {
		super.onResume();
		startService(new Intent(this, NotificationService.class).putExtra("max_notify", 0));
		if (paused){
			getLoaderManager().restartLoader(0, null, this);
			paused = false;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
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
		case R.id.action_new_from_template:{
			final Cursor cursor = getContentResolver().query(GoDoContentProvider.TASKS_URI, 
					new String[]{ TasksTable.COLUMN_ID, TasksTable.COLUMN_NAME, 
					              TasksTable.COLUMN_NOTES }, 
					TasksTable.COLUMN_REPEAT+"=2", null, TasksTable.COLUMN_NAME);
			new AlertDialog.Builder(this)
				.setAdapter(
					new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, 
							cursor, new String[]{TasksTable.COLUMN_NAME, TasksTable.COLUMN_NOTES}, 
							new int[]{android.R.id.text1, android.R.id.text2}, 0), 
					new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							cursor.moveToPosition(which);
							long id = cursor.getLong(0);
							Intent i = new Intent(MainActivity.this, TaskActivity.class);
							i.putExtra("task", id);
							startActivity(i);

						}

					})
				.setNegativeButton(android.R.string.cancel, null)
				.show();

			return true;
		}
		case R.id.action_notify:
			startService(new Intent(this, NotificationService.class));
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
	
	public void checkBoxClick(View v){
		ListView lv = getListView();
		CheckBox cb = (CheckBox)v;
		Instance inst = Instance.get(DatabaseHelper.getInstance(this), lv.getItemIdAtPosition(lv.getPositionForView(v)));
		inst.updateDone(cb.isChecked());
		inst.flush();
		getContentResolver().notifyChange(GoDoContentProvider.INSTANCES_URI, null);
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
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_FUTURE, false))
			where = DatabaseUtils.concatenateWhere(where, "coalesce(start_date,0) < DATETIME('now', 'localtime')");
		
		if (where == null)
			where = "";
		else 
			where = "("+where+") or ";
		where = where+"(length(due_date) > 10 and due_date <= DATETIME('now', 'localtime'))";
		
		if (!prefs.getBoolean(SettingsActivity.PREF_SHOW_DONE, false))
			where = DatabaseUtils.concatenateWhere(where, "done_date is null or done_date > DATETIME('now', '-1 hours', 'localtime')");
		
		where = DatabaseUtils.concatenateWhere(where, "task_name is not null");
		
		CursorLoader cursorLoader = new CursorLoader(this, uri, 
				TaskAdapter.PROJECTION,
				where, null, TaskAdapter.SORT);
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
