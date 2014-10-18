package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import fake.domain.adamlopresto.godo.db.InstancesView;

public class GoDoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int ID = 0;
    private static final int TASK_NAME = 1;
    private static final int DUE_DATE = 2;
    private static final int PLAN_DATE = 3;
    @NonNull
    private final Context context;
    @Nullable
    private Cursor cursor;

    GoDoViewsFactory(@NonNull Context context) {
        Log.e("GoDo", "Creating new factory");
        this.context = context;
    }

    @Override
    public int getCount() {
        Log.e("GoDo", "getCount()");
        getCursor();
        assert cursor != null;
        Log.e("GoDo", "count is " + cursor.getCount());
        return cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        Log.e("GoDo", "getItemId(" + position + ")");
        getCursor();
        assert cursor != null;
        cursor.moveToPosition(position);
        return cursor.getLong(ID);
    }

    @Nullable
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @NonNull
    @Override
    public RemoteViews getViewAt(int position) {
        Log.e("GoDo", "getViewAt(" + position + ")");
        getCursor();
        assert cursor != null;
        cursor.moveToPosition(position);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_item);
        rv.setTextViewText(android.R.id.text1, cursor.getString(TASK_NAME));
        rv.setTextColor(android.R.id.text1,
                Utils.isBeforeNow(cursor.getString(DUE_DATE)) ? Color.RED :
                Utils.isAfterNow(cursor.getString(PLAN_DATE)) ? Color.GRAY :
                                                                Color.WHITE);
        Bundle extras = new Bundle();
        extras.putLong(InstanceHolderActivity.EXTRA_INSTANCE, cursor.getLong(ID));
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
        return false;
    }

    @Override
    public void onCreate() {
        Log.e("GoDo", "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.e("GoDo", "onDataSetChanged");
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        getCursor();
    }

    //We've had problems with getCount() having NPE for cursor.
    //Guarantee that there's an open cursor to work with.
    //TODO This probably isn't the right way to proceed.
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
        Log.e("GoDo", "cleanup");
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        cursor = null;
    }

    @Override
    public void onDestroy() {
        Log.e("GoDo", "onDestroy");
        cleanup();
    }
}
