package fake.domain.adamlopresto.godo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskerPluginReceiver extends BroadcastReceiver {
	public TaskerPluginReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		TasksNotification.notify(context, intent.getExtras().keySet().toString(), 7);
		//throw new UnsupportedOperationException("Not yet implemented");
	}
}
