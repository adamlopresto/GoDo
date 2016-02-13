package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

import java.util.List;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public abstract class InstanceHolderActivity extends ActionBarActivity {
    public static final String EXTRA_INSTANCE = "instance";
    public static final String EXTRA_TASK = "task";
    @NonNull
    public Task task;
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
            String name = names.get(0);
            task = new Task(helper, this, Character.toTitleCase(name.charAt(0)) + name.substring(1));
            instance = Instance.fromTaskByName(helper, task);
            return true;
        }
        String name = bundle.getString("task_name");
        if (name != null) {
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            task = new Task(helper, this, Character.toTitleCase(name.charAt(0)) + name.substring(1));
            instance = Instance.fromTaskByName(helper, task);
            return true;

        }
        return false;
    }
}
