package com.gobus.observer;

import com.gobus.dao.NotificationDAO;
import com.gobus.entity.Booking;
import com.gobus.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentNotificationObserver implements BookingEventListener {

    private final NotificationDAO notificationDAO;

    public PaymentNotificationObserver(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void onPaymentSuccess(Booking booking) {
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setBookingId(booking.getId());
        notification.setMessage("Payment successful! Your booking code: " + booking.getBookingCode()
                + " | Seats " + booking.getSeatNumbersText()
                + " | Route: " + booking.getRoute());
        notification.setSendTime(LocalDateTime.now());
        notification.setRead(false);
        notificationDAO.save(notification);
    }
}
