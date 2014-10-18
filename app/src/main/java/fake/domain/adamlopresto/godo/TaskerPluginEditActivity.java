package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;

public class TaskerPluginEditActivity extends Activity {

    private final ArrayList<String> activate = new ArrayList<>();
    private final ArrayList<String> deactivate = new ArrayList<>();
    private TextView activate_view;
    private TextView deactivate_view;
    private Spinner maxNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_plugin_edit);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);


        findViewById(R.id.activate_button).setOnClickListener(new EditButtonClickListener(activate));
        findViewById(R.id.deactivate_button).setOnClickListener(new EditButtonClickListener(deactivate));

        activate_view = (TextView) findViewById(R.id.activate);
        deactivate_view = (TextView) findViewById(R.id.deactivate);
        maxNotify = (Spinner) findViewById(R.id.max_notify);

        maxNotify.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                update();
            }

        });

        Bundle extras;
        Intent i = getIntent();
        if (i != null && (extras = i.getExtras()) != null
                && (extras = extras.getBundle("com.twofortyfouram.locale.intent.extra.BUNDLE")) != null) {
            String[] temp = extras.getStringArray("activate");
            if (temp != null)
                activate.addAll(Arrays.asList(temp));

            temp = extras.getStringArray("deactivate");
            if (temp != null)
                deactivate.addAll(Arrays.asList(temp));

            maxNotify.setSelection(extras.getInt("max_notify", 4));
        } else {
            maxNotify.setSelection(4);
        }
        update();
    }

    @SuppressLint("DefaultLocale")
    private void update() {

        Bundle b = new Bundle();

        StringBuilder builder = new StringBuilder();
        String states;
        if (activate.isEmpty()) {
            activate_view.setText(R.string.none);
        } else {
            states = TextUtils.join(", ", activate);
            builder.append("On: ");
            builder.append(states);
            activate_view.setText(states);
            b.putStringArray("activate", activate.toArray(new String[1]));
        }

        if (deactivate.isEmpty()) {
            deactivate_view.setText(R.string.none);
        } else {
            states = TextUtils.join(", ", deactivate);
            if (builder.length() > 0)
                builder.append('\n');

            builder.append("Off: ");
            builder.append(states);
            deactivate_view.setText(states);
            b.putStringArray("deactivate", deactivate.toArray(new String[1]));
        }

        int maxLevel = maxNotify.getSelectedItemPosition();
        builder.append("\nNotify: ");
        builder.append(NotificationLevels.values()[maxLevel].toString().toLowerCase());

        if (b.isEmpty() && maxLevel == 0) {
            setResult(RESULT_CANCELED);
        } else {
            b.putInt("max_notify", maxLevel);
            setResult(RESULT_OK, new Intent()
                    .putExtra("com.twofortyfouram.locale.intent.extra.BLURB", builder.toString())
                    .putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", b));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class EditButtonClickListener implements OnClickListener {

        private final ArrayList<String> list;

        public EditButtonClickListener(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public void onClick(View ignored) {
            SQLiteDatabase db = DatabaseHelper.getInstance(TaskerPluginEditActivity.this).getReadableDatabase();
            Cursor cursor = db.query(ContextsTable.TABLE, new String[]{ContextsTable.COLUMN_NAME
            }, null, null, null, null, null);

            int count = cursor.getCount();

            final String[] items = new String[count];
            boolean[] checkedItems = new boolean[count];

            final Collection<String> temp = new HashSet<>(list);

            cursor.moveToFirst();
            for (int i = 0; i < count; ++i) {
                items[i] = cursor.getString(0);
                checkedItems[i] = temp.contains(items[i]);
                cursor.moveToNext();
            }

            new AlertDialog.Builder(TaskerPluginEditActivity.this)
                    .setMultiChoiceItems(items, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if (isChecked) {
                                        temp.add(items[which]);
                                    } else {
                                        temp.remove(items[which]);
                                    }
                                }
                            }
                    )
                    .setTitle(R.string.title_contexts)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list.clear();
                            list.addAll(temp);
                            update();
                        }
                    })
                    .show();
        }
    }
}