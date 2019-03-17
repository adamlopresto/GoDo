package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class DependenciesActivity extends InstanceHolderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!extractTaskAndOrInstanceFromBundle(savedInstanceState)) {
            if (!extractTaskAndOrInstanceFromBundle(getIntent().getExtras())) {
                Log.e("GoDo", "Couldn't initialize repetition rule activity. Inform Adam at once");
                Toast.makeText(this, "Error, attempted to create repetitions without a task to tie them to.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0.0f);
        }
        //setContentView(R.layout.activity_repetition_rules_list);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new AllDependenciesFragment())
                    //.replace(R.id.container, new AllDependenciesFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
