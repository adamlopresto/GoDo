package fake.domain.adamlopresto.godo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;
import fake.domain.adamlopresto.godo.db.TasksTable;

public class GoDoContentProvider extends ContentProvider {

    private static final String AUTHORITY = "fake.domain.adamlopresto.godo.contentprovider";
    private static final Uri BASE = Uri.parse("content://" + AUTHORITY);
    // Used for the UriMatcher
    // Odd numbers have an ID, evens don't.
    private static final int INSTANCES = 0;
    private static final int INSTANCE_ID = 1;
    private static final int CONTEXTS = 2;
    private static final int TOGGLE_CONTEXT = 4;
    private static final int TASKS = 6;
    private static final int TASK_ID = 7;
    private static final int REPETITION_RULES = 8;
    private static final int REPETITION_RULE_ID = 9;
    private static final int DEPENDENCIES = 10;
    private static final int DEPENDENCY_ID = 11;
    private static final int INSTANCES_SEARCH = 12;
    private static final int DEPENDENT_INSTANCES = 14;
    private static final int TEMPLATES = 16;

    private static final String INSTANCE_BASE_PATH = "instances";
    public static final Uri INSTANCES_URI = Uri.withAppendedPath(BASE, INSTANCE_BASE_PATH);
    private static final String INSTANCE_SEARCH_PATH = "instances/search";
    public static final Uri INSTANCE_SEARCH_URI = Uri.withAppendedPath(BASE, INSTANCE_SEARCH_PATH);
    private static final String CONTEXTS_BASE_PATH = "contexts";
    public static final Uri CONTEXTS_URI = Uri.withAppendedPath(BASE, CONTEXTS_BASE_PATH);
    private static final String TOGGLE_CONTEXT_PATH = "contexts/toggle";
    public static final Uri TOGGLE_CONTEXT_URI = Uri.withAppendedPath(BASE, TOGGLE_CONTEXT_PATH);
    private static final String TASK_BASE_PATH = "tasks";
    public static final Uri TASKS_URI = Uri.withAppendedPath(BASE, TASK_BASE_PATH);
    private static final String REPETITION_RULES_BASE_PATH = "repetition_rules";
    public static final Uri REPETITION_RULES_URI = Uri.withAppendedPath(BASE, REPETITION_RULES_BASE_PATH);
    private static final String DEPENDENCY_BASE_PATH = "dependencies";
    public static final Uri DEPENDENCY_URI = Uri.withAppendedPath(BASE, DEPENDENCY_BASE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String DEPENDANT_INSTANCES_PATH = "dependent_instances";
    public static final Uri DEPENDANT_INSTANCES_URI = Uri.withAppendedPath(BASE, DEPENDANT_INSTANCES_PATH);
    private static final String TEMPLATES_PATH = "templates";
    public static final Uri TEMPLATES_URI = Uri.withAppendedPath(BASE, TEMPLATES_PATH);

    /*
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/GoShopItems";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/GoShopItem";
     */
    static {
        sURIMatcher.addURI(AUTHORITY, INSTANCE_BASE_PATH, INSTANCES);
        sURIMatcher.addURI(AUTHORITY, INSTANCE_BASE_PATH + "/#", INSTANCE_ID);
        sURIMatcher.addURI(AUTHORITY, CONTEXTS_BASE_PATH, CONTEXTS);
        sURIMatcher.addURI(AUTHORITY, TOGGLE_CONTEXT_PATH, TOGGLE_CONTEXT);
        sURIMatcher.addURI(AUTHORITY, TASK_BASE_PATH, TASKS);
        sURIMatcher.addURI(AUTHORITY, TASK_BASE_PATH + "/#", TASK_ID);
        sURIMatcher.addURI(AUTHORITY, REPETITION_RULES_BASE_PATH, REPETITION_RULES);
        sURIMatcher.addURI(AUTHORITY, REPETITION_RULES_BASE_PATH + "/#", REPETITION_RULE_ID);
        sURIMatcher.addURI(AUTHORITY, DEPENDENCY_BASE_PATH, DEPENDENCIES);
        sURIMatcher.addURI(AUTHORITY, DEPENDENCY_BASE_PATH + "/#", DEPENDENCY_ID);
        sURIMatcher.addURI(AUTHORITY, INSTANCE_SEARCH_PATH, INSTANCES_SEARCH);
        sURIMatcher.addURI(AUTHORITY, DEPENDANT_INSTANCES_PATH+"/#", DEPENDENT_INSTANCES);
        sURIMatcher.addURI(AUTHORITY, TEMPLATES_PATH, TEMPLATES);
    }

    @NonNull
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private DatabaseHelper helper;

    private static String appendIdToSelection(String original) {
        return DatabaseUtils.concatenateWhere(original, "_id = ?");
    }

    @Nullable
    private static String[] appendSelectionArgs(@Nullable String[] originalValues, @Nullable String[] newValues) {
        if (originalValues == null) {
            return newValues;
        }
        if (newValues == null) {
            return originalValues;
        }
        return DatabaseUtils.appendSelectionArgs(originalValues, newValues);
    }

    @Nullable
    private static String[] appendSelectionArg(String[] originalValues, String newValue) {
        return appendSelectionArgs(originalValues, new String[]{newValue});
    }

    @Override
    public boolean onCreate() {
        helper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendIdToSelection(selection);
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case INSTANCES:
                queryBuilder.setTables(InstancesView.VIEW);
                break;
            case INSTANCES_SEARCH:
                //we're only including the most recently created instance for each task
                //see http://stackoverflow.com/a/123481/338660 for the algorithm used here
                queryBuilder.setTables(InstancesView.VIEW + " AS v1 " +
                                "LEFT OUTER JOIN " + InstancesView.VIEW + " AS v2 " +
                                " ON v1.task = v2.task AND v1." + InstancesView.COLUMN_CREATE_DATE + "<v2." +
                                InstancesView.COLUMN_CREATE_DATE
                );
                selection = DatabaseUtils.concatenateWhere(selection,
                        "v2." + InstancesView.COLUMN_CREATE_DATE + " IS NULL");
                projection = projection.clone();
                for (int i = 0; i < projection.length; i++) {
                    String s = projection[i];
                    projection[i] = "v1." + s + " AS " + s;
                }
                /*
                Map<String, String> map = new HashMap<>(projection.length);
                for (String s : projection) {
                    map.put(s, "v1." + s);
                }
                queryBuilder.setProjectionMap(map);
                */

                break;
            case CONTEXTS:
                queryBuilder.setTables(ContextsTable.TABLE);
                break;
            case REPETITION_RULES:
                queryBuilder.setTables(RepetitionRulesTable.TABLE);
                break;
            case DEPENDENCIES:
                queryBuilder.setTables(InstanceDependencyTable.TABLE);
                break;
            case TASKS:
                queryBuilder.setTables(TasksTable.TABLE);
                break;
            case DEPENDENT_INSTANCES:{
                String id = uri.getLastPathSegment();
                return helper.getReadableDatabase().rawQuery(
                        "SELECT _id, task_name, task_notes, instance_notes, due_date, plan_date, " +
                                "done_date, 0 as item_type " +
                        "FROM instances_view " +
                        "WHERE _id in (SELECT " + InstanceDependencyTable.COLUMN_FIRST +
                                     " FROM " + InstanceDependencyTable.TABLE +
                                     " WHERE " + InstanceDependencyTable.COLUMN_SECOND + " = ? )" +
                        "UNION "+
                        "SELECT -1 as _id, null as task_name, null as task_notes, " +
                                "null as instance_notes, null as due_date, null as plan_date, " +
                                "null as done_date, 1 as item_type " +
                        "UNION "+
                        "SELECT _id, task_name, task_notes, instance_notes, due_date, plan_date, " +
                                "done_date, 2 as item_type " +
                        "FROM instances_view " +
                        "WHERE _id in (SELECT " + InstanceDependencyTable.COLUMN_SECOND +
                        " FROM " + InstanceDependencyTable.TABLE +
                        " WHERE " + InstanceDependencyTable.COLUMN_FIRST + " = ? )" +
                        "ORDER BY item_type ASC",
                        new String[]{id, id});
            }
            case TEMPLATES:
                return helper.getReadableDatabase().rawQuery(
                        "select task, task_name from instances_view " +
                                "where repeat = 2 group by task " +
                                "order by max(create_date) > max(done_date), max(create_date) desc",
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri+" of type "+uriType);
        }

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        if (cursor != null) {
            Context context = getContext();
            if (context != null) {
                cursor.setNotificationUri(context.getContentResolver(), uri);
            }
        }

        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        int rowsUpdated;

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendIdToSelection(selection);
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case TASKS:
                rowsUpdated = sqlDB.delete(TasksTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    GoDoAppWidget.updateAllAppWidgets(getContext());
                    helper.notifyChange(TASKS_URI);
                }
                return rowsUpdated;
            case INSTANCES:
                rowsUpdated = sqlDB.delete(InstancesTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    GoDoAppWidget.updateAllAppWidgets(getContext());
                    helper.notifyChange(INSTANCES_URI);
                }
                return rowsUpdated;
            case CONTEXTS:
                rowsUpdated = sqlDB.delete(ContextsTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(CONTEXTS_URI);
                }
                return rowsUpdated;
            case REPETITION_RULES:
                rowsUpdated = sqlDB.delete(RepetitionRulesTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(REPETITION_RULES_URI);
                }
                return rowsUpdated;
            case DEPENDENCIES:
                rowsUpdated = sqlDB.delete(InstanceDependencyTable.TABLE, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(DEPENDENCY_URI);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        long id;
        switch (uriType) {
            case INSTANCES:
                id = sqlDB.insertOrThrow(InstancesTable.TABLE, null, values);
                GoDoAppWidget.updateAllAppWidgets(getContext());
                break;
            case TASKS:
                id = sqlDB.insertOrThrow(TasksTable.TABLE, null, values);
                break;
            case CONTEXTS:
                id = sqlDB.insertOrThrow(ContextsTable.TABLE, null, values);
                break;
            case REPETITION_RULES:
                id = sqlDB.insertOrThrow(RepetitionRulesTable.TABLE, null, values);
                break;
            case DEPENDENCIES:
                id = sqlDB.insertOrThrow(InstanceDependencyTable.TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        helper.notifyChange(uri);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        int rowsUpdated;

        //If it's odd, then it has an ID appended.
        if ((uriType % 2) == 1) {
            String id = uri.getLastPathSegment();
            selection = appendIdToSelection(selection);
            selectionArgs = appendSelectionArg(selectionArgs, id);
            uriType--;
        }

        switch (uriType) {
            case TOGGLE_CONTEXT:
                sqlDB.execSQL("UPDATE " + ContextsTable.TABLE + " SET " + ContextsTable.COLUMN_ACTIVE + "= NOT " + ContextsTable.COLUMN_ACTIVE + " WHERE " + selection, selectionArgs);
                GoDoAppWidget.updateAllAppWidgets(getContext());
                helper.notifyChange(CONTEXTS_URI);
                return 1;
            case TASKS:
                rowsUpdated = sqlDB.update(TasksTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    GoDoAppWidget.updateAllAppWidgets(getContext());
                    helper.notifyChange(TASKS_URI);
                }
                return rowsUpdated;
            case INSTANCES:
                rowsUpdated = sqlDB.update(InstancesTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    GoDoAppWidget.updateAllAppWidgets(getContext());
                    helper.notifyChange(INSTANCES_URI);
                }
                return rowsUpdated;
            case CONTEXTS:
                rowsUpdated = sqlDB.update(ContextsTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    GoDoAppWidget.updateAllAppWidgets(getContext());
                    helper.notifyChange(CONTEXTS_URI);
                }
                return rowsUpdated;
            case REPETITION_RULES:
                rowsUpdated = sqlDB.update(RepetitionRulesTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(REPETITION_RULES_URI);
                }
                return rowsUpdated;
            case DEPENDENCIES:
                rowsUpdated = sqlDB.update(InstanceDependencyTable.TABLE, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    helper.notifyChange(DEPENDENCY_URI);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }


}
