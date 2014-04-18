package fake.domain.adamlopresto.godo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class GoDoAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.app_widget);
        views.setRemoteAdapter(android.R.id.list, new Intent(context, GoDoWidgetService.class));
        views.setOnClickPendingIntent(R.id.text,
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(TaskActivity.class);
        stackBuilder.addNextIntentWithParentStack(
                new Intent(context, TaskActivity.class));
        views.setPendingIntentTemplate(android.R.id.list, stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.action_new_task,
                stackBuilder.getPendingIntent(2, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent recognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizer.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getText(R.string.task_name_hint));
        recognizer.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT,
                stackBuilder.getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.action_new_task_voice,
                PendingIntent.getActivity(context, 0, recognizer, PendingIntent.FLAG_UPDATE_CURRENT));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllAppWidgets(Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        if (man == null) {
            return;
        }
        ComponentName widget = new ComponentName(context, GoDoAppWidget.class);
        man.notifyAppWidgetViewDataChanged(man.getAppWidgetIds(widget), android.R.id.list);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
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
