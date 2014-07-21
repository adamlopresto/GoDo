package fake.domain.adamlopresto.godo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class GoDoReceiver extends BroadcastReceiver {
    public static final String MARK_COMPLETE_INTENT = "fake.domain.adamlopresto.godo.MARK_COMPLETE";

    @Override
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {

        String action = intent.getAction();
        if (action == null ||
                "com.twofortyfouram.locale.intent.action.FIRE_SETTING".equals(action) ||
                "android.intent.action.BOOT_COMPLETED".equals(action)) {
            Bundle extras = intent.getExtras();
            ContentResolver res = context.getContentResolver();
            ContentValues values = new ContentValues(1);
            String where = ContextsTable.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[1];

            int max = 4;
            if (extras != null) {
                String[] deactivate = extras.getStringArray("deactivate");
                if (deactivate != null) {
                    values.put(ContextsTable.COLUMN_ACTIVE, 0);
                    for (String d : deactivate) {
                        selectionArgs[0] = d;
                        res.update(GoDoContentProvider.CONTEXTS_URI, values, where, selectionArgs);
                    }
                }

                String[] activate = extras.getStringArray("activate");
                if (activate != null) {
                    values.put(ContextsTable.COLUMN_ACTIVE, 1);
                    for (String a : activate) {
                        selectionArgs[0] = a;
                        res.update(GoDoContentProvider.CONTEXTS_URI, values, where, selectionArgs);
                    }
                }

                max = extras.getInt("max_notify", 4);
            }
            if (max > 0)
                context.startService(new Intent(context, NotificationService.class).putExtra("max_notify", max));
        } else if (action.equals(MARK_COMPLETE_INTENT)) {
            long id = intent.getLongExtra(InstanceHolderActivity.EXTRA_INSTANCE, -1L);
            if (id != -1) {
                Instance instance = Instance.get(DatabaseHelper.getInstance(context), id);
                instance.setDoneDate(new Date());
                instance.flush();
            }
            context.startService(new Intent(context, NotificationService.class).putExtra("max_notify", 1));
        } else {
            Log.e("GoDo", "Unknown intent action " + action + ", " + intent);
        }

    }

}
