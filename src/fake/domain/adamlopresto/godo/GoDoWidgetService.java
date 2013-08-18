package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstancesView;

public class GoDoWidgetService extends RemoteViewsService {
	public GoDoWidgetService() {
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new GoDoViewsFactory(this.getApplicationContext());
	}
}

class GoDoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	Cursor cursor;
	DatabaseHelper helper;
	SQLiteDatabase db;
	Context context;
	
	public final static int ID = 0;
	public final static int TASK_NAME = 1;
	public final static int DUE_DATE = 2;
	public final static int PLAN_DATE = 3;
	
	GoDoViewsFactory(Context context){
		helper = DatabaseHelper.getInstance(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		getCursor();
		return cursor.getCount();
	}

	@Override
	public long getItemId(int position) {
		getCursor();
		cursor.moveToPosition(position);
		return cursor.getLong(0);
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		getCursor();
		cursor.moveToPosition(position);
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_item);
		rv.setTextViewText(android.R.id.text1, cursor.getString(TASK_NAME));
		if (DateCalc.isBeforeNow(cursor.getString(DUE_DATE)))
			rv.setTextColor(android.R.id.text1, Color.RED);
		else if (DateCalc.isAfterNow(cursor.getString(PLAN_DATE)))
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
		if (db == null || !db.isOpen())
			db = helper.getWritableDatabase();
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		getCursor();
	}
	
	//We've had problems with getCount() erroring out. Guarantee that there's an open cursor to work with.
	private void getCursor(){
		if (cursor == null || cursor.isClosed()){
			if (db == null || !db.isOpen()){
				if (helper == null)
					helper = DatabaseHelper.getInstance(context);
				db = helper.getReadableDatabase();
			}
				
			String where = "((((NOT blocked_by_context) " +
					"          AND (NOT blocked_by_task)) " +
					"         AND (coalesce(start_date, 0) <= DATETIME('now', 'localtime')))" +
					"        OR (length(due_date) > 10 and due_date <= DATETIME('now', 'localtime')))" +
					"       AND (done_date IS NULL)"+
					"       AND (task_name IS NOT NULL)";
	
			cursor = db.query(InstancesView.VIEW, new String[]{
					InstancesView.COLUMN_ID, InstancesView.COLUMN_TASK_NAME, 
					InstancesView.COLUMN_DUE_DATE, InstancesView.COLUMN_PLAN_DATE
			}, where, null, null, null, 
			"case when due_date <= DATETIME('now', 'localtime') then due_date || ' 23:59:59' else '9999-99-99' end, "
			+"coalesce(plan_date || ' 23:59:59', DATETIME('now', 'localtime')), due_date || ' 23:59:59', "
			+"notification DESC, random()"
			);
		}
	}
	
	private void cleanup(){
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		cursor = null;
		if (db != null && db.isOpen())
			db.close();
		db = null;
	}

	@Override
	public void onDestroy() {
		cleanup();
	}
}