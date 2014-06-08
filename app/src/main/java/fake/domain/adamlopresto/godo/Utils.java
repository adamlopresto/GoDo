package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;


@SuppressLint ("SimpleDateFormat")
public final class Utils {

    private static final DateFormat SHORT_TIME = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final long DECEMBER_31_2099_IN_MS = 64063198800000L;
    public static final Date SOMEDAY = new Date(DECEMBER_31_2099_IN_MS);
    @SuppressWarnings ("SpellCheckingInspection")
    private static final DateFormat weekday = new SimpleDateFormat("EEEE");
    private static final DateFormat SHORT_DATE = new SimpleDateFormat("MMM d");
    private static final DateFormat SHORT_DATE_WITH_YEAR = new SimpleDateFormat("MMM d, yyyy");
    private static final DateFormat REALLY_SHORT_TIME = new SimpleDateFormat("h a");

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
            return SHORT_DATE.format(then);
        return SHORT_DATE_WITH_YEAR.format(then);
    }

    public static String formatShortRelativeDate(@NotNull String formattedDate) {
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
    public static String getString(@NotNull TextView view) {
        CharSequence seq = view.getText();
        if (seq == null)
            return null;
        return seq.toString();
    }
}
