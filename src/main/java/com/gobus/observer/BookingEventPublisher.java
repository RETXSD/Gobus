package com.gobus.observer;

import com.gobus.entity.Booking;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingEventPublisher {

    private final List<BookingEventListener> listeners;

    public BookingEventPublisher(List<BookingEventListener> listeners) {
        this.listeners = listeners;
    }

    public void notifyPaymentSuccess(Booking booking) {
        for (BookingEventListener listener : listeners) {
            listener.onPaymentSuccess(booking);
        }
    }
}
