package com.ruoyi.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author ruoyi
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     *
     * @param endDate   最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }


    /**
     * 获取指定时间的周几
     *
     * @param date 时间
     * @param day  周几
     */
    public static Date getWeekDay(Date date, int day) {
        day = Math.min(7, day);
        day = Math.max(1, day);
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateDay = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(day)));
        return toDate(dateDay);
    }

    public static Date getWeekDay(Date date, int week, int day) {
        day = Math.min(7, day);
        day = Math.max(1, day);
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        localDate.plusWeeks(week);
        LocalDate dateDay = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(day)));
        return toDate(dateDay);
    }

    public static String getWeekDay(Date date, int week, int day, String format) {
        return parseDateToStr(format, getWeekDay(date, week, day));
    }

    public static String getWeekDay(Date date, int day, String format) {
        return parseDateToStr(format, getWeekDay(date, day));
    }

    /**
     * 根据指定时间返回所在年份的第几周
     */
    public static Long getWeekDayNumber(Date day, Date year) {
        LocalDate dayDate = day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate yearDate = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 获取year参数所在年份
        int targetYear = yearDate.getYear();

        // 创建一个带有周数计算规则的日期对象（周一作为一周的开始，第一周最少包含1天）
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);

        // 获取目标年份的第一天和最后一天
        LocalDate firstDayOfYear = LocalDate.of(targetYear, 1, 1);
        LocalDate lastDayOfYear = LocalDate.of(targetYear, 12, 31);

        // 如果day日期在目标年份之前或之后，也可以计算其在该年份中的相对周数
        if (dayDate.isBefore(firstDayOfYear)) {
            // 日期在目标年份之前，返回第0周或第1周
            return 1L;
        } else if (dayDate.isAfter(lastDayOfYear)) {
            // 日期在目标年份之后，返回最后一周
            return (long) lastDayOfYear.get(weekFields.weekOfYear());
        } else {
            // 日期在目标年份内，正常计算周数
            return (long) dayDate.get(weekFields.weekOfYear());
        }
    }

    /**
     * 获取指定时间所在月的指定日
     *
     * @param date 时间
     * @param day  天数
     */
    public static Date getMonthDay(Date date, int day) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        day = Math.min(day, localDate.lengthOfMonth());
        day = Math.max(day, 1);
        LocalDate dateDay = localDate.withDayOfMonth(day);
        return DateUtils.toDate(dateDay);
    }

    public static String getMonthDay(Date date, int day, String format) {
        return parseDateToStr(format, getMonthDay(date, day));
    }

    /**
     * 获取指定月份指定天数的日期
     *
     * @param date  时间
     * @param month 月份
     * @param day   天数
     */
    public static Date getMonthDay(Date date, int month, int day) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //加减月份
        localDate = localDate.plusMonths(month);
        //根据月份获取指定日
        int maxDays = localDate.lengthOfMonth();
        day = Math.min(day, maxDays);
        day = Math.max(day, 1);
        LocalDate targetDate = localDate.withDayOfMonth(day);
        return DateUtils.toDate(targetDate);
    }

    public static String getMonthDay(Date date, int month, int day, String format) {
        return parseDateToStr(format, getMonthDay(date, month, day));
    }

    /**
     * 获取指定年份的指定日
     *
     * @param date
     * @param year
     * @param day
     * @return String
     **/
    public static Date getYearDay(Date date, int year, int day) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //加减年份
        localDate = localDate.plusYears(year);
        //根据年份获取指定日
        day = Math.min(day, localDate.lengthOfYear());
        day = Math.max(day, 1);
        LocalDate temporalAccessor = localDate.withDayOfYear(day);
        return toDate(temporalAccessor);
    }

    public static String getYearDay(Date date, int year, int day, String format) {
        return parseDateToStr(format, getYearDay(date, year, day));
    }

    /**
     * 获取指定时间所在年份的指定日
     *
     * @param date 时间
     * @param day  天数
     */
    public static Date getYearDay(Date date, int day) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        day = Math.min(day, localDate.lengthOfYear());
        day = Math.max(day, 1);
        LocalDate dateDay = localDate.withDayOfYear(day);
        return toDate(dateDay);
    }

    public static String getYearDay(Date date, int day, String format) {
        return parseDateToStr(format, getYearDay(date, day));
    }

    /**
     * 获取指定时间指定n天后
     */
    public static Date getDay(Date date, int day) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate days = localDate.minusDays(day);
        return DateUtils.toDate(days);
    }

    public static String getDay(Date date, int day, String format) {
        return parseDateToStr(format, getDay(date, day));
    }

}
