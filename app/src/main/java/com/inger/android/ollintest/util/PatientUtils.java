package com.inger.android.ollintest.util;

import com.inger.android.ollintest.R;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class PatientUtils {

    static final int BIRTHDAY_FORMAT = 1;
    static final int UPDATE_FORMAT = 2;

    public static String getFormatName(String fullName) {
        fullName = fullName.replaceAll("\\s+", " ");
        String outcome = fullName.length() > 20 ?
                fullName.substring(0, 17) + "..." :
                fullName;
        return outcome;
    }

    public static String getAge(String birthdayISO8601) {
        int age;

        DateUtil dateObj = new DateUtil();

        String today = convertFromISO8601(dateObj.getCurrentDate(), UPDATE_FORMAT); // DD/MM/YYYY
        String[] todayArray = today.split("/");
        int today_day = Integer.parseInt(todayArray[0]);
        int today_month = Integer.parseInt(todayArray[1]);
        int today_year = Integer.parseInt(todayArray[2]);

        String birthday = convertFromISO8601(birthdayISO8601, BIRTHDAY_FORMAT); // DD/MM/YYYY
        String[] birthdayArray = birthday.split("/");
        int birthday_day = Integer.parseInt(birthdayArray[0]);
        int birthday_month = Integer.parseInt(birthdayArray[1]);
        int birthday_year = Integer.parseInt(birthdayArray[2]);


        age = (today_year - birthday_year);

        if (today_month >= birthday_month && today_day >= birthday_day) age += 1;

        return age + " ";
    }

    // Return it in format: YYYY-MM-DDTHH:MM:SSZ+HH:MM
    public static String convertToISO8601(String date, int option) {
        String formattedDate = "";

        if (!date.isEmpty()) {
            // We received follow formatted date: dd/mm/yyyy
            if (option == BIRTHDAY_FORMAT) {
                String[] dateArray = date.split("/");
                String day = dateArray[0];
                String month = dateArray[1];
                String year = dateArray[2];

                if (day.length() == 1) day = "0" + day;
                if (month.length() == 1) month = "0" + month;

                formattedDate = year + "-" + month + "-" + day;
            }

            if (option == UPDATE_FORMAT) {
                // Not required yet
            }
        }

        return formattedDate;
    }

    // Return it in format: DD/MM/YYYY (HH:MM)
    public static String convertFromISO8601(String date, int option) {
        String formattedDate = "";

        if (!date.isEmpty()) {
            // We received follow formatted date: YYYY-MM-DD
            if (option == BIRTHDAY_FORMAT) {
                String[] dateArray = date.split("-");
                String day = dateArray[2];
                String month = dateArray[1];
                String year = dateArray[0];

                if (day.length() == 1) day = "0" + day;
                if (month.length() == 1) month = "0" + month;

                formattedDate = day + "/" + month + "/" + year;
            }

            // We received follow formatted date: YYYY-MM-DDTHH:MM:SSZ+HH:MM
            if (option == UPDATE_FORMAT) {
                String[] dateArray = date.split("-");
                String day_rest = dateArray[2];
                String month = dateArray[1];
                String year = dateArray[0];

                String[] dayArray = day_rest.split("T");
                String day = dayArray[0];
                String time = dayArray[1];

                String[] timeArray = time.split(":");
                String hour = timeArray[0];
                String minute = timeArray[1];

                if (day.length() == 1) day = "0" + day;
                if (month.length() == 1) month = "0" + month;

                formattedDate = day + "/" + month + "/" + year;
            }
        }

        return formattedDate;
    }

}
