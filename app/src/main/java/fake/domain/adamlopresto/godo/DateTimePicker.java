package fake.domain.adamlopresto.godo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;


public class DateTimePicker extends LinearLayout {

    private static final Comparator<DateHolder> comparator = new Comparator<DateHolder>() {
        @SuppressWarnings ("VariableNotUsedInsideIf")
        @Override
        public int compare(DateHolder lhs, DateHolder rhs) {
            if (lhs == null)
                return rhs == null ? 0 : 1;
            if (rhs == null)
                return -1;

            if (lhs.date == null)
                return rhs.date == null ? 0 : 1;
            if (rhs.date == null)
                return -1;

            if (lhs.isOther)
                return rhs.isOther ? 0 : 1;
            if (rhs.isOther)
                return -1;

            return lhs.date.compareTo(rhs.date);
        }
    };
    @Nullable
    private Date date;
    private RepetitionRuleColumns column;
    private Spinner dateSpinner;
    private Button timeButton;
    private OnDateChangeListener listener;
    private ArrayAdapter<DateHolder> adapter;
    private boolean hasTime;

    @SuppressWarnings ("UnusedDeclaration")
    public DateTimePicker(Context context) {
        super(context);
        init(context);
    }

    @SuppressWarnings ("UnusedDeclaration")
    public DateTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressWarnings ("UnusedDeclaration")
    public DateTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private static void addIfNotFound(Collection<DateHolder> collection, DateHolder newItem) {
        if (!collection.contains(newItem))
            collection.add(newItem);
    }

    private void init(final Context context) {

        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_time_spinner, this, true);

        dateSpinner = (Spinner) getChildAt(0);
        timeButton = (Button) getChildAt(1);

        timeButton.setText("No time");

        if (isInEditMode()) {
            return;
        }

        adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(adapter);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DateHolder holder = (DateHolder) (parent.getItemAtPosition(position));
                assert holder != null;
                if (holder.isOther) {
                    final Calendar cal = Calendar.getInstance();
                    if (date != null && !Utils.SOMEDAY.equals(date))
                        cal.setTime(date);

                    DatePickerDialog dlg = new DatePickerDialog(context, null,
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    final DatePicker dp = dlg.getDatePicker();
                    assert dp != null;
                    dp.setSpinnersShown(false);
                    dp.setCalendarViewShown(true);
                    dlg.setTitle(null);
                    CalendarView cv = dp.getCalendarView();
                    if (cv != null) cv.setShowWeekNumber(false);
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Set", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //noinspection MagicConstant
                            cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                            listener.onDateChanged(cal.getTime(), hasTime, column);
                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dateSpinner.setSelection(adapter.getPosition(new DateHolder(date)));
                        }
                    });
                    dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "None", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            date = null;
                            hasTime = false;
                            listener.onDateChanged(null, false, column);
                        }
                    });
                    dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    dlg.show();
                } else {
                    if (date == null || holder.date == null || Utils.SOMEDAY.equals(holder.date)) {
                        date = holder.date;
                        hasTime = false;
                    } else {
                        //change date without changing time
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(date);
                        Calendar newCal = new GregorianCalendar();
                        newCal.setTime(holder.date);
                        //noinspection MagicConstant
                        cal.set(newCal.get(Calendar.YEAR), newCal.get(Calendar.MONTH), newCal.get(Calendar.DAY_OF_MONTH));
                        date = cal.getTime();
                    }

                    if (listener != null)
                        listener.onDateChanged(date, hasTime, column);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        timeButton.setOnClickListener(new OnClickListener() {
            boolean confirm;

            @Override
            public void onClick(View v) {
                confirm = true;
                final Calendar cal = new GregorianCalendar();
                if (date != null)
                    cal.setTime(date);

                if (!hasTime) {
                    //Hard coded to Carrie's preference.
                    cal.set(Calendar.HOUR_OF_DAY, 8);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                }

                TimePickerDialog dlg = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                if (confirm) {
                                    cal.set(Calendar.HOUR_OF_DAY, hour);
                                    cal.set(Calendar.MINUTE, minute);
                                    date = cal.getTime();
                                    hasTime = true;
                                    if (listener != null)
                                        listener.onDateChanged(date, true, column);
                                }
                            }
                        },
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
                );

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Set", (DialogInterface.OnClickListener) null);
                }
                dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirm = false;
                    }
                });
                dlg.setButton(DialogInterface.BUTTON_NEUTRAL, "None", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirm = false;
                        hasTime = false;
                    }
                });
                dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (listener != null)
                            listener.onDateChanged(date, hasTime, column);
                        timeButton.setText(hasTime ? Utils.formatShortTime(date) : "No time");
                    }
                });
                dlg.show();
            }
        });
    }

    @Override
    public int getBaseline() {
        return dateSpinner.getTop() + dateSpinner.getBaseline();
    }

    public void setColumn(RepetitionRuleColumns column) {
        this.column = column;
        init();
    }

    public void setDate(Date date, boolean hasTime) {
        this.date = date;
        this.hasTime = hasTime;
        init();
    }

    private void init() {
        Collection<DateHolder> list = new ArrayList<>(8);

        Calendar cal = new GregorianCalendar();
        //today
        if (column != RepetitionRuleColumns.NEW_START)
            addIfNotFound(list, new DateHolder(cal));

        //tomorrow
        cal.add(Calendar.DATE, 1);
        addIfNotFound(list, new DateHolder(cal));

        //Next week
        cal.add(Calendar.DATE, 6);
        addIfNotFound(list, new DateHolder(cal));

        //Saturday
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        addIfNotFound(list, new DateHolder(cal));

        //Monday
        cal.setTime(now);
        /*
        Sunday = 1, add 1 or *
        Monday = 2, add 0 or 7 or *
        Tuesday = 3, add 6
        Wed = 4, add 5
        Thu = 5, add 4
        Fri = 6, add 3
        Sat = 7, add 2
        */

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek >= Calendar.TUESDAY) {
            cal.add(Calendar.DATE, 7 - dayOfWeek + Calendar.MONDAY);
            addIfNotFound(list, new DateHolder(cal));
        }

        //Someday
        if (column != RepetitionRuleColumns.NEW_START)
            addIfNotFound(list, new DateHolder(Utils.SOMEDAY));

        //Other
        addIfNotFound(list, new DateHolder(true));

        //None
        addIfNotFound(list, new DateHolder(false));

        DateHolder selectedDate = new DateHolder(date);

        //current value
        //noinspection VariableNotUsedInsideIf
        if (date != null)
            addIfNotFound(list, selectedDate);

        adapter.clear();
        adapter.addAll(list);
        adapter.sort(comparator);

        dateSpinner.setSelection(adapter.getPosition(selectedDate));

        if (date == null || Utils.SOMEDAY.equals(date))
            timeButton.setVisibility(GONE);
        else {
            timeButton.setVisibility(VISIBLE);
            timeButton.setText(hasTime ? Utils.formatShortTime(date) : "No time");
        }
    }

    public void setOnDateDateChangeListener(OnDateChangeListener listener) {
        this.listener = listener;
    }

    public static interface OnDateChangeListener {
        public void onDateChanged(Date newDate, boolean hasTime, RepetitionRuleColumns column);
    }


    private static class DateHolder {
        public final boolean isOther;
        @Nullable
        public final Date date;

        public DateHolder(boolean isOther) {
            this.isOther = isOther;
            date = null;
        }

        public DateHolder(Calendar calendar) {
            Calendar localCal = new GregorianCalendar(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            date = localCal.getTime();
            isOther = false;
        }

        public DateHolder(@Nullable Date date) {
            isOther = false;
            if (date == null) {
                this.date = null;
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                Calendar localCal = new GregorianCalendar(cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                this.date = localCal.getTime();
            }
        }

        public String toString() {
            if (isOther) return "Other...";
            if (date == null) return "None";
            return Utils.formatLongRelativeDate(date);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DateHolder that = (DateHolder) o;

            return (isOther == that.isOther) &&
                    ((date == null) ? (that.date == null) : date.equals(that.date));

        }

        @Override
        public int hashCode() {
            int result = (isOther ? 1 : 0);
            result = 31 * result + (date != null ? date.hashCode() : 0);
            return result;
        }
    }
}
