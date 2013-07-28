package fake.domain.adamlopresto.godo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	
	GoDoViewsFactory(Context context){
		helper = DatabaseHelper.getInstance(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public long getItemId(int position) {
		return cursor.getLong(0);
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		cursor.moveToPosition(position);
		RemoteViews rv = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);
		rv.setTextViewText(android.R.id.text1, cursor.getString(1));
		
		Bundle extras = new Bundle();
        extras.putLong("instance", cursor.getLong(0));
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
			db = helper.getReadableDatabase();
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		cursor = db.query(InstancesView.VIEW, new String[]{
				InstancesView.COLUMN_ID, InstancesView.COLUMN_TASK_NAME
		}, null, null, null, null, null);
	}
	
	void cleanup(){
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