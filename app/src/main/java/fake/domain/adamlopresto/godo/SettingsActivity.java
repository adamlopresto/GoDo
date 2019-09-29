package fake.domain.adamlopresto.godo;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_SHOW_BLOCKED_BY_TASK = "pref_show_blocked_by_task";
    public static final String PREF_SHOW_BLOCKED_BY_CONTEXT = "pref_show_blocked_by_context";
    public static final String PREF_SHOW_DONE = "pref_show_done";
    public static final String PREF_SHOW_FUTURE = "pref_show_future";
    public static final String PREF_DEFAULT_NOTIFICATION = "pref_default_notification";
    public static final String PREF_DEFAULT_DUE_NOTIFICATION = "pref_default_due_notification";
    public static final String PREF_COLORFUL_WIDGET = "pref_colorful_widget";
    public static final String PREF_LED = "pref_led";
    public static final String PREF_SHOWOFF = "pref_showoff";
    public static final String PREF_THEME = "pref_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            ListPreference theme = findPreference(PREF_THEME);
            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //TODO
                    AppCompatDelegate.setDefaultNightMode(Integer.parseInt(newValue.toString()));
                    return true;
                }
            });
        }
    }

}
