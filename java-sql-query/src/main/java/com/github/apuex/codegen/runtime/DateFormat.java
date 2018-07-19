package com.github.apuex.codegen.runtime;

import com.google.protobuf.Timestamp;
import org.joda.time.DateTime;
import scala.Option;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wangxy on 17-8-28.
 */
public class DateFormat {
  public static final String DATE_PATTERN = "yyyy-MM-dd";
  public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

  public static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
  public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(DATETIME_PATTERN);
  public static final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);

  public static String formatTimestamp(Long tks) {
    Date d = new Date(tks);
    return timestampFormat.format(d);
  }

  public static String formatTimestamp(Date d) {
    return timestampFormat.format(d);
  }

  public static Option<Date> toDate(Option<Timestamp> d) {
    if (d.isEmpty()) {
      return Option.empty();
    } else {
      return Option.apply(toDate(d.get()));
    }
  }

  public static Date toDate(Timestamp d) {
    return new Date(d.getSeconds() * 1000 + d.getNanos() / 1000000);
  }

  public static DateTime toDateTime(Timestamp d) {
    return new DateTime(d.getSeconds() * 1000 + d.getSeconds() / 1000000);
  }

  public static Option<Timestamp> toTimestamp(Option<Date> d) {
    if (d.isEmpty()) {
      return Option.empty();
    } else {
      return Option.apply(toTimestamp(d.get()));
    }
  }

  public static Timestamp toTimestamp(Date d) {
    long seconds = d.getTime() / 1000;
    long nanos = (d.getTime() - seconds * 1000) * 1000000;
    return Timestamp.newBuilder()
        .setSeconds(seconds)
        .setNanos((int) nanos)
        .build();
  }

  public static String formatTimestamp(Timestamp d) {
    return timestampFormat.format(toDate(d));
  }

  public static Date parseTimestamp(String str) {
    try {
      Date d = timestampFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Date parseDate(String str) {
    try {
      Date d = dateFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Date parseDatetime(String str) {
    try {
      Date d = datetimeFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Timestamp parseProtobufTimestamp(String str) {
    return toTimestamp(parseTimestamp(str));
  }
}
