package fake.domain.adamlopresto.godo;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public abstract class InstanceHolderActivity extends FragmentActivity {
    public static final String EXTRA_INSTANCE = "instance";
    public static final String EXTRA_TASK = "task";
    @NotNull
    public Task task;
    @NotNull
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
            instance = new Instance(helper, task);
            return true;
        }
        String name = bundle.getString("task_name");
        if (name != null) {
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            task = new Task(helper, this, Character.toTitleCase(name.charAt(0)) + name.substring(1));
            instance = new Instance(helper, task);
            return true;

        }
        return false;
    }
}