package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;


@SuppressLint ("SimpleDateFormat")
public final class Utils {

    //private static final long DECEMBER_31_2099_IN_MS = 64063198800000L;
    @SuppressWarnings ("MagicNumber")
    public static final Date SOMEDAY = new GregorianCalendar(2099, Calendar.DECEMBER, 31).getTime();
    //private static final DateFormat SHORT_TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final DateFormat SHORT_TIME = new SimpleDateFormat("h:mm\u00a0a");
    @SuppressWarnings ("SpellCheckingInspection")
    private static final DateFormat weekday = new SimpleDateFormat("EEEE");
    private static final DateFormat SHORT_DATE = new SimpleDateFormat("MMM d");
    private static final DateFormat SHORT_DATE_WITH_YEAR = new SimpleDateFormat("MMM d, yyyy");
    @SuppressWarnings ("SpellCheckingInspection")
    private static final DateFormat LONG_DATE = new SimpleDateFormat("MMMM d");
    @SuppressWarnings ("SpellCheckingInspection")
    private static final DateFormat LONG_DATE_WITH_YEAR = new SimpleDateFormat("MMMM d, yyyy");
    private static final DateFormat REALLY_SHORT_TIME = new SimpleDateFormat("h\u00a0a");

    private Utils() {
    }

    private static String formatShortRelativeDate(@Nullable Date then, boolean hasTime) {
        if (then == null)
            return "Null";
        if (then.equals(SOMEDAY))
            return "Someday";

        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Calendar thenCal = new GregorianCalendar();
        thenCal.setTime(then);
        thenCal.set(Calendar.HOUR_OF_DAY, 0);
        thenCal.set(Calendar.MINUTE, 0);
        thenCal.set(Calendar.SECOND, 0);
        thenCal.set(Calendar.MILLISECOND, 0);

        String date;

        // diff is the number of days from now to then. If then is the future, the result is positive.
        int diff = (int) ((thenCal.getTimeInMillis() - now.getTimeInMillis()) / DateUtils.DAY_IN_MILLIS);
        //noinspection IfStatementWithTooManyBranches
        if (diff == 0)
            date = "Today";
        else if (diff == -1)
            date = "Yesterday";
        else if (diff == 1)
            date = "Tomorrow";
        else if (diff > 0 && diff <= 7)
            date = weekday.format(then);
        else if (diff < 0 && diff >= -7)
            date = "Last " + weekday.format(then);
        else //noinspection IfMayBeConditional
            if (thenCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
                date = SHORT_DATE.format(then);
            else
                date = SHORT_DATE_WITH_YEAR.format(then);

        if (!hasTime)
            return date;

        thenCal.setTime(then);
        String time;
        time = thenCal.get(Calendar.MINUTE) == 0
               ? REALLY_SHORT_TIME.format(then)
               : SHORT_TIME.format(then);

        return diff == 0
               ? time
               : date + " " + time;
    }

    public static CharSequence formatShortTime(@Nullable Date time) {
        if (time == null)
            return "No time";
        return SHORT_TIME.format(time);
    }

    public static String formatLongRelativeDate(@Nullable Date then) {
        if (then == null)
            return "Null";

        if (then.equals(SOMEDAY))
            return "Someday";

        Calendar now = new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Calendar thenCal = new GregorianCalendar();
        thenCal.setTime(then);
        thenCal.set(Calendar.HOUR_OF_DAY, 0);
        thenCal.set(Calendar.MINUTE, 0);
        thenCal.set(Calendar.SECOND, 0);
        thenCal.set(Calendar.MILLISECOND, 0);

        // diff is the number of days from now to then. If then is the future, the result is positive.
        int diff = (int) ((thenCal.getTimeInMillis() - now.getTimeInMillis()) / DateUtils.DAY_IN_MILLIS);
        if (diff == 0)
            return "Today";
        if (diff == -1)
            return "Yesterday";
        if (diff == 1)
            return "Tomorrow";
        if (diff > 0 && diff <= 7)
            return weekday.format(then);
        if (diff < 0 && diff >= -7)
            return "Last " + weekday.format(then);
        if (thenCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
            return LONG_DATE.format(then);
        return LONG_DATE_WITH_YEAR.format(then);
    }

    public static String formatShortRelativeDate(@NonNull String formattedDate) {
        try {
            if (formattedDate.length() <= 10)
                return formatShortRelativeDate(DatabaseHelper.dateFormatter.parse(formattedDate), false);
            return formatShortRelativeDate(DatabaseHelper.dateTimeFormatter.parse(formattedDate), true);
        } catch (ParseException ignored) {
            return "Invalid date: " + formattedDate;
        }
    }

    /**
     * Returns true iff the formattedDate passed is unambiguously past. Returns false for null,
     * empty string, today (with no time), and any date tomorrow and onward, or any
     * datetime later today. @param formattedDate string formatted date, in local
     * time
     *
     * @return boolean described above
     */
    public static boolean isBeforeNow(@Nullable String formattedDate) {
        if (formattedDate == null)
            return false;
        if (formattedDate.length() > 10)
            return formattedDate.compareTo(DatabaseHelper.dateTimeFormatter.format(new Date())) < 0;
        return formattedDate.compareTo(DatabaseHelper.dateFormatter.format(new Date())) < 0;
    }

    /**
     * Returns true iff the formattedDate passed is unambiguously in the future.
     * Returns false for null, * empty string, today (with no time), and any date in the past.
     *
     * @param formattedDate string formatted date, in local time
     * @return boolean described above
     */
    public static boolean isAfterNow(@Nullable String formattedDate) {
        if (formattedDate == null)
            return false;
        if (formattedDate.length() > 10)
            return formattedDate.compareTo(DatabaseHelper.dateTimeFormatter.format(new Date())) > 0;
        return formattedDate.compareTo(DatabaseHelper.dateFormatter.format(new Date())) > 0;
    }

    @Nullable
    public static String getString(@NonNull TextView view) {
        CharSequence seq = view.getText();
        if (seq == null)
            return null;
        return seq.toString();
    }

    public static String repetitionRuleTextFromCursor(Cursor cursor, boolean template) {
        String to;
        switch (cursor.getInt(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TO))) {
            case 0:
                to = "Starts ";
                break;
            case 1:
                to = "Planned for ";
                break;
            case 2:
                to = "Due ";
                break;
            default:
                to = "Error: to column is unexpectedly " + cursor.getInt(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TO));
        }

        String from;
        switch (cursor.getInt(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_FROM))) {
            case 0:
                from = "new start date";
                break;
            case 1:
                from = "new plan date";
                break;
            case 2:
                from = "new due date";
                break;
            case 3:
                from = template ? "creation date" : "completion date";
                break;
            case 4:
                from = template ? "creation date" : "old start date";
                break;
            case 5:
                from = template ? "creation date" : "old plan date";
                break;
            case 6:
                from = template ? "creation date" : "old due date";
                break;
            default:
                from = "\nError: from column is unexpectedly " + cursor.getInt(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_FROM));
        }

        String subvalue = cursor.getString(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_SUBVALUE));
        if (subvalue == null)
            subvalue = "";
        String direction = subvalue.startsWith("-") ? " before " : " after ";
        subvalue = subvalue.replace("-", "");
        String s = "1".equals(subvalue) ? "" : "s";

        String full;
        switch (cursor.getInt(cursor.getColumnIndexOrThrow(RepetitionRulesTable.COLUMN_TYPE))) {
            case 0:
                full = to + subvalue + " day" + s + direction + from;
                break;
            case 1:
                full = to + subvalue + " month" + s + direction + from;
                break;
            case 2:
                full = to + "next " + subvalue + direction + from;
                break;
            case 3:
                full = to + subvalue + " week" + s + direction + from;
                break;
            case 4:
                full = to + subvalue + " year" + s + direction + from;
                break;
            case 5:

                full = to + "at " + subvalue;
                break;
            default:
                full = "Error: unknown rule type";
                break;
        }
        return full;
    }

    /**
     * Takes a Calendar and adds enough days to wrap around to the given weekday.
     * Never moves less than 2 days forward (if you want "today" or "tomorrow", use those).
     * @param cal Calendar set to a date, usually today
     * @param weekday Calendar.SUNDAY through Calendar.SATURDAY
     */
    public static void advanceCalendarToNextWeekday(Calendar cal, int weekday){
        int start = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, (weekday-start+5)%7+2);
    }

    public static String[] idToSelectionArgs(long id) {
        return new String[]{String.valueOf(id)};
    }
}
