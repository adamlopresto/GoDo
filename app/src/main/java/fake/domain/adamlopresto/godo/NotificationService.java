package fake.domain.adamlopresto.godo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;

public class NotificationService extends Service {
    private static final String GROUP_KEY = "GoDoGroup";
    @Nullable
    private TextToSpeech tts;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        int max = 4;
        if (intent != null)
            max = intent.getIntExtra("max_notify", 4);

        //GoDoAppWidget.updateAllAppWidgets(this);

        ContentResolver res = getContentResolver();
        String nextDateString = nextDate(res, InstancesView.COLUMN_DUE_DATE);
        nextDateString = stringMin(nextDateString, nextDate(res, InstancesView.COLUMN_START_DATE));
        nextDateString = stringMin(nextDateString, nextDate(res, InstancesView.COLUMN_PLAN_DATE));

        try {
            if (nextDateString != null) {
                Date date;
                boolean quiet = false;
                if (nextDateString.length() > 10) {
                    date = DatabaseHelper.dateTimeFormatter.parse(nextDateString);
                } else {
                    date = DatabaseHelper.dateFormatter.parse(nextDateString);
                    quiet = true;
                }
                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

                Intent alarmIntent = new Intent(this, GoDoReceiver.class);
                alarmIntent.putExtra("max_notify", quiet ? 1 : 4);
                PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                manager.set(AlarmManager.RTC_WAKEUP, date.getTime(), contentIntent);
            }
        } catch (ParseException ignored) {
            //if we can't parse the date, give up.
            Log.e("GoDo", "Parse error parsing next date");
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.cancelAll();

        if (max == 0) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Cursor c = res.query(GoDoContentProvider.INSTANCES_URI,
                new String[]{
                        InstancesView.COLUMN_TASK_NAME, InstancesView.COLUMN_TASK_NOTES,
                        InstancesView.COLUMN_INSTANCE_NOTES,
                        "max(0, " +
                                "	(case when (length(due_date) > 10 and due_date <= DATETIME('now', 'localtime')) then due_notification else 0 end), " +
                                "   (case when (NOT blocked_by_context AND NOT blocked_by_task AND COALESCE(plan_date, start_date, '') <= DATETIME('now', 'localtime')) then notification else 0 end)) " +
                                "as notification",
                        InstancesView.COLUMN_ID
                },
                "NOT task_name IS NULL " +
                        "AND done_date IS NULL " +
                        "AND ((NOT blocked_by_context AND NOT blocked_by_task " +
                        "AND COALESCE(plan_date, start_date, '') <= DATETIME('now', 'localtime') " +
                        "AND notification > 0" +
                        "     ) OR " +
                        "(length(due_date) > 10 and due_date <= DATETIME('now', 'localtime') AND due_notification > 0)" +
                        "    )",
                null,
                "case when due_date <= DATETIME('now', 'localtime') then due_date || ' 23:59:59' else '9999-99-99' end, " +
                        "coalesce(plan_date || ' 23:59:59', DATETIME('now', 'localtime')), due_date || ' 23:59:59' , notification DESC, random()"
        );

        int numToNotify = 0;
        boolean audible = false;
        boolean vibrate = false;
        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        String name;
        String taskNotes;
        String instanceNotes;
        final ArrayList<String> spoken = new ArrayList<>();
        long id;
        if (c != null) {
            c.moveToFirst();
            int total = c.getCount();
            while (!c.isAfterLast()) {
                SpannableStringBuilder sb = new SpannableStringBuilder();
                numToNotify++;
                name = c.getString(0);
                if (!TextUtils.isEmpty(name)) {
                    sb.append(name);
                    /*
                    sb.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                            name.length(), 0);
                            */
                    sb.setSpan(new StyleSpan(Typeface.BOLD), 0,
                            name.length(), 0);
                    taskNotes = c.getString(1);
                    if (!TextUtils.isEmpty(taskNotes)) {
                        sb.append(' ');
                        sb.append(taskNotes);
                    }
                    instanceNotes = c.getString(2);
                    if (!TextUtils.isEmpty(instanceNotes)) {
                        sb.append(' ');
                        sb.append(instanceNotes);
                    }
                    inbox.addLine(sb);

                    switch (NotificationLevels.values()[Math.min(c.getInt(3), max)]) {
                        case SPOKEN:
                            spoken.add(name);
                            break;
                        case NOISY:
                            audible = true;
                            //fall through
                        case VIBRATE:
                            vibrate = true;
                            break;
                        default:
                            break;
                    }
                    id = c.getLong(4);

                    NotificationCompat.Builder builder = makeBuilder(audible, vibrate);
                    populateBuilder(builder, id, name, taskNotes,
                            instanceNotes);
                    if (total != 1) {
                        builder.setGroup(GROUP_KEY);
                        builder.setSortKey(String.format("%03d", numToNotify));
                    }
                    nm.notify((int)id, builder.build());


                }
                c.moveToNext();
            }
            c.close();
        }

        if (numToNotify > 1) {

            NotificationCompat.Builder builder = makeBuilder(audible, vibrate);

            builder.setContentTitle(numToNotify + " tasks")
                    .setContentText("GoDo")
                    .setTicker(numToNotify + " tasks")
                    .setNumber(numToNotify)

                    .setContentIntent(
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    new Intent(this, MainActivity.class),
                                    PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    .setStyle(inbox)
                    .setGroup(GROUP_KEY)
                    .setGroupSummary(true);

            nm.notify("Tasks", 0, builder.build());
        }
        if (numToNotify > 0){
            if (spoken.isEmpty()) {
                stopSelf();
            } else {
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @SuppressWarnings ("ConstantConditions")
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            for (int i = 0; i < spoken.size() - 1; i++) {
                                tts.speak(spoken.get(i), TextToSpeech.QUEUE_ADD, null);
                            }
                            HashMap<String, String> params = new HashMap<>(1);
                            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Last");
                            tts.speak(spoken.get(spoken.size() - 1), TextToSpeech.QUEUE_ADD, params);
                        } else {
                            Log.e("GoDo", "Error " + status);
                        }
                    }
                });
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                    @Override
                    public void onDone(String utteranceId) {
                        if ("Last".equals(utteranceId)) {
                            tts.shutdown();
                            tts = null;
                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if ("Last".equals(utteranceId)) {
                            tts.shutdown();
                            tts = null;
                            stopSelf();
                        }
                        Log.e("GoDo", "Error with " + utteranceId);
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

    @Nullable
    private String nextDate(@NonNull ContentResolver res, String column) {
        Cursor c = res.query(GoDoContentProvider.INSTANCES_URI,
                new String[]{column},
                column + " > datetime('now', 'localtime') AND done_date is null",
                null, column + " LIMIT 1");
        if (c == null) {
            return null;
        }
        c.moveToFirst();
        String nextDate = null;
        if (!c.isAfterLast())
            nextDate = c.getString(0);
        c.close();
        return nextDate;
    }


    /**
     * Returns the lesser (alphabetically earlier) of the two strings. If either is
     * null, the other is returned. If both are null, null is returned (duh).
     *
     * @param s1 first string to compare
     * @param s2 second string to compare
     * @return whichever string is first alphabetically
     */
    @Nullable
    private String stringMin(@Nullable String s1, @Nullable String s2) {
        if (s1 == null)
            return s2;
        if (s2 == null)
            return s1;
        return s1.compareTo(s2) < 0 ? s1 : s2;

    }

    private void populateBuilder(NotificationCompat.Builder builder, long id,
                                 CharSequence name, String taskNotes,
                                 String instanceNotes){

    StringBuilder sb = new StringBuilder();
    if (!TextUtils.isEmpty(taskNotes)) {
        sb.append(taskNotes);
    }
    if (!TextUtils.isEmpty(instanceNotes)) {
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append(instanceNotes);

    }
    builder.setContentTitle(name)
            .setContentText(sb)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(sb))
            .setTicker(name);
    PendingIntent markDone = PendingIntent.getBroadcast(this, (int)id,
            new Intent(this, GoDoReceiver.class).setAction(GoDoReceiver.MARK_COMPLETE_INTENT)
                    .putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id),
            PendingIntent.FLAG_UPDATE_CURRENT
    );
    builder.addAction(R.drawable.ic_action_accept, getString(R.string.mark_complete), markDone);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    //stackBuilder.addParentStack(TaskActivity.class);
    stackBuilder.addNextIntentWithParentStack(
            new Intent(this, TaskActivity.class).putExtra(InstanceHolderActivity.EXTRA_INSTANCE, id));
    builder.setContentIntent(stackBuilder.getPendingIntent((int)id,
            PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private NotificationCompat.Builder makeBuilder(boolean audible, boolean vibrate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_tasks)
                .setContentTitle("GoDo")
                .setAutoCancel(true);

        if (audible)
                builder.setDefaults(Notification.DEFAULT_SOUND);
        if (vibrate)
                //noinspection MagicNumber
                builder.setVibrate(new long[]{0L, 1000L, 300L, 1000L});
        builder.setLights(Color.GREEN, 1000, 1000);
        builder.setAutoCancel(true);
        return builder;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
