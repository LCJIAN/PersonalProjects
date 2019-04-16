package com.lcjian.drinkwater.util;

public class StringUtils {

    public static String formatDecimalToString(Double aDouble) {
        return String.valueOf(Math.round(aDouble));
    }

    public static int formatDecimalToInt(Double aDouble) {
        return (int) Math.round(aDouble);
    }
}
