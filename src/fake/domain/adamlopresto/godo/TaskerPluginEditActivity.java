package fake.domain.adamlopresto.godo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class TaskerPluginEditActivity extends Activity {

	private ArrayList<String> toEnable = new ArrayList<String>();
	private ArrayList<String> toDisable = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasker_plugin_edit);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
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
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
