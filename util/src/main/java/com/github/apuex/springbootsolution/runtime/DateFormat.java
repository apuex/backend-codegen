package com.github.apuex.springbootsolution.runtime;

import com.google.protobuf.Timestamp;
import com.google.protobuf.timestamp.Timestamp$;
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


  public static String formatTimestamp(Long tks) {
    Date d = new Date(tks);
    final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
    return timestampFormat.format(d);
  }

  public static String formatTimestamp(Date d) {
    final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
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
    if(null == d) return null;
    return new Date(d.getSeconds() * 1000 + d.getNanos() / 1000000);
  }

  public static DateTime toDateTime(Timestamp d) {
    if(null == d) return null;
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
    if(null == d) return null;
    long seconds = d.getTime() / 1000;
    long nanos = (d.getTime() - seconds * 1000) * 1000000;
    return Timestamp.newBuilder()
        .setSeconds(seconds)
        .setNanos((int) nanos)
        .build();
  }

  public static com.google.protobuf.timestamp.Timestamp toScalapbTimestamp(Date d) {
    if(null == d) return null;
    long seconds = d.getTime() / 1000;
    long nanos = (d.getTime() - seconds * 1000) * 1000000;
    return Timestamp$.MODULE$.apply(seconds, (int)nanos);
  }

  public static String formatTimestamp(Timestamp d) {
    if(null == d) return null;
    final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
    return timestampFormat.format(toDate(d));
  }

  public static Date parseTimestamp(String str) {
    try {
      final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
      Date d = timestampFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(str, e);
    }
  }

  public static Date parseDate(String str) {
    try {
      final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
      Date d = dateFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(str, e);
    }
  }

  public static Date parseDatetime(String str) {
    try {
      final SimpleDateFormat datetimeFormat = new SimpleDateFormat(DATETIME_PATTERN);
      Date d = datetimeFormat.parse(str);
      return d;
    } catch (ParseException e) {
      throw new IllegalArgumentException(str, e);
    }
  }

  public static Timestamp parseProtobufTimestamp(String str) {
    return toTimestamp(parseTimestamp(str));
  }

  public static com.google.protobuf.timestamp.Timestamp  parseScalapbTimestamp(String str) {
    return toScalapbTimestamp(parseTimestamp(str));
  }

  public static Date scalapbToDate(com.google.protobuf.timestamp.Timestamp d) {
    if(null == d) return null;
    return new Date(d.seconds() * 1000 + d.nanos() / 1000000);
  }

  public static Option<Date> scalapbToDate(Option<com.google.protobuf.timestamp.Timestamp> d) {
    return d.map(x -> scalapbToDate(x));
  }
}
