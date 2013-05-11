package fake.domain.adamlopresto.godo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;


public abstract class DateCalc {

	/**
	 * Returns the number of days from now to then. If then is the future, the result is positive.
	 * @param then The date to end up at
	 * @return number of days
	 */
	public static int dateDiff(Date then){
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
		
		return (int)((thenCal.getTimeInMillis()-now.getTimeInMillis()) / 24 / 60 / 60 / 1000);
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat weekday = new SimpleDateFormat("EEEE");
	private static DateFormat shorttime = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
	
	public static String relativeDaysOrDate(Date then){
		int diff = dateDiff(then);
		if (diff == 0)
			return "today";
		else if (diff == -1)
			return "yesterday";
		else if (diff == 1)
			return "tomorrow";
		else if (diff > 0 && diff <= 7)
			return weekday.format(then);
		else if (diff < 0 && diff >= -7)
			return "last "+weekday.format(then);
		else
			return shorttime.format(then);
	}
	
	public static String relativeDaysOrDate(String formattedDate){
		try {
			return relativeDaysOrDate(DatabaseHelper.dateFormatter.parse(formattedDate));
		} catch (ParseException e) {
			return "Invalid date: "+formattedDate;
		}
	}

}
