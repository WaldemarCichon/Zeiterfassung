package de.cisoft.zeiterfassung.implementation.helpers;

import de.cisoft.zeiterfassung.implementation.entity.Booking;

public interface ResendCallback {
        void callback(int count, Booking booking);
}
