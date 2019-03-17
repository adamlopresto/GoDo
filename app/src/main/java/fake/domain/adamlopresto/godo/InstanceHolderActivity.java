package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public abstract class InstanceHolderActivity extends AppCompatActivity {
    public static final String EXTRA_INSTANCE = "instance";
    public static final String EXTRA_TASK = "task";

    //These are initialized in subclasses, but not in the constructors because of silly lifecycle
    //issues.
    @SuppressWarnings ("NullableProblems")
    @NonNull
    public Task task;
    @SuppressWarnings ("NullableProblems")
    @NonNull
    public Instance instance;

    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    protected boolean extractTaskAndOrInstanceFromBundle(@Nullable Bundle bundle) {
        if (bundle == null)
            return false;
        long instance_id = bundle.getLong(EXTRA_INSTANCE, -1L);
        if (instance_id != -1L) {
            instance = Instance.get(DatabaseHelper.getInstance(this), instance_id);
            task = instance.getTask();
            return true;
        }
        long task_id = bundle.getLong(EXTRA_TASK, -1L);
        if (task_id != -1L) {
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            task = Task.get(helper, task_id);
            instance = task.createRepetition(null);
            return true;
        }
        List<String> names = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        if (names != null) {
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            instance = Instance.createFromNames(helper, this, names);
            task = instance.getTask();
            return true;
        }
        String name = bundle.getString("task_name");
        if (name != null) {
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            instance = Instance.createFromName(helper, this, name);
            task = instance.getTask();
            return true;

        }
        return false;
    }
}
