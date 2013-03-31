package fake.domain.adamlopresto.godo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.InstancesView;

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
		
		notify(context);
	}
		
	public static void notify(Context context){
		ContentResolver res = context.getContentResolver();
		Cursor c = res.query(GoDoContentProvider.INSTANCES_URI, 
				new String[]{
					InstancesView.COLUMN_TASK_NAME, InstancesView.COLUMN_TASK_NOTES, 
					InstancesView.COLUMN_INSTANCE_NOTES, InstancesView.COLUMN_NOTIFICATION, 
					InstancesView.COLUMN_ID
				}, 
				"NOT blocked_by_context AND NOT blocked_by_task AND done_date IS NULL " +
				"AND COALESCE(plan_date, start_date, '') < current_timestamp AND notification > 0",
				null, null);
		c.moveToFirst();
		
		int numToNotify = 0;
		boolean audible = false;
		boolean vibrate = false;
		NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
		String name = null;
		String taskNotes = null;
		String instanceNotes = null;
		long id = -1L;
		while (!c.isAfterLast()){
			SpannableStringBuilder sb = new SpannableStringBuilder();
			numToNotify++;
			name = c.getString(0);
			sb.append(name);
			sb.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
				name.length(), 0);
			taskNotes = c.getString(1);
			if (!TextUtils.isEmpty(taskNotes)){
				sb.append(" ");
				sb.append(taskNotes);
			}
			instanceNotes = c.getString(2);
			if (!TextUtils.isEmpty(instanceNotes)){
				sb.append(" ");
				sb.append(instanceNotes);
			}
			inbox.addLine(sb);
			
			switch(NotificationLevels.values()[c.getInt(3)]){
			case SPOKEN:
				//TODO: speech
			case NOISY:
				audible = true;
			case VIBRATE:
				vibrate = true;
			default:
				break;
			}
			
			id = c.getLong(4);
			c.moveToNext();
		}
		
		if (numToNotify > 0){
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

					// Set required fields, including the small icon, the
					// notification title, and text.
					.setSmallIcon(R.drawable.ic_stat_tasks)
					.setContentTitle("GoDo")
					.setContentText("Text")
					.setAutoCancel(true);
			
			int defaults = Notification.DEFAULT_LIGHTS;
			if (audible)
				defaults |= Notification.DEFAULT_SOUND;
			if (vibrate)
				defaults |= Notification.DEFAULT_VIBRATE;
			builder.setDefaults(defaults);
			
			if (numToNotify == 1){
				builder.setContentTitle(name)
				       .setContentText(taskNotes)
				       .setSubText(instanceNotes)
				       .setTicker(name);
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
				stackBuilder.addParentStack(TaskActivity.class);
				stackBuilder.addNextIntent(
						new Intent(context, TaskActivity.class).putExtra("instance", id));
				builder.setContentIntent(stackBuilder.getPendingIntent(0,
									PendingIntent.FLAG_UPDATE_CURRENT));
			} else {
				builder.setContentTitle(numToNotify+" tasks")
				       .setContentText("GoDo")
				       .setTicker(numToNotify+" tasks")
					   .setNumber(numToNotify)

					.setContentIntent(
							PendingIntent.getActivity(
									context,
									0,
									new Intent(context, MainActivity.class),
									PendingIntent.FLAG_UPDATE_CURRENT))

					.setStyle(inbox);

					/*
					// Example additional actions for this notification. These will
					// only show on devices running Android 4.1 or later, so you
					// should ensure that the activity in this notification's
					// content intent provides access to the same actions in
					// another way.
					.addAction(
							R.drawable.ic_action_stat_share,
							res.getString(R.string.action_share),
							PendingIntent.getActivity(context, 0, Intent
									.createChooser(
											new Intent(Intent.ACTION_SEND).setType(
													"text/plain")
													.putExtra(Intent.EXTRA_TEXT,
															"Dummy text"),
											"Dummy title"),
									PendingIntent.FLAG_UPDATE_CURRENT))
					.addAction(R.drawable.ic_action_stat_reply,
							res.getString(R.string.action_reply), null)
							*/
			} // end switch on numToNotify

			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify("Tasks", 0, builder.build());
		}
	}
}
