package com.gobus.scheduler;

import com.gobus.dao.BookingDAO;
import com.gobus.entity.Booking;
import com.gobus.entity.Notification;
import com.gobus.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final BookingDAO bookingDAO;
    private final NotificationService notificationService;
    private final List<ReminderStrategy> reminderStrategies;

    public NotificationScheduler(BookingDAO bookingDAO, NotificationService notificationService) {
        this.bookingDAO = bookingDAO;
        this.notificationService = notificationService;
        this.reminderStrategies = List.of(
                new ThreeDayReminderStrategy(),
                new OneDayReminderStrategy(),
                new FiveHourReminderStrategy(),
                new ThreeHourReminderStrategy()
        );
    }

    /**
     * Runs every hour and checks for upcoming departures.
     */
    @Scheduled(fixedRate = 3600000)
    public void sendReminderNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[Scheduler] Checking reminder notifications at {}", now);

        for (ReminderStrategy strategy : reminderStrategies) {
            sendRemindersForStrategy(now, strategy);
        }
    }

    private void sendRemindersForStrategy(LocalDateTime now, ReminderStrategy strategy) {
        List<Booking> bookings = bookingDAO.findPaidBookingsWithDepartureBetween(strategy.from(now), strategy.to(now));
        for (Booking booking : bookings) {
            String message = strategy.messagePrefix() + booking.getBookingCode()
                    + " | Route: " + booking.getRoute()
                    + " | Seats: " + booking.getSeatNumbersText();

            if (!notificationService.existsByBookingIdAndMessage(booking.getId(), message)) {
                Notification notification = new Notification();
                notification.setUserId(booking.getUserId());
                notification.setBookingId(booking.getId());
                notification.setMessage(message);
                notification.setSendTime(LocalDateTime.now());
                notification.setRead(false);
                notificationService.save(notification);
                log.info("[Scheduler] Notification sent for booking {} user {}", booking.getBookingCode(), booking.getUserId());
            }
        }
    }

    private interface ReminderStrategy {
        LocalDateTime from(LocalDateTime now);

        LocalDateTime to(LocalDateTime now);

        String messagePrefix();
    }

    private static class ThreeDayReminderStrategy implements ReminderStrategy {
        @Override
        public LocalDateTime from(LocalDateTime now) {
            return now.plusHours(71);
        }

        @Override
        public LocalDateTime to(LocalDateTime now) {
            return now.plusHours(73);
        }

        @Override
        public String messagePrefix() {
            return "3-Day Reminder: Your trip is in 3 days! Code: ";
        }
    }

    private static class OneDayReminderStrategy implements ReminderStrategy {
        @Override
        public LocalDateTime from(LocalDateTime now) {
            return now.plusHours(23);
        }

        @Override
        public LocalDateTime to(LocalDateTime now) {
            return now.plusHours(25);
        }

        @Override
        public String messagePrefix() {
            return "1-Day Reminder: Your trip is tomorrow! Code: ";
        }
    }

    private static class FiveHourReminderStrategy implements ReminderStrategy {
        @Override
        public LocalDateTime from(LocalDateTime now) {
            return now.plusMinutes(270);
        }

        @Override
        public LocalDateTime to(LocalDateTime now) {
            return now.plusMinutes(330);
        }

        @Override
        public String messagePrefix() {
            return "5-Hour Reminder: Your trip is in 5 hours! Code: ";
        }
    }

    private static class ThreeHourReminderStrategy implements ReminderStrategy {
        @Override
        public LocalDateTime from(LocalDateTime now) {
            return now.plusMinutes(150);
        }

        @Override
        public LocalDateTime to(LocalDateTime now) {
            return now.plusMinutes(210);
        }

        @Override
        public String messagePrefix() {
            return "3-Hour Reminder: Your trip is in 3 hours! Prepare now! Code: ";
        }
    }
}
