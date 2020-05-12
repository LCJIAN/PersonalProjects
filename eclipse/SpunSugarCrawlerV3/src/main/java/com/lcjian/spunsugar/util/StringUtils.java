package com.lcjian.spunsugar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 字符快捷方式：判断字符串<中文字符、邮箱、手机号、空字符、整数、浮点数>
 */
public class StringUtils {
    public static final String EMPTY_STRING = ""; // 空字符串
    
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

    // 解析短信推送内容
    public static Map<String, Object> getParameterMap(String data) {
        Map<String, Object> map = null;
        if (data != null) {
            map = new HashMap<String, Object>();
            String[] params = data.split("&");
            for (int i = 0; i < params.length; i++) {
                int idx = params[i].indexOf("=");
                if (idx >= 0) {
                    map.put(params[i].substring(0, idx), params[i].substring(idx + 1));
                }
            }
        }
        return map;
    }

    // 检测字符串是否符合用户名
    public static boolean checkingMsg(int len) {
        boolean isValid = true;
        if (5 < len && len < 21) {
            isValid = false;
        } else {
            isValid = true;
        }
        return isValid;
    }

    // 检测
    public static boolean isVaild(int len) {
        boolean isValid = true;
        if (1 < len && len < 17) {
            isValid = false;
        }
        return isValid;
    }
    
    // 判断字符串是否是整数
    public static boolean isInteger(String aString) {
        try {
            Integer.parseInt(aString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 判断字符串是否是浮点数
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

    // 检测字符串是否为中文字符
    public static boolean isChinesrChar(String str) {
        if (str.length() < str.getBytes().length) {
            return true;
        } else {
            return false;
        }
    }

    // 判断字符串是否为邮箱
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

    // 判断字符串是否为手机号码
    public static boolean isMobileNumber(String aTelNumber) {
        Pattern p = Pattern.compile("(^1((((3[5-9])|(47)|(5[0-2])|(5[7-9])|(82)|(8[7-8]))\\d{8})|((34[0-8])\\d{7}))$)|(^1((3[0-2])|(5[5-6])|(8[0-6]))\\d{8}$)|(^1((33[0-9])|(349)|(53[0-9])|(80[0-9])|(89[0-9]))\\d{7}$)");
        Matcher m = p.matcher(aTelNumber);
        return m.matches();
    }
    // 格式化手机号码
    public static String formatPhoneNum(String aPhoneNum) {
        String first = aPhoneNum.substring(0, 3);
        String end = aPhoneNum.substring(7, 11);
        String phoneNumber = first + "****" + end;
        return phoneNumber;
    }

    // 检查字符串是否为纯数字
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0;) {
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

    // 去除字符串中空格
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
     * capitalize first letter
     * 
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
}