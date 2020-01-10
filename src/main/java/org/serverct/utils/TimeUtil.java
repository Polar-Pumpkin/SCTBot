package org.serverct.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {

    public static Calendar getCalender(String date) {
        String[] dates = date.split("-");
        Calendar result = Calendar.getInstance();

        result.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[2]));

        return result;
    }

    public static String format(Calendar calendar, String format) {
        return new SimpleDateFormat(format).format(calendar.getTime());
    }

}
