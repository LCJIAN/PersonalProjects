package com.lcjian.spunsugar.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static Map<String, String> filterMapProductionCountry = getFilterMap("filter_production_country.txt");
    private static Map<String, String> filterMapGenre = getFilterMap("filter_movie_tv_show_genre.txt");

    private static Map<String, String> getFilterMap(String resourceName) {
        BufferedReader br = null;
        Map<String, String> result = new HashMap<>();
        try {
            br = new BufferedReader(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(resourceName), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    String[] ar = line.split(":");
                    result.put(ar[0], ar[1]);
                } else {
                    result.put(line, "");
                }
            }
        } catch (FileNotFoundException ignore) {
        } catch (IOException ignore) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return result;
    }

    public static String getFilteredGenreName(String name) {
        String result = filterMapGenre.get(name);
        return result == null ? name  : result;
    }
    
    public static String getFilteredProductionCountryName(String name) {
        String result = filterMapProductionCountry.get(name);
        return result == null ? name  : result;
    }
}
