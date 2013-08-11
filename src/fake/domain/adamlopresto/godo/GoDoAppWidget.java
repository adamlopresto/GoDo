package fake.domain.adamlopresto.godo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class GoDoAppWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
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

	static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {

		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.app_widget);
		views.setRemoteAdapter(android.R.id.list, new Intent(context, GoDoWidgetService.class));
		views.setOnClickPendingIntent(R.id.text, 
				PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 
						PendingIntent.FLAG_UPDATE_CURRENT));
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		//stackBuilder.addParentStack(TaskActivity.class);
		stackBuilder.addNextIntentWithParentStack(
				new Intent(context, TaskActivity.class));
		views.setPendingIntentTemplate(android.R.id.list, stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT));
		views.setOnClickPendingIntent(R.id.action_new_task, 
				stackBuilder.getPendingIntent(2, PendingIntent.FLAG_UPDATE_CURRENT));

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	
	public static void updateAllAppWidgets(Context context) {
		AppWidgetManager man = AppWidgetManager.getInstance(context);
		ComponentName widget = new ComponentName(context, GoDoAppWidget.class);
		man.notifyAppWidgetViewDataChanged(man.getAppWidgetIds(widget), android.R.id.list);
	}
}
