package com.lcjian.drinkwater.util;

public class ComputeUtils {

    public static double computeDailyRecommendIntakeGoal(double weight, int gender) {
        return weight + gender + 800;
    }
}
