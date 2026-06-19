package com.gobus.controller;

import com.gobus.entity.Booking;
import com.gobus.entity.Schedule;
import com.gobus.entity.Notification;
import com.gobus.entity.User;
import com.gobus.service.BookingService;
import com.gobus.service.ScheduleService;
import com.gobus.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final ScheduleService scheduleService;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    public UserController(ScheduleService scheduleService, BookingService bookingService, NotificationService notificationService) {
        this.scheduleService = scheduleService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    private User requireUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return null;
        return user;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        List<Booking> myBookings = bookingService.findByUserId(user.getId());
        long unread = notificationService.countUnread(user.getId());
        model.addAttribute("myBookings", myBookings);
        model.addAttribute("unreadCount", unread);
        return "user/dashboard";
    }

    // ==================== SCHEDULE ====================

    @GetMapping("/schedule")
    public String scheduleList(@RequestParam(required = false) String busName,
                             @RequestParam(required = false) String destination,
                             HttpSession session,
                             Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("scheduleList", scheduleService.search(busName, destination));
        model.addAttribute("busName", busName);
        model.addAttribute("destination", destination);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "user/schedule-list";
    }

    // ==================== SEAT MAP ====================

    @GetMapping("/seat-map")
    public String seatMap(@RequestParam Long scheduleId, HttpSession session, Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) return "redirect:/user/schedule";
        List<Integer> bookedSeats = bookingService.getBookedSeats(scheduleId);
        model.addAttribute("schedule", schedule);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "user/seat-map";
    }

    // ==================== BOOKING ====================

    @PostMapping("/booking/create")
    public String createBooking(@RequestParam Long scheduleId,
                                @RequestParam String seatNumbers,
                                HttpSession session,
                                RedirectAttributes ra) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        try {
            List<Integer> selectedSeats = parseSeatNumbers(seatNumbers);
            List<Long> bookingIds = bookingService.createBookings(user.getId(), scheduleId, selectedSeats);
            return "redirect:/user/payment?bookingId=" + bookingIds.get(0);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create booking: " + e.getMessage());
            return "redirect:/user/seat-map?scheduleId=" + scheduleId;
        }
    }

    // ==================== PAYMENT ====================

    @GetMapping("/payment")
    public String paymentPage(@RequestParam Long bookingId, HttpSession session, Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        Booking booking = bookingService.findById(bookingId);
        if (booking == null || !booking.getUserId().equals(user.getId())) return "redirect:/user/dashboard";
        if ("PAID".equals(booking.getPaymentStatus())) return "redirect:/user/booking-success?bookingId=" + bookingId;
        List<Booking> bookingGroup = bookingService.findBookingGroup(bookingId);
        model.addAttribute("booking", booking);
        model.addAttribute("bookingGroup", bookingGroup);
        model.addAttribute("totalPrice", calculateTotalPrice(bookingGroup));
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "user/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam Long bookingId, HttpSession session, RedirectAttributes ra) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        try {
            bookingService.pay(bookingId);
            return "redirect:/user/booking-success?bookingId=" + bookingId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to process payment: " + e.getMessage());
            return "redirect:/user/payment?bookingId=" + bookingId;
        }
    }

    // ==================== BOOKING SUCCESS ====================

    @GetMapping("/booking-success")
    public String bookingSuccess(@RequestParam Long bookingId, HttpSession session, Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        Booking booking = bookingService.findById(bookingId);
        if (booking == null || !booking.getUserId().equals(user.getId())) return "redirect:/user/dashboard";
        List<Booking> bookingGroup = bookingService.findBookingGroup(bookingId);
        model.addAttribute("booking", booking);
        model.addAttribute("bookingGroup", bookingGroup);
        model.addAttribute("totalPrice", calculateTotalPrice(bookingGroup));
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "user/booking-success";
    }

    // ==================== NOTIFICATIONS ====================

    @GetMapping("/notifications")
    public String notificationPage(HttpSession session, Model model) {
        User user = requireUser(session);
        if (user == null) return "redirect:/login";
        notificationService.markAllRead(user.getId());
        List<Notification> notificationList = notificationService.findByUserId(user.getId());
        model.addAttribute("notificationList", notificationList);
        model.addAttribute("unreadCount", 0L);
        return "user/notifications";
    }

    // ==================== NOTIFICATION COUNT (AJAX) ====================

    @GetMapping("/notifications/count")
    @ResponseBody
    public long notificationCount(HttpSession session) {
        User user = requireUser(session);
        if (user == null) return 0;
        return notificationService.countUnread(user.getId());
    }

    private List<Integer> parseSeatNumbers(String seatNumbers) {
        List<Integer> seats = new ArrayList<>();
        if (seatNumbers == null || seatNumbers.trim().isEmpty()) {
            return seats;
        }

        for (String value : seatNumbers.split(",")) {
            if (!value.trim().isEmpty()) {
                seats.add(Integer.parseInt(value.trim()));
            }
        }
        return seats;
    }

    private BigDecimal calculateTotalPrice(List<Booking> bookingGroup) {
        BigDecimal total = BigDecimal.ZERO;
        for (Booking booking : bookingGroup) {
            total = total.add(booking.getPrice().multiply(BigDecimal.valueOf(booking.getSeatCount())));
        }
        return total;
    }
}
