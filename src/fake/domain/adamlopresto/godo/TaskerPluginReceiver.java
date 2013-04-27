package fake.domain.adamlopresto.godo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import fake.domain.adamlopresto.godo.db.ContextsTable;

public class TaskerPluginReceiver extends BroadcastReceiver {
	public TaskerPluginReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle extras = intent.getExtras();
		ContentResolver res = context.getContentResolver();
		ContentValues values = new ContentValues(1);
		String where = ContextsTable.COLUMN_NAME+"=?";
		String[] selectionArgs = new String[1];
		
		String[] deactivate = extras.getStringArray("deactivate");
		if (deactivate != null){
			values.put(ContextsTable.COLUMN_ACTIVE, 0);
			for (String d : deactivate){
				selectionArgs[0] = d;
				res.update(GoDoContentProvider.CONTEXTS_URI, values, where, selectionArgs);
			}
		}
		
		String[] activate = extras.getStringArray("activate");
		if (activate != null){
			values.put(ContextsTable.COLUMN_ACTIVE, 1);
			for (String a : activate){
				selectionArgs[0] = a;
				res.update(GoDoContentProvider.CONTEXTS_URI, values, where, selectionArgs);
			}
		}
		
		context.startService(new Intent(context, NotificationService.class));
	}
		
}
