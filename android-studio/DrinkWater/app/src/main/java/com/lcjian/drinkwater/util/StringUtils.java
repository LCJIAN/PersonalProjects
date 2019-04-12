package com.lcjian.drinkwater.util;

import java.text.DecimalFormat;

public class StringUtils {

    private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public static String formatDecimal(Double aDouble) {
        return DECIMAL_FORMAT.format(aDouble);
    }
}
