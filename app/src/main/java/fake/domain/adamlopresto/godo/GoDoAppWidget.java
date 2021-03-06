package fake.domain.adamlopresto.godo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class GoDoAppWidget extends AppWidgetProvider {

    private static void updateAppWidget(@NonNull Context context,
                                        @NonNull AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views;
        views = new RemoteViews(context.getPackageName(),
                prefs.getBoolean(SettingsActivity.PREF_COLORFUL_WIDGET, true) ?
                R.layout.app_widget_colorful : R.layout.app_widget);
        views.setRemoteAdapter(android.R.id.list, new Intent(context, GoDoWidgetService.class));
        views.setOnClickPendingIntent(R.id.text,
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        TaskStackBuilder stackBuilder = getStackBuilder(context);
        views.setPendingIntentTemplate(android.R.id.list, stackBuilder.getPendingIntent(-1, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.action_new_task,
                stackBuilder.getPendingIntent(-1, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent recognizer = getSpeechRecognizerIntent(context, stackBuilder);
        views.setOnClickPendingIntent(R.id.action_new_task_voice,
                PendingIntent.getActivity(context, 0, recognizer, PendingIntent.FLAG_UPDATE_CURRENT));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @NonNull
    public static Intent getSpeechRecognizerIntent(@NonNull Context context, TaskStackBuilder stackBuilder) {
        PendingIntent newFromVoicePendingIntent = stackBuilder.getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent recognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        recognizer.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getText(R.string.task_name_hint));
        recognizer.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT,
                newFromVoicePendingIntent);
        return recognizer;
    }

    @NonNull
    public static TaskStackBuilder getStackBuilder(@NonNull Context context) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(TaskActivity.class);
        stackBuilder.addNextIntentWithParentStack(
                new Intent(context, TaskActivity.class));
        return stackBuilder;
    }

    public static void updateAllAppWidgets(@NonNull Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        if (man == null) {
            return;
        }
        ComponentName widget = new ComponentName(context, GoDoAppWidget.class);
        man.notifyAppWidgetViewDataChanged(man.getAppWidgetIds(widget), android.R.id.list);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager,
                         @NonNull int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.e("GoDo", "GoDoAppWidget.onUpdate, " + appWidgetIds.length);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
