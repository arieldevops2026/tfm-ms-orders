package dev.ariel.tfm.orders.util.utils;

public class GeneralUtils {

    private GeneralUtils() {}

    public static String coalesce(String... values) {
        for (String v : values)
            if (v != null) return v;
        return "";
    }

}
