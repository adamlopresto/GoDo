package fake.domain.adamlopresto.godo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

/**
 * Used to determine when the app loses focus, so we can put up a notification.
 * Code adapted from http://www.mjbshaw.com/2012/12/determining-if-your-android-application.html
 */
public class GoDoApplication extends Application implements Application.ActivityLifecycleCallbacks {

    //Number of current activities with focus. Will be 0 when GoDo is in the background, 1 when it's
    //not, and 2 occasionally as we move from one activity to another.
    private int current;

    @Override
    public void onCreate() {
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
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
