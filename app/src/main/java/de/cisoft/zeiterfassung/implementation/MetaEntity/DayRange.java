package de.cisoft.zeiterfassung.implementation.MetaEntity;

import java.util.LinkedList;

import de.cisoft.zeiterfassung.implementation.entity.Booking;

public abstract class DayRange {
    private LinkedList<Booking> bookings;
    private double sum;

    public DayRange() {
        bookings = new LinkedList<>();
        sum = 0.0;
    }

    public void add (Booking booking) {
        bookings.add(booking);
    }

    public double getSum() {
        return sum;
    }

    public void recalc() {
        bookings.forEach(booking -> {

        });
    }

    public abstract boolean isValid(Booking booking);

    public boolean isFirstBooking(Booking booking) {
        return booking == bookings.getFirst();
    }

    public boolean isLastBooking(Booking booking) {
        return booking == bookings.getLast();
    }

}
