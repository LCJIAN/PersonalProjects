package com.lcjian.osc.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 日期工具类
 *
 * @author LCJIAN
 */
public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM_SS_2 = "yyMMddHHmmss";
    /**
     * 年-月-日，默认日期格式
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_2 = "yyyyMMdd";
    public static final String YY_MM_DD = "yy-MM-dd";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final long DAY = 24 * 60 * 60 * 1000L;
    private static final String TAG = "DateUtils";
    private static final Map<String, SimpleDateFormat> DFS = new HashMap<String, SimpleDateFormat>();
    private static StringBuilder mFormatBuilder;
    private static Formatter mFormatter;

    static {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * protected构造方法
     */
    protected DateUtils() {
    }

    public static SimpleDateFormat getFormat(String pattern) {
        SimpleDateFormat format = DFS.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern, Locale.getDefault());
            DFS.put(pattern, format);
        }
        return format;
    }

    /**
     * 使用默认格式将日期转换成字符串
     *
     * @param date 需要转换的日期
     * @return 转换后的字符串，如果发生异常，返回空字符串
     */
    public static String convertDateToStr(Date date) {
        if (date == null) {
            return "";
        } else {
            return convertDateToStr(date, YYYY_MM_DD);
        }
    }

    /**
     * 使用自定义格式，将日期转换成字符串
     *
     * @param date    需要转换的日期
     * @param pattern 自定义格式
     * @return 转换后的字符串，如果发生异常，返回空字符串
     */
    public static String convertDateToStr(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateFormat = getFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 使用默认格式，将字符串转换成日期
     *
     * @param str 需要转换的字符串
     * @return 转换后的日期，如果发生异常，返回null
     */
    public static Date convertStrToDate(String str) {
        if (str == null || str.equals("")) {
            return null;
        } else {
            return convertStrToDate(str, YYYY_MM_DD);
        }
    }

    /**
     * 根据自定义pattern将字符串日期转换成Date类型
     *
     * @param str     需要转换的字符串
     * @param pattern 自定义格式
     * @return 转换后的日期，如果发生异常，返回null
     */
    public static Date convertStrToDate(String str, String pattern) {
        if (str == null || str.equals("")) {
            return null;
        }
        SimpleDateFormat dateFormat = getFormat(pattern);
        try {
            return dateFormat.parse(str);
        } catch (ParseException ex) {
            Log.i(TAG, "convertStrToDate: " + ex.getMessage());
            return null;
        }
    }

    /**
     * 将patternSrc格式的str转换为patterDst的字符串
     *
     * @param str        需要转换的字符串
     * @param patternSrc 原格式
     * @param patterDst  目标格式
     * @return
     */
    public static String formatDateStr(String str, String patternSrc, String patterDst) {
        return convertDateToStr(convertStrToDate(str, patternSrc), patterDst);
    }

    /**
     * 判断原日期是否在目标日期之前
     *
     * @param src
     * @param dst
     * @return
     */
    public static boolean isBefore(Date src, Date dst) {
        return src.before(dst);
    }

    /**
     * 判断原日期是否在目标日期之后
     *
     * @param src
     * @param dst
     * @return
     */
    public static boolean isAfter(Date src, Date dst) {
        return src.after(dst);
    }

    /**
     * 返回两个日期间的差异天数
     *
     * @param date1 参照日期
     * @param date2 比较日期
     * @return 参照日期与比较日期之间的天数差异，正数表示参照日期在比较日期之后，0表示两个日期同天，负数表示参照日期在比较日期之前
     */
    public static int dayDiff(Date date1, Date date2) {
        Date fromDate = convertStrToDate(convertDateToStr(date1));
        Date toDate = convertStrToDate(convertDateToStr(date2));
        return (int) ((fromDate.getTime() - toDate.getTime()) / (DAY));
    }

    public static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String sp_time = sf.format(date1);
        String current_time = sf.format(date2);

        return sp_time.equals(current_time);
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String sp_time = sf.format(date1);
        String current_time = sf.format(date2);

        return sp_time.equals(current_time);
    }

    /**
     * 判断两日期是否相同(毫秒)
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isEqual(Date date1, Date date2) {
        return date1.compareTo(date2) == 0;
    }

    public static String getRelativeTimeStr(Date date) {
        long l = now().getTime() - date.getTime();
        if (l < 1000 * 60 * 60) {
            return (l / (1000 * 60)) + "分钟前";
        } else if (l < (1000 * 60 * 60 * 24)) {
            return (l / (1000 * 60 * 60)) + "小时前";
        } else if (l < (1000 * 60 * 60 * 24 * 2)) {
            return "昨天";
        } else if (l < (1000 * 60 * 60 * 24 * 3)) {
            return "前天";
        } else {
            return convertDateToStr(date);
        }
    }

    public static String getPeriod(Date begin, Date end) {
        long between = (end.getTime() - begin.getTime()) / 1000;
        long day = between / (24 * 3600);
        long hour = between % (24 * 3600) / 3600;
        long minute = between % 3600 / 60;
        long second = between % 60 / 60;

        return (day == 0 ? "" : day + "天")
                + (hour == 0 ? "" : hour + "小时")
                + (minute == 0 ? "" : minute + "分")
                + (second == 0 ? "" : second + "秒");
    }

    /**
     * 获得当前时间的Date对象
     *
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取星期几
     */
    public static String getWeekDay(Date date) {
//        String[] weeks = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
//        if (week_index < 0) {
//            week_index = 0;
//        }
//        return weeks[week_index];
        SimpleDateFormat sdf = getFormat("EEEE");
        return sdf.format(date);
    }

    /**
     * 返回当月第一天的日期
     */
    public static Date firstDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 返回当月最后一天的日期
     */
    public static Date lastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        return calendar.getTime();
    }

    /**
     * 增加月份
     */
    public static Date addMonths(Date src, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(src);
        calendar.add(Calendar.MONTH, num);
        return calendar.getTime();
    }

    /**
     * 增加天数
     */
    public static Date addDays(Date src, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(src);
        calendar.add(Calendar.DATE, num);
        return calendar.getTime();
    }

    /**
     * @param year  年
     * @param month 月(1-12)
     * @param day   日(1-31)
     * @return 输入的年、月、日是否是有效日期
     */
    public static boolean isValid(int year, int month, int day) {
        if (month > 0 && month < 13 && day > 0 && day < 32) {
            // month of calendar is 0-based
            int mon = month - 1;
            Calendar calendar = new GregorianCalendar(year, mon, day);
            if (calendar.get(Calendar.YEAR) == year
                    && calendar.get(Calendar.MONTH) == mon
                    && calendar.get(Calendar.DAY_OF_MONTH) == day) {
                return true;
            }
        }
        return false;
    }

    public static String stringForTime(long timeSeconds) {
        long seconds = timeSeconds % 60;
        long minutes = (timeSeconds / 60) % 60;
        long hours = timeSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}