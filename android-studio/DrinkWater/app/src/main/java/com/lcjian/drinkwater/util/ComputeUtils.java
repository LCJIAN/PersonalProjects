package com.lcjian.drinkwater.util;

import java.util.Date;

public class ComputeUtils {

    public static double computeDailyRecommendIntakeGoal(double weight, int gender) {
        return weight * 35 + (gender == 0 ? 750 : 0);
    }

    public static int computeDailyTimeInMinutes(String wakeUpTime, String sleepTime) {
        Date w = DateUtils.convertStrToDate(wakeUpTime, "HH:mm");
        Date s = DateUtils.convertStrToDate(sleepTime, "HH:mm");
        if (DateUtils.isAfter(w, s)) {
            s = DateUtils.addDays(s, 1);
        }
        return (int) (s.getTime() - w.getTime()) / (60 * 1000);
    }
}
