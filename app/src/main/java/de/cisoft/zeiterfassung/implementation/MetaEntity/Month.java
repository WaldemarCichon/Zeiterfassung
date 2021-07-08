package de.cisoft.zeiterfassung.implementation.MetaEntity;

import java.util.LinkedList;

import de.cisoft.zeiterfassung.implementation.entity.Booking;


public class Month extends DayRange {
    private static String[] Months = {
            "Januar",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember"
    };


    private int month;


    public Month(int monthIndex) {
        this.month = monthIndex;
    }

    public boolean isValid(Booking booking) {
        return (booking.getTimestamp().getMonth() == month);
    }

    public String getName() {
        return Months[month];
    }
}
