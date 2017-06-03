package de.freewarepoint.whohasmystuff;

import android.content.res.Resources;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

class DurationCalculator {
    
    private final Resources resources;

    DurationCalculator(Resources resources) {
        this.resources = resources;
    }

    String getTimeDifference(Calendar lentDate, Calendar now) {
        if (now.before(lentDate)) {
            return "0 days";
        }

        // Check if one or more years have passed

        int differenceInYears = now.get(Calendar.YEAR) - lentDate.get(Calendar.YEAR);
        Calendar lentTimeInSameYear = new GregorianCalendar();
        lentTimeInSameYear.setTimeInMillis(lentDate.getTimeInMillis());
        lentTimeInSameYear.set(Calendar.YEAR, now.get(Calendar.YEAR));
        if (now.before(lentTimeInSameYear)) {
            differenceInYears--;
        }

        if (differenceInYears > 1) {
            return differenceInYears + " " + resources.getString(R.string.years);
        }
        else if (differenceInYears > 0) {
            return differenceInYears + " " + resources.getString(R.string.year);
        }

        // Check if one or more months have passed

        int monthsOfLentDate = lentDate.get(Calendar.YEAR) * 12 + lentDate.get(Calendar.MONTH);
        int monthsNow = now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH);
        int differenceInMonths = monthsNow - monthsOfLentDate;
        Calendar lentTimeInSameMonth = new GregorianCalendar();
        lentTimeInSameMonth.setTimeInMillis(lentDate.getTimeInMillis());
        lentTimeInSameMonth.set(Calendar.YEAR, now.get(Calendar.YEAR));
        lentTimeInSameMonth.set(Calendar.MONTH, now.get(Calendar.MONTH));
        if (now.before(lentTimeInSameMonth)) {
            differenceInMonths--;
        }

        if (differenceInMonths > 1) {
            return differenceInMonths + " " + resources.getString(R.string.months);
        }
        else if (differenceInMonths > 0) {
            return differenceInMonths + " " + resources.getString(R.string.month);
        }

        // Check if one or more weeks have passed

        long difference = now.getTimeInMillis() - lentDate.getTimeInMillis();
        int differenceInDays = (int) (difference / DateUtils.DAY_IN_MILLIS);
        int differenceInWeeks = differenceInDays / 7;

        if (differenceInWeeks > 1) {
            return differenceInWeeks + " " + resources.getString(R.string.weeks);
        }
        else if (differenceInWeeks > 0) {
            return differenceInWeeks + " " + resources.getString(R.string.week);
        }

        // Check if one or more days have passed

        final Calendar lentTimeInSameDay = new GregorianCalendar();
        lentTimeInSameDay.setTimeInMillis(lentDate.getTimeInMillis());
        lentTimeInSameDay.set(Calendar.YEAR, now.get(Calendar.YEAR));
        lentTimeInSameDay.set(Calendar.MONTH, now.get(Calendar.MONTH));
        lentTimeInSameDay.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

        if (differenceInDays == 1 && now.before(lentTimeInSameDay)) {
            differenceInDays++;
        }

        if (differenceInDays > 1) {
            return differenceInDays + " " + resources.getString(R.string.days);
        }
        else if (differenceInDays == 1) {
            return resources.getString(R.string.yesterday);
        }
        else if (differenceInDays == 0) {
            if (now.get(Calendar.DAY_OF_MONTH) == lentDate.get(Calendar.DAY_OF_MONTH)) {
                return resources.getString(R.string.today);
            }
            else {
                return resources.getString(R.string.yesterday);
            }
        }
        else {
            return resources.getString(R.string.unknown);
        }
    }
}
