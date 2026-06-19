package com.gobus.observer;

import com.gobus.entity.Booking;

public interface BookingEventListener {
    void onPaymentSuccess(Booking booking);
}
