package fake.domain.adamlopresto.godo;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SimpleCursorAdapter;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class MainActivity extends ListActivity {

	private SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, null,
				new String[]{"task_name", "task_notes"},
				new int[]{android.R.id.text1, android.R.id.text2}, 
				0);
		
		setListAdapter(adapter);
		new DatabaseHelper(this).getWritableDatabase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
