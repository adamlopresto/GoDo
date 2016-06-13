package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Locale;

import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;

public class TaskRepetitionRuleActivity extends ActionBarActivity {

    private final CheckBox[] weekdays = new CheckBox[7];
    private Spinner to;
    private Spinner from;
    private Spinner direction;
    private Spinner ruleType;
    private TextView numberLabel;
    private EditText number;
    private LinearLayout weekdayLayout;
    private TimePicker timePicker;
    private boolean template = false;

    private boolean weekdaysHidden = true;

    private long task_id = -1L;
    private long rule_id = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_repetition_rule);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        to = (Spinner) findViewById(R.id.to);
        from = (Spinner) findViewById(R.id.from);
        direction = (Spinner) findViewById(R.id.direction);
        ruleType = (Spinner) findViewById(R.id.rule_type);
        numberLabel = (TextView) findViewById(R.id.number_label);
        number = (EditText) findViewById(R.id.number);
        weekdayLayout = (LinearLayout) findViewById(R.id.weekdays);
        weekdays[0] = (CheckBox) findViewById(R.id.sunday);
        weekdays[1] = (CheckBox) findViewById(R.id.monday);
        weekdays[2] = (CheckBox) findViewById(R.id.tuesday);
        weekdays[3] = (CheckBox) findViewById(R.id.wednesday);
        weekdays[4] = (CheckBox) findViewById(R.id.thursday);
        weekdays[5] = (CheckBox) findViewById(R.id.friday);
        weekdays[6] = (CheckBox) findViewById(R.id.saturday);
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        ruleType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                switch (RepetitionRuleTypes.values()[position]) {
                    case ADD_DAY:
                    case ADD_MONTH:
                    case ADD_WEEK:
                    case ADD_YEAR:
                        weekdayLayout.setVisibility(View.GONE);
                        numberLabel.setVisibility(View.VISIBLE);
                        number.setVisibility(View.VISIBLE);
                        timePicker.setVisibility(View.GONE);
                        showKeyboard(number);
                        weekdaysHidden = true;
                        break;
                    case WEEKDAY:
                        weekdayLayout.setVisibility(View.VISIBLE);
                        numberLabel.setVisibility(View.GONE);
                        number.setVisibility(View.GONE);
                        timePicker.setVisibility(View.GONE);
                        weekdaysHidden = false;
                        hideKeyboard(number);
                        break;
                    case SET_TIME:
                        weekdayLayout.setVisibility(View.GONE);
                        numberLabel.setVisibility(View.GONE);
                        number.setVisibility(View.GONE);
                        timePicker.setVisibility(View.VISIBLE);
                        weekdaysHidden = true;
                        hideKeyboard(number);
                        break;
                }
            }

            /**
             * Hides the currently displayed keyboard. Needs any view in the window
             * @param view some view in the current window
             */
            private void hideKeyboard(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            /**
             * Shows the soft keyboard.
             * @param view the view to focus and show input for
             */
            private void showKeyboard(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                view.requestFocusFromTouch();
                imm.showSoftInput(view, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        if (!extractFromBundle(savedInstanceState)) {
            if (!extractFromBundle(getIntent().getExtras())) {
                Toast.makeText(
                        this,
                        "Error: must have either an existing rule, or at least a task to bind to",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        if (template) {
            ArrayAdapter<CharSequence> ad = ArrayAdapter.createFromResource(this, R.array.from_columns_template,
                    android.R.layout.simple_spinner_item);
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            from.setAdapter(ad);
        }

        if (rule_id == -1L) {
            from.setSelection(3);
            number.setText("1");
        } else {
            Cursor c = getContentResolver().query(
                    GoDoContentProvider.REPETITION_RULES_URI,
                    new String[]{RepetitionRulesTable.COLUMN_ID,
                            RepetitionRulesTable.COLUMN_TASK,
                            RepetitionRulesTable.COLUMN_TYPE,
                            RepetitionRulesTable.COLUMN_SUBVALUE,
                            RepetitionRulesTable.COLUMN_FROM,
                            RepetitionRulesTable.COLUMN_TO},
                    RepetitionRulesTable.COLUMN_ID + "=?",
                    new String[]{String.valueOf(rule_id)}, null
            );

            if (c == null) {
                from.setSelection(3);
                number.setText("1");
                return;
            }
            c.moveToFirst();

            task_id = c.getLong(c.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TASK));
            to.setSelection(c.getInt(c
                    .getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TO)));
            from.setSelection(Math.min(template ? 3 : 6,
                    c.getInt(c.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_FROM))));
            int ruleTypeNumber = c.getInt(c
                    .getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TYPE));
            ruleType.setSelection(ruleTypeNumber);
            String subValue = c
                    .getString(c
                            .getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_SUBVALUE));
            c.close();
            if (subValue == null)
                subValue = "";
            if (subValue.startsWith("-")) {
                direction.setSelection(1);
                subValue = subValue.substring(1);
            } else {
                direction.setSelection(0);
            }
            switch (RepetitionRuleTypes.values()[ruleTypeNumber]) {
                case ADD_DAY:
                case ADD_MONTH:
                case ADD_WEEK:
                case ADD_YEAR:
                    number.setText(subValue);
                    break;
                case WEEKDAY:
                    for (String s : subValue.split(",")) {
                        switch (s) {
                            case "Su":
                                weekdays[0].setChecked(true);
                                break;
                            case "M":
                                weekdays[1].setChecked(true);
                                break;
                            case "Tu":
                                weekdays[2].setChecked(true);
                                break;
                            case "W":
                                weekdays[3].setChecked(true);
                                break;
                            case "Th":
                                weekdays[4].setChecked(true);
                                break;
                            case "F":
                                weekdays[5].setChecked(true);
                                break;
                            case "Sa":
                                weekdays[6].setChecked(true);
                                break;
                        }
                    }
                    break;
                case SET_TIME:
                    try {
                        String[] parts = subValue.split(":", 2);
                        int hr = Integer.valueOf(parts[0]);
                        int min = Integer.valueOf(parts[1]);
                        timePicker.setCurrentHour(hr);
                        timePicker.setCurrentMinute(min);
                    } catch (NumberFormatException e) {
                        Snackbar.make(null, "Error setting time: " + e, Snackbar.LENGTH_LONG)
                                .show();
                    }
                    break;
                default:
                    break;
            }
        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean extractFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        if ((rule_id = bundle.getLong("rule", -1L)) != -1L) {
            template = bundle.getBoolean("template", false);
            return true;
        }
        if ((task_id = bundle.getLong("task", -1L)) != -1L) {
            template = bundle.getBoolean("template", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_repetition_rule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        ContentValues values = new ContentValues();
        values.put(RepetitionRulesTable.COLUMN_TO, to.getSelectedItemPosition());
        values.put(RepetitionRulesTable.COLUMN_FROM,
                from.getSelectedItemPosition());
        int ruleTypeSelectedItemPosition = ruleType.getSelectedItemPosition();
        values.put(RepetitionRulesTable.COLUMN_TYPE,
                ruleTypeSelectedItemPosition);
        String subValue = "";
        switch (RepetitionRuleTypes.values()[ruleTypeSelectedItemPosition]) {
            case WEEKDAY:
                StringBuilder b = new StringBuilder();
                if (weekdays[0].isChecked())
                    b.append(",Su");
                if (weekdays[1].isChecked())
                    b.append(",M");
                if (weekdays[2].isChecked())
                    b.append(",Tu");
                if (weekdays[3].isChecked())
                    b.append(",W");
                if (weekdays[4].isChecked())
                    b.append(",Th");
                if (weekdays[5].isChecked())
                    b.append(",F");
                if (weekdays[6].isChecked())
                    b.append(",Sa");
                if (b.length() > 0)
                    subValue = b.substring(1);
                break;
            case SET_TIME:
                subValue = String.format(Locale.US, "%02d:%02d", timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                break;
            default:
                subValue = Utils.getString(number);
        }
        if (direction.getSelectedItemPosition() == 1) {
            subValue = "-" + subValue;
        }

        values.put(RepetitionRulesTable.COLUMN_SUBVALUE, subValue);
        if (rule_id == -1L) {
            values.put(RepetitionRulesTable.COLUMN_TASK, task_id);
            Uri newItem = getContentResolver().insert(
                    GoDoContentProvider.REPETITION_RULES_URI, values);
            if (newItem != null)
                rule_id = Long.valueOf(newItem.getLastPathSegment());
        } else {
            getContentResolver().update(
                    GoDoContentProvider.REPETITION_RULES_URI, values,
                    RepetitionRulesTable.COLUMN_ID + "=?",
                    new String[]{String.valueOf(rule_id)});
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveData();
        if (task_id != -1L)
            outState.putLong("task", task_id);
        if (rule_id != -1L)
            outState.putLong("rule", rule_id);
    }

}
