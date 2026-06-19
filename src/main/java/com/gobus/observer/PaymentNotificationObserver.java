package com.gobus.observer;

import com.gobus.dao.NotifikasiDAO;
import com.gobus.entity.Booking;
import com.gobus.entity.Notifikasi;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentNotificationObserver implements BookingEventListener {

    private final NotifikasiDAO notifikasiDAO;

    public PaymentNotificationObserver(NotifikasiDAO notifikasiDAO) {
        this.notifikasiDAO = notifikasiDAO;
    }

    @Override
    public void onPaymentSuccess(Booking booking) {
        Notifikasi notif = new Notifikasi();
        notif.setUserId(booking.getUserId());
        notif.setBookingId(booking.getId());
        notif.setMessage("Payment successful! Your booking code: " + booking.getBookingCode()
                + " | Seats " + booking.getSeatNumbersText()
                + " | Route: " + booking.getRoute());
        notif.setSendTime(LocalDateTime.now());
        notif.setRead(false);
        notifikasiDAO.save(notif);
    }
}
