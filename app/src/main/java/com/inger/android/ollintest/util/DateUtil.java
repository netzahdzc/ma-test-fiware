package com.inger.android.ollintest.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class DateUtil {

    public DateUtil() {  }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z");
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nowAsISO = sdf.format(new Date());

        return nowAsISO;
    }

    public String getCurrentDateSQL() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nowAsISO = sdf.format(new Date());

        return nowAsISO;
    }
}
