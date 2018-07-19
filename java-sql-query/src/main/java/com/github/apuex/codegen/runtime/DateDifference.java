package com.github.apuex.codegen.runtime;

import com.google.protobuf.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Seconds;

public class DateDifference {
    public static boolean isBefore(Timestamp theTime, long refInstant) {
        return DateFormat.toDateTime(theTime).isBefore(refInstant);
    }

    public static int diffInSeconds(Timestamp begin, Timestamp end) {
        return Seconds.secondsBetween(DateFormat.toDateTime(begin), DateFormat.toDateTime(end)).getSeconds();
    }

    public static int differenceInDays(DateTime begin, DateTime end) {
        return Days.daysBetween(begin, end).getDays();
    }
}
