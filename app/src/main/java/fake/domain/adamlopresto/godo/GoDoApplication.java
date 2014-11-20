package fake.domain.adamlopresto.godo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by adam on 11/19/2014.
 */
public class GoDoApplication extends Application implements Application.ActivityLifecycleCallbacks {

    // I use two separate variables here. You can, of course, just use one and
    // increment/decrement it instead of using two and incrementing both.
    private int current;

    @Override
    public void onCreate() {
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        ++current;
        startService(new Intent(this, NotificationService.class).putExtra("max_notify", 0));
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (--current == 0)
            startService(new Intent(this, NotificationService.class).putExtra("max_notify", 1));
    }
}
