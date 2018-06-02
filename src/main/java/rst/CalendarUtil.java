package rst;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class CalendarUtil {
    private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    static DateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    static String getYesterdayDateString() {
        return dateFormat.format(yesterday());
    }

    static String getTodayDateString() {
        return dateFormat.format(new Date());
    }

    static String getTimeStamp() {
        return fullDateFormat.format(new Date());
    }
}
