package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	
	public final static String PREF_SHOW_BLOCKED_BY_TASK = "pref_show_blocked_by_task";
	public final static String PREF_SHOW_BLOCKED_BY_CONTEXT = "pref_show_blocked_by_context";
	public final static String PREF_SHOW_DONE = "pref_show_done";
	public final static String PREF_SHOW_FUTURE = "pref_show_future";
	public final static String PREF_DEFAULT_NOTIFICATION = "pref_default_notification";
	public final static String PREF_DEFAULT_DUE_NOTIFICATION = "pref_default_due_notification";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
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
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class SettingsFragment extends PreferenceFragment {
		@Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
	}

}
