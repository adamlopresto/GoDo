package fake.domain.adamlopresto.godo;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fake.domain.adamlopresto.godo.db.DatabaseHelper;


@SuppressLint("SimpleDateFormat")
public abstract class Utils {
    public static final DateFormat SHORT_TIME = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    private static final SimpleDateFormat weekday = new SimpleDateFormat("EEEE");
    private static final DateFormat SHORT_DATE = new SimpleDateFormat("MMM d");
    private static final DateFormat SHORT_DATE_WITH_YEAR = new SimpleDateFormat("MMM d, yyyy");
    private static final DateFormat REALLY_SHORT_TIME = new SimpleDateFormat("h a");

    public static String formatShortRelativeDate(Date then, boolean hasTime) {
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
        else if (thenCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
            date = SHORT_DATE.format(then);
        else
            date = SHORT_DATE_WITH_YEAR.format(then);

        if (!hasTime)
            return date;

        thenCal.setTime(then);
        String time;
        if (thenCal.get(Calendar.MINUTE) == 0)
            time = REALLY_SHORT_TIME.format(then);
        else
            time = SHORT_TIME.format(then);

        if (diff == 0)
            return time;
        else
            return date + " " + time;
    }

    public static String formatLongRelativeDate(Date then) {
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
        else if (diff == -1)
            return "Yesterday";
        else if (diff == 1)
            return "Tomorrow";
        else if (diff > 0 && diff <= 7)
            return weekday.format(then);
        else if (diff < 0 && diff >= -7)
            return "Last " + weekday.format(then);
        else if (thenCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
            return SHORT_DATE.format(then);
        else
            return SHORT_DATE_WITH_YEAR.format(then);
    }

    public static String formatShortRelativeDate(String formattedDate) {
        try {
            if (formattedDate.length() <= 10)
                return formatShortRelativeDate(DatabaseHelper.dateFormatter.parse(formattedDate), false);
            else
                return formatShortRelativeDate(DatabaseHelper.dateTimeFormatter.parse(formattedDate), true);
        } catch (ParseException e) {
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
    public static boolean isBeforeNow(String formattedDate) {
        if (formattedDate == null)
            return false;
        if (formattedDate.length() > 10)
            return formattedDate.compareTo(DatabaseHelper.dateTimeFormatter.format(new Date())) < 0;
        else
            return formattedDate.compareTo(DatabaseHelper.dateFormatter.format(new Date())) < 0;
    }

    /**
     * Returns true iff the formattedDate passed is unambiguously in the future.
     * Returns false for null, * empty string, today (with no time), and any date in the past.
     *
     * @param formattedDate string formatted date, in local time
     * @return boolean described above
     */
    public static boolean isAfterNow(String formattedDate) {
        if (formattedDate == null)
            return false;
        if (formattedDate.length() > 10)
            return formattedDate.compareTo(DatabaseHelper.dateTimeFormatter.format(new Date())) > 0;
        else
            return formattedDate.compareTo(DatabaseHelper.dateFormatter.format(new Date())) > 0;
    }

    public static String getString(TextView view) {
        CharSequence seq = view.getText();
        if (seq == null)
            return null;
        return seq.toString();
    }
}
