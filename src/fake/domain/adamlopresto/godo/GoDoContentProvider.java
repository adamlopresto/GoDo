package fake.domain.adamlopresto.godo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import fake.domain.adamlopresto.godo.db.AvailableInstancesView;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class GoDoContentProvider extends ContentProvider {

	private DatabaseHelper helper;

	// Used for the UriMatcher
	private static final int AVAILABLE_INSTANCES = 1;

	public static final String AUTHORITY = "fake.domain.adamlopresto.godo.contentprovider";

	private static final String INSTANCE_BASE_PATH = "instances";
	public static final Uri INSTANCES_URI = Uri.parse("content://" + AUTHORITY + "/" + INSTANCE_BASE_PATH);
	
	private static final String AVAILABLE_INSTANCE_PATH = INSTANCE_BASE_PATH+"/available";
	public static final Uri AVAILABLE_INSTANCES_URI =Uri.parse("content://" + AUTHORITY + "/" 
			+ AVAILABLE_INSTANCE_PATH);
	
	
	/*
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/GoShopItems";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/GoShopItem";
	 */

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, AVAILABLE_INSTANCE_PATH, AVAILABLE_INSTANCES);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		/* TODO
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsDeleted = 0;
		String id;
		switch (uriType) {
		case ITEM_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(ItemsTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			//notify of this particular item
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case ITEMS:
			rowsDeleted = sqlDB.delete(ItemsTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0) {
				getContext().getContentResolver().notifyChange(ITEM_URI, null);
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			}
			break;
		case ITEM_AISLE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(ItemAisleTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case ITEM_AISLE:
			rowsDeleted = sqlDB.delete(ItemAisleTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0)
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			break;
		case STORE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(StoresTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case STORE:
			rowsDeleted = sqlDB.delete(StoresTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0){
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
				getContext().getContentResolver().notifyChange(STORES_URI, null);
			}
			break;
		case AISLE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(AislesTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case AISLE:
			rowsDeleted = sqlDB.delete(AislesTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0){
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
				getContext().getContentResolver().notifyChange(AISLES_URI, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		return rowsDeleted;
		*/
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		/* TODO

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case ITEMS:
			id = sqlDB.insertOrThrow(ItemsTable.TABLE, null, values);
			break;
		case ITEM_AISLE:
			id = sqlDB.insertOrThrow(ItemAisleTable.TABLE, null, values);
			break;
		case STORE:
			id = sqlDB.insertOrThrow(StoresTable.TABLE, null, values);
			break;
		case AISLE:
			id = sqlDB.insertOrThrow(AislesTable.TABLE, null, values);
			break;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(uri+"/"+id);
		*/
		return null;
	}

	@Override
	public boolean onCreate() {
		helper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case AVAILABLE_INSTANCES:
			queryBuilder.setTables(AvailableInstancesView.VIEW);
			break;
		/*	
		case ITEMS:
			// Adding the ID to the original query
			queryBuilder.setTables(ItemsTable.TABLE);
			break;
		case ITEM_AISLE_ID:
			queryBuilder.appendWhere(ItemAisleDetailView.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case ITEM_AISLE:
			queryBuilder.setTables(ItemAisleDetailView.VIEW);
			break;
		case STORE_ID:
			queryBuilder.appendWhere(StoresTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case STORE:
			queryBuilder.setTables(StoresTable.TABLE);
			break;
		case STORES_WITH_ALL:
			Cursor c = helper.getReadableDatabase().rawQuery(
					//"SELECT _id, list, store_name FROM (SELECT -1 AS _id, 0 AS list, 'All' AS store_name, 0 as sortfield UNION SELECT _id, list, store_name, 1 as sortfield FROM stores ORDER BY sortfield, store_name)", null);
					"SELECT _id, list, store_name FROM (SELECT -1 AS _id, 0 AS list, 'All' AS store_name, 0 as sortfield UNION SELECT store as _id, list, store_name, 1 as sortfield FROM item_aisle_detail WHERE status <> 'H' GROUP BY store HAVING count(item) > 0 ORDER BY sortfield, store_name)", null);
			c.setNotificationUri(getContext().getContentResolver(), ITEM_AISLE_URI);
			return c;
		case AISLE_ID:
			queryBuilder.appendWhere(AislesTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case AISLE:
			queryBuilder.setTables(AislesTable.TABLE);
			break;
			*/
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		/* 
		 * TODO
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsUpdated = 0;
		String id;
		switch (uriType) {
		case ITEMS:
			rowsUpdated = sqlDB.update(ItemsTable.TABLE, 
					values, 
					selection,
					selectionArgs);
			
			getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			break;
		case ITEM_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ItemsTable.TABLE, 
						values,
						ItemsTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(ItemsTable.TABLE, 
						values,
						ItemsTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			getContext().getContentResolver().notifyChange(ITEM_URI, null);
			break;
		case AISLE_ID:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(AislesTable.TABLE, 
						values,
						AislesTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(AislesTable.TABLE, 
						values,
						AislesTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
		*/
		return 0;
	}

	private static String[] appendSelectionArgs(String originalValues[], String newValues[]){
		if (originalValues == null){
			return newValues;
		}
		if (newValues == null){
			return originalValues;
		}
		return DatabaseUtils.appendSelectionArgs(originalValues, newValues);
	}

}
