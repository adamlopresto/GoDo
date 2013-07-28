package fake.domain.adamlopresto.godo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;

public class NotificationService extends Service {
	private static TextToSpeech tts;
	
	public NotificationService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int max = 4;
		if (intent != null) 
			max = intent.getIntExtra("max_notify", 4);
		
		GoDoAppWidget.updateAllAppWidgets(this);
		
		ContentResolver res = getContentResolver();
		Cursor c = res.query(GoDoContentProvider.INSTANCES_URI, 
				new String[]{InstancesView.COLUMN_DUE_DATE}, 
				"due_date > current_timestamp AND done_date is null", 
				null, "due_date LIMIT 1");
		c.moveToFirst();
		String nextDueString = null;
		if (!c.isAfterLast())
			nextDueString = c.getString(0);
		c.close();
		
		c = res.query(GoDoContentProvider.INSTANCES_URI, 
				new String[]{"coalesce(plan_date, start_date)"}, 
				"NOT blocked_by_context AND NOT blocked_by_task AND coalesce(plan_date, start_date) > current_timestamp AND done_date is null", 
				null, "coalesce(plan_date, start_date) LIMIT 1");
		String nextPlanString = null;
		c.moveToFirst();
		if (!c.isAfterLast())
			nextPlanString = c.getString(0);
		c.close();
		
		String nextDateString;
		if (nextDueString == null)
			nextDateString = nextPlanString;
		else if (nextPlanString == null) 
			nextDateString = nextDueString;
		else if (nextDueString.compareTo(nextPlanString) < 0)
			nextDateString = nextDueString;
		else
			nextDateString = nextPlanString;
					                                   
		try {
			if (nextDateString != null){
				Date date = null;
				boolean quiet = false;
				if (nextDateString.length() > 10){
					date = DatabaseHelper.dateTimeFormatter.parse(nextDateString);
				} else {
					date = DatabaseHelper.dateFormatter.parse(nextDateString);
					quiet = true;
				}
				AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		    	
		    	Intent alarmIntent = new Intent(this, TaskerPluginReceiver.class);
		    	if (quiet)
		    		alarmIntent.putExtra("max_notify", 1);
		    	else
		    		alarmIntent.putExtra("max_notify", 4);
		    	PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		    	
		    	manager.set(AlarmManager.RTC_WAKEUP, date.getTime(), contentIntent);
			}
		} catch (ParseException ignored) {
			//if we can't parse the date, give up.
		}	
		
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
		
		if (max == 0){
			stopSelf();
			return START_NOT_STICKY;
		}
		
		c = res.query(GoDoContentProvider.INSTANCES_URI, 
				new String[]{
					InstancesView.COLUMN_TASK_NAME, InstancesView.COLUMN_TASK_NOTES, 
					InstancesView.COLUMN_INSTANCE_NOTES, 
					"max(0, " +
					"	(case when (length(due_date) > 10 and due_date <= current_timestamp) then due_notification else 0 end), " +
					"   (case when (NOT blocked_by_context AND NOT blocked_by_task AND COALESCE(plan_date, start_date, '') <= current_timestamp) then notification else 0 end)) " +
					"as notification",
					InstancesView.COLUMN_ID
				}, 
				"NOT task_name IS NULL " +
				"AND done_date IS NULL " +
				"AND ((NOT blocked_by_context AND NOT blocked_by_task " +
					  "AND COALESCE(plan_date, start_date, '') <= current_timestamp " +
					  "AND notification > 0" +
				"     ) OR "+
					 "(length(due_date) > 10 and due_date <= current_timestamp AND due_notification > 0)" +
				"    )",
				null, 
				"case when due_date <= current_timestamp then due_date || ' 23:59:59' else '9999-99-99' end, " +
				"coalesce(plan_date || ' 23:59:59', current_timestamp), due_date || ' 23:59:59' , notification DESC, random()"
				);
		c.moveToFirst();
		
		int numToNotify = 0;
		boolean audible = false;
		boolean vibrate = false;
		NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
		String name = null;
		String taskNotes = null;
		String instanceNotes = null;
		final ArrayList<String> spoken = new ArrayList<String>();
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
			
			switch(NotificationLevels.values()[Math.min(c.getInt(3), max)]){
			case SPOKEN:
				spoken.add(name);
				break;
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
		
		c.close();
		
		if (numToNotify > 0){

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

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
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				//stackBuilder.addParentStack(TaskActivity.class);
				stackBuilder.addNextIntentWithParentStack(
						new Intent(this, TaskActivity.class).putExtra("instance", id));
				builder.setContentIntent(stackBuilder.getPendingIntent(0,
									PendingIntent.FLAG_CANCEL_CURRENT));
			} else {
				builder.setContentTitle(numToNotify+" tasks")
				       .setContentText("GoDo")
				       .setTicker(numToNotify+" tasks")
					   .setNumber(numToNotify)

					.setContentIntent(
							PendingIntent.getActivity(
									this,
									0,
									new Intent(this, MainActivity.class),
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

			nm.notify("Tasks", 0, builder.build());
			
			if (spoken.isEmpty()){
				stopSelf();
			} else {
				tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
					@Override
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS){
							for (int i = 0; i < spoken.size()-1; i++){
								tts.speak(spoken.get(i), TextToSpeech.QUEUE_ADD, null);
							}
							HashMap<String, String> params = new HashMap<String, String>(1);
							params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Last");
							tts.speak(spoken.get(spoken.size()-1), TextToSpeech.QUEUE_ADD, params);
						} else {
							Log.e("GoDo", "Error "+status);
						}
					}
				});
				tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){

					@Override
					public void onDone(String utteranceId) {
						if ("Last".equals(utteranceId)){
							tts.shutdown();
							tts = null;
							stopSelf();
						}
					}

					@Override
					public void onError(String utteranceId) {
						if ("Last".equals(utteranceId)){
							tts.shutdown();
							tts = null;
							stopSelf();
						}
						Log.e("GoDo", "Error with "+utteranceId);
					}

					@Override
					public void onStart(String utteranceId) {
					}
					
				});
			}
		} else {
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
		
}
