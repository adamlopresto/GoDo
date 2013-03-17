package fake.domain.adamlopresto.godo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class TaskerPluginEditActivity extends Activity {

	private ArrayList<String> activate   = new ArrayList<String>();
	private ArrayList<String> deactivate = new ArrayList<String>();
	private TextView activate_view;
	private TextView deactivate_view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasker_plugin_edit);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		((Button)findViewById(R.id.activate_button)).setOnClickListener(new EditButtonClickListener(activate));
		((Button)findViewById(R.id.deactivate_button)).setOnClickListener(new EditButtonClickListener(deactivate));
		
		activate_view = (TextView)findViewById(R.id.activate);
		deactivate_view = (TextView)findViewById(R.id.deactivate);
		
		Bundle extras;
		Intent i;
		if ((i=getIntent()) != null && (extras=i.getExtras())!=null 
				&& (extras=extras.getBundle("com.twofortyfouram.locale.intent.extra.BUNDLE")) != null){
			String[] temp = extras.getStringArray("activate");
			if (temp != null)
				activate.addAll(Arrays.asList(temp));
			
			temp = extras.getStringArray("deactivate");
			if (temp != null)
				deactivate.addAll(Arrays.asList(temp));
		}	
		update();
		/*
		 * TODO:
		 * Four TextViews, two being labels "Contexts to Activate" and "Contexts to Deactivate"
		 * The other two will be comma separated lists of contexts, or the text "None".
		 * There will be Select buttons for each.
		 * use multi-select dialog like the contexts dialog for a task
		 * 
		 * Should we refactor that dialog into its own class? Seems to show up a lot.
		 */
	}
	
	private void update(){
		
		Bundle b = new Bundle();
		
		StringBuilder builder = new StringBuilder();
		String states;
		if (activate.isEmpty()){
			activate_view.setText(R.string.none);
		} else {
			states = TextUtils.join(", ", activate);
			builder.append("Activate: ");
			builder.append(states);
			activate_view.setText(states);
			b.putStringArray("activate", activate.toArray(new String[1]));
		}
		
		if (deactivate.isEmpty()){
			deactivate_view.setText(R.string.none);
		} else {
			states = TextUtils.join(", ", deactivate);
			if (builder.length() > 0)
				builder.append('\n');
			
			builder.append("Deactivate: ");
			builder.append(states);
			deactivate_view.setText(states);
			b.putStringArray("deactivate", deactivate.toArray(new String[1]));
		}
		
		if (b.isEmpty()){
			setResult(RESULT_CANCELED);
		} else {
			setResult(RESULT_OK, new Intent()
				.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", builder.toString())
				.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", b));
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class EditButtonClickListener implements OnClickListener {
		
		private ArrayList<String> list;
		
		public EditButtonClickListener(ArrayList<String> list){
			this.list = list;
		}
		
		public void onClick(View ignored){
				SQLiteDatabase db = new DatabaseHelper(TaskerPluginEditActivity.this).getReadableDatabase();
				Cursor cursor = db.query(ContextsTable.TABLE, new String[]{ContextsTable.COLUMN_NAME 
					}, null, null, null, null, null);
				
				int count = cursor.getCount();
				
				final String[] items = new String[count];
				boolean[] checkedItems = new boolean[count];
				
				final HashSet<String> temp = new HashSet<String>(list);
				
				cursor.moveToFirst();
				for (int i = 0; i < count; ++i){
					items[i] = cursor.getString(0);
					checkedItems[i] = temp.contains(items[i]);
					cursor.moveToNext();
				}
			
				new AlertDialog.Builder(TaskerPluginEditActivity.this)
				.setMultiChoiceItems(items, checkedItems, 
						new DialogInterface.OnMultiChoiceClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								if (isChecked){
									temp.add(items[which]);
								} else {
									temp.remove(items[which]);
								}
							}
						})
				.setTitle(R.string.title_activity_contexts)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						list.clear();
						list.addAll(temp);
						update();
					}
				})
				.show();
		}
	}
}