package com.myapartment1;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {


    public static int getMonth(String month) {

        try {
            Date date = new SimpleDateFormat("MMMM").parse(month);//put your month name here
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int monthNumber = cal.get(Calendar.MONTH);
            return monthNumber;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getMonthName(String dateFromExcel) {
        String[] monthName = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date parse = null;
        try {
            parse = sdf.parse(dateFromExcel);
            Calendar c = Calendar.getInstance();
            c.setTime(parse);
            String monthNameStr = monthName[c.get(Calendar.MONTH)];
            return monthNameStr;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }


    }

}
