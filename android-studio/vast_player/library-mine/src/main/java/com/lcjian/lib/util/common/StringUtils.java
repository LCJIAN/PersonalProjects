package com.lcjian.lib.util.common;

import java.util.Formatter;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static StringBuilder mFormatBuilder;
    private static Formatter mFormatter;

    public static String join(String[] strs) {
        StringBuilder result = new StringBuilder();
        if (strs != null) {
            for (String str : strs) {
                result.append(str).append(",");
            }
        }
        if (result.length() > 0) {
            return result.substring(0, result.length() - 1);
        }
        return "";
    }

    public static boolean isInteger(String aString) {
        try {
            Integer.parseInt(aString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains("."))
                return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isChinesrChar(String str) {
        if (str.length() < str.getBytes().length) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmailVaild(String aEmail) {
        boolean isValid = true;
        Pattern pattern = Pattern.compile(
                "^([a-zA-Z0-9]+[_|-|.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|-|.]?)*[a-zA-Z0-9]+\\.[a-zA-Z]{2,3}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(aEmail);
        if (matcher.matches()) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * 大陆号码或香港号码均可
     */
    public static boolean isPhoneLegal(String str) {
        return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 145,147,149
     * 15+除4的任意数(不要写^4，这样的话字母也会被认为是正确的)
     * 166
     * 17+3,5,6,7,8
     * 18+任意数
     * 198,199
     */
    public static boolean isChinaPhoneLegal(String str) {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[3,5,6,7,8])" +
                "|(18[0-9])|(19[8,9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    public static boolean isHKPhoneLegal(String str) {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static String formatPhoneNum(String aPhoneNum) {
        String first = aPhoneNum.substring(0, 3);
        String end = aPhoneNum.substring(7, 11);
        String phoneNumber = first + "****" + end;
        return phoneNumber;
    }

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')
                    && !(s.charAt(i) >= 'a' && s.charAt(i) <= 'z')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLetterOrNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')
                    && !(s.charAt(i) >= 'a' && s.charAt(i) <= 'z')
                    && !Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLetterOrNumericOrChinese(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')
                    && !(s.charAt(i) >= 'a' && s.charAt(i) <= 'z')
                    && !Character.isDigit(s.charAt(i)) && !(s.length() < s.getBytes().length)) {
                return false;
            }
        }
        return true;
    }

    public static String clearSpaces(String aString) {
        StringTokenizer aStringTok = new StringTokenizer(aString, " ", false);
        String aResult = "";
        while (aStringTok.hasMoreElements()) {
            aResult += aStringTok.nextElement();
        }
        return aResult;
    }

    /**
     * is null or its length is 0 or it is made by space
     *
     * @param str
     * @return if string is null or its size is 0 or it is made by space, return true, else return false.
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * is null or its length is 0
     *
     * @param str
     * @return if string is null or its size is 0, return true, else return false.
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    /**
     * compare two string
     *
     * @param actual
     * @param expected
     * @return
     * @see ObjectUtils#isEquals(Object, Object)
     */
    public static boolean isEquals(String actual, String expected) {
        return ObjectUtils.isEquals(actual, expected);
    }

    /**
     * capitalize first letter
     * <p>
     * <pre>
     * capitalizeFirstLetter(null)     =   null;
     * capitalizeFirstLetter("")       =   "";
     * capitalizeFirstLetter("2ab")    =   "2ab"
     * capitalizeFirstLetter("a")      =   "A"
     * capitalizeFirstLetter("ab")     =   "Ab"
     * capitalizeFirstLetter("Abc")    =   "Abc"
     * </pre>
     *
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char c = str.charAt(0);
        return (!Character.isLetter(c) || Character.isUpperCase(c)) ? str
                : new StringBuilder(str.length()).append(Character.toUpperCase(c)).append(str.substring(1)).toString();
    }

    public static String stringForTime(int timeMs) {
        return stringForTime(timeMs, false, false);
    }

    public static String stringForTime(int timeMs, boolean show, boolean space) {
        if (mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder();
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            if (show) {
                if (space) {
                    return mFormatter.format(" %02d : %02d : %02d ", hours, minutes, seconds).toString();
                } else {
                    return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
                }
            } else {
                return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            }
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}