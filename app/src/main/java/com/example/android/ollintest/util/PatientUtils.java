package com.example.android.ollintest.util;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class PatientUtils {

    public static String getFormatName(String fullName) {
        fullName = fullName.replaceAll("\\s+", " ");
        String outcome = fullName.length() > 15 ?
                fullName.substring(0, 12) + "..." :
                fullName;
        return outcome;
    }

    public static String getAgeName(String birthday){
        return birthday;
    }
}
