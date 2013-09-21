package fake.domain.adamlopresto.godo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;
import fake.domain.adamlopresto.godo.db.TaskContextTable;

public class TaskActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	public Task task;
	public Instance instance;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);
		
		if (getIntent() == null){
			Log.e("GoDo", "Null intent");
		} else {
			if (getIntent().getExtras() == null)
				Log.e("GoDo", "No extras");
			else {
				Bundle bundle = getIntent().getExtras();
				for (String key : bundle.keySet()) {
				    Object value = bundle.get(key);
				    Log.d("GoDo", String.format("%s %s (%s)", key,  
				        value.toString(), value.getClass().getName()));
				}
			}
				
		}
		if(!extractTaskAndOrInstanceFromBundle(savedInstanceState)){
			if (!extractTaskAndOrInstanceFromBundle(getIntent().getExtras())){
				instance = new Instance(DatabaseHelper.getInstance(this), this);
				task = instance.getTask();
			}
		}
		
		{
		long[] tmp;
				
		if ((tmp = getIntent().getLongArrayExtra("prereq")) != null){
			Log.e("GoDo", "Create prereqs");
			ContentValues cv = new ContentValues(2);
			cv.put(InstanceDependencyTable.COLUMN_SECOND, instance.forceId());
			for (long id : tmp){
				Log.e("GoDo", "Create "+id);
				cv.put(InstanceDependencyTable.COLUMN_FIRST, id);
				getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
			}
		}

		if ((tmp = getIntent().getLongArrayExtra("next")) != null){
			ContentValues cv = new ContentValues(2);
			cv.put(InstanceDependencyTable.COLUMN_FIRST, instance.forceId());
			for (long id : tmp){
				cv.put(InstanceDependencyTable.COLUMN_SECOND, id);
				getContentResolver().insert(GoDoContentProvider.DEPENDENCY_URI, cv);
			}
		}
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	private boolean extractTaskAndOrInstanceFromBundle(Bundle bundle) {
		if (bundle == null)
			return false;
		long instance_id = bundle.getLong("instance", -1L);
		if (instance_id != -1L){
			instance = Instance.get(DatabaseHelper.getInstance(this), instance_id);
			task = instance.getTask();
			return true;
		} else {
			long task_id = bundle.getLong("task", -1L);
			if (task_id != -1L){
				DatabaseHelper helper = DatabaseHelper.getInstance(this);
				task = Task.get(helper, task_id);
				instance = new Instance(helper, task);
				return true;
			} else {
				ArrayList<String> names = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
				if (names != null){
					DatabaseHelper helper = DatabaseHelper.getInstance(this);
					String name = names.get(0);
					task = new Task(helper, this, Character.toTitleCase(name.charAt(0))+name.substring(1));
					instance = new Instance(helper, task);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task, menu);
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
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_contexts:
			final long task_id = task.forceId();
			final SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableDatabase();
			Cursor cursor = db.query(ContextsTable.TABLE, new String[]{ContextsTable.COLUMN_ID, ContextsTable.COLUMN_NAME, 
					"exists (select * from "+TaskContextTable.TABLE+" where "+TaskContextTable.COLUMN_TASK+"="+task_id+" and context=contexts._id) AS selected"}, null, null, null, null, null);
			final ArrayList<Long> orig = new ArrayList<Long>(cursor.getCount());
			cursor.moveToFirst();
			while (!cursor.isAfterLast()){
				orig.add(Long.valueOf(cursor.getLong(0)));
				cursor.moveToNext();
			}
			cursor.moveToFirst();
			
			final HashSet<Long> toAdd = new HashSet<Long>();
			final HashSet<Long> toDel = new HashSet<Long>();
			
			new AlertDialog.Builder(this)
				.setMultiChoiceItems(cursor, "selected", ContextsTable.COLUMN_NAME, 
						new DialogInterface.OnMultiChoiceClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								Long id = orig.get(which);
								if (isChecked){
									if (!toDel.remove(id))
										toAdd.add(id);
								} else {
									if (!toAdd.remove(id))
										toDel.add(id);
								}
							}
						})
				.setTitle(R.string.title_activity_contexts)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Set", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String[] whereArgs = new String[]{String.valueOf(task_id), null};
						for (Long id : toDel){
							whereArgs[1]=String.valueOf(id);
							db.delete(TaskContextTable.TABLE, 
									TaskContextTable.COLUMN_TASK + "=? AND "+TaskContextTable.COLUMN_CONTEXT + "=?", 
									whereArgs);
						}
						
						ContentValues cv = new ContentValues(2);
						cv.put(TaskContextTable.COLUMN_TASK, task_id);
						for (Long id : toAdd){
							cv.put(TaskContextTable.COLUMN_CONTEXT, id);
							db.insert(TaskContextTable.TABLE, null, cv);
						}
					}
				})
				.show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("task", task.forceId());
		outState.putLong("instance", instance.forceId());
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Log.d("GoDo", "SectionsPagerAdapter getItem("+position+")");
			// getItem is called to instantiate the fragment for the given page.
			switch (position){
			case 0:{
				Fragment f = new TaskDetailsFragment();
				return f;
			}
			case 1:{
				Fragment f = new TaskRepetitionRuleFragment();
				return f;
			}
			case 2:{
				Fragment f = new TaskHistoryFragment();
				return f;
			}
			case 3:{
				Fragment f = new DependencyFragment();
				Bundle b = new Bundle();
				b.putBoolean("prereq", true);
				f.setArguments(b);
				return f;
			}
			case 4:{
				Fragment f = new DependencyFragment();
				return f;
			}
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public int getCount() {
			// total number of pages
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_fragment_task_details).toUpperCase(l);
			case 1:
				return getString(R.string.title_fragment_task_repetitions).toUpperCase(l);
			case 2:
				return "History";
			case 3:
				return "Prerequesites";
			case 4:
				return "Next steps";
			}
			return null;
		}
	}
}
