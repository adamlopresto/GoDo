package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fake.domain.adamlopresto.godo.db.InstancesView;

public class GoDoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final int ID = 0;
    public static final int TASK_NAME = 1;
    public static final int DUE_DATE = 2;
    public static final int PLAN_DATE = 3;
    @NotNull
    final Context context;
    @Nullable
    Cursor cursor;

    GoDoViewsFactory(@NotNull Context context) {
        Log.e("GoDo", "Creating new factory");
        this.context = context;
    }

    @Override
    public int getCount() {
        Log.e("GoDo", "getCount()");
        getCursor();
        Log.e("GoDo", "count is " + cursor.getCount());
        return cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        Log.e("GoDo", "getItemId(" + position + ")");
        getCursor();
        cursor.moveToPosition(position);
        return cursor.getLong(ID);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.e("GoDo", "getViewAt(" + position + ")");
        getCursor();
        cursor.moveToPosition(position);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_item);
        rv.setTextViewText(android.R.id.text1, cursor.getString(TASK_NAME));
        if (Utils.isBeforeNow(cursor.getString(DUE_DATE)))
            rv.setTextColor(android.R.id.text1, Color.RED);
        else if (Utils.isAfterNow(cursor.getString(PLAN_DATE)))
            rv.setTextColor(android.R.id.text1, Color.GRAY);
        else
            rv.setTextColor(android.R.id.text1, Color.WHITE);

        Bundle extras = new Bundle();
        extras.putLong("instance", cursor.getLong(ID));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(android.R.id.text1, fillInIntent);

        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        if (cursor != null && !cursor.isClosed())
            cursor.close();

        getCursor();
    }

    //We've had problems with getCount() erroring out. Guarantee that there's an open cursor to work with.
    private void getCursor() {
        if (cursor == null || cursor.isClosed()) {
            Log.e("GoDo", "Opening cursor");
            String where = "((((NOT blocked_by_context) " +
                    "          AND (NOT blocked_by_task)) " +
                    "         AND (coalesce(start_date, 0) <= DATETIME('now', 'localtime')))" +
                    "        OR (length(due_date) > 10 and due_date <= DATETIME('now', 'localtime')))" +
                    "       AND (done_date IS NULL)" +
                    "       AND (task_name IS NOT NULL)";

            final long token = Binder.clearCallingIdentity();
            try {
                cursor = context.getContentResolver().query(GoDoContentProvider.INSTANCES_URI,
                        new String[]{
                                InstancesView.COLUMN_ID, InstancesView.COLUMN_TASK_NAME,
                                InstancesView.COLUMN_DUE_DATE, InstancesView.COLUMN_PLAN_DATE
                        }, where, null,
                        "case when due_date <= DATETIME('now', 'localtime') then due_date || ' 23:59:59' else '9999-99-99' end, "
                                + "coalesce(plan_date || ' 23:59:59', DATETIME('now', 'localtime')), due_date || ' 23:59:59', "
                                + "notification DESC, random()"
                );
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private void cleanup() {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        cursor = null;
    }

    @Override
    public void onDestroy() {
        cleanup();
    }
}