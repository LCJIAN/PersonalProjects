package com.lcjian.mmt.util;

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

    public static boolean isMobileNumber(String aTelNumber) {
        Pattern p = Pattern.compile("(^1((((3[5-9])|(47)|(5[0-2])|(5[7-9])|(82)|(8[7-8]))\\d{8})|((34[0-8])\\d{7}))$)|(^1((3[0-2])|(5[5-6])|(8[0-6]))\\d{8}$)|(^1((33[0-9])|(349)|(53[0-9])|(80[0-9])|(89[0-9]))\\d{7}$)");
        Matcher m = p.matcher(aTelNumber);
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