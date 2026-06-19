package com.gobus.controller;

import com.gobus.entity.Booking;
import com.gobus.entity.Bus;
import com.gobus.entity.Schedule;
import com.gobus.entity.User;
import com.gobus.service.BookingService;
import com.gobus.service.BusService;
import com.gobus.service.ScheduleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BusService busService;
    private final ScheduleService scheduleService;
    private final BookingService bookingService;

    public AdminController(BusService busService, ScheduleService scheduleService, BookingService bookingService) {
        this.busService = busService;
        this.scheduleService = scheduleService;
        this.bookingService = bookingService;
    }

    private User requireAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) return null;
        return user;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("busList", busService.findAll());
        model.addAttribute("scheduleList", scheduleService.findAll());
        model.addAttribute("bookingList", bookingService.findAll());
        return "admin/dashboard";
    }

    // ==================== BUS ====================

    @GetMapping("/bus/form")
    public String busForm(@RequestParam(required = false) Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Bus bus = (id != null) ? busService.findById(id) : new Bus();
        model.addAttribute("bus", bus);
        return "admin/bus-form";
    }

    @PostMapping("/bus/save")
    public String busSave(@RequestParam(required = false) Long id,
                          @RequestParam String brand,
                          @RequestParam String plateNumber,
                          @RequestParam int totalSeats,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Bus bus = new Bus();
        bus.setId(id);
        bus.setBrand(brand);
        bus.setPlateNumber(plateNumber);
        bus.setTotalSeats(totalSeats);
        if (id != null) {
            busService.update(bus);
            ra.addFlashAttribute("success", "Bus updated successfully!");
        } else {
            busService.save(bus);
            ra.addFlashAttribute("success", "Bus added successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/bus/delete")
    public String busDelete(@RequestParam Long id, HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        busService.delete(id);
        ra.addFlashAttribute("success", "Bus deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    // ==================== SCHEDULE ====================

    @GetMapping("/schedule/form")
    public String scheduleForm(@RequestParam(required = false) Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Schedule schedule = (id != null) ? scheduleService.findById(id) : new Schedule();
        model.addAttribute("schedule", schedule);
        model.addAttribute("busList", busService.findAll());
        return "admin/schedule-form";
    }

    @PostMapping("/schedule/save")
    public String scheduleSave(@RequestParam(required = false) Long id,
                             @RequestParam Long busId,
                             @RequestParam String route,
                             @RequestParam String departureTime,
                             @RequestParam BigDecimal price,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Schedule schedule = new Schedule();
        schedule.setId(id);
        schedule.setBusId(busId);
        schedule.setRoute(route);
        schedule.setDepartureTime(LocalDateTime.parse(departureTime));
        schedule.setPrice(price);
        if (id != null) {
            scheduleService.update(schedule);
            ra.addFlashAttribute("success", "Schedule updated successfully!");
        } else {
            scheduleService.save(schedule);
            ra.addFlashAttribute("success", "Schedule added successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/schedule/delete")
    public String scheduleDelete(@RequestParam Long id, HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        scheduleService.delete(id);
        ra.addFlashAttribute("success", "Schedule deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    // ==================== TRIPS ====================

    @GetMapping("/trips")
    public String tripList(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        List<Schedule> scheduleList = scheduleService.findAll();
        model.addAttribute("scheduleList", scheduleList);
        model.addAttribute("now", LocalDateTime.now());
        return "admin/trip-list";
    }

    @RequestMapping(value = "/api/trips/{id}", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> deleteExpiredTrip(@PathVariable Long id, HttpSession session) {
        if (requireAdmin(session) == null) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        boolean deleted = scheduleService.deleteIfExpired(id);
        if (!deleted) {
            return Map.of("success", false, "message", "Only departed trips can be deleted.");
        }

        return Map.of("success", true, "message", "Departed trip deleted successfully.");
    }

    @GetMapping("/trips/detail")
    public String tripDetail(@RequestParam Long scheduleId,
                              @RequestParam(required = false) String bookingCode,
                              HttpSession session,
                              Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Schedule schedule = scheduleService.findById(scheduleId);
        List<Booking> allBookings = bookingService.findByScheduleId(scheduleId);
        List<Booking> bookings = bookingService.searchByScheduleIdAndBookingCode(scheduleId, bookingCode);
        List<Integer> bookedSeatNumbers = new ArrayList<>();
        for (Booking booking : allBookings) {
            bookedSeatNumbers.addAll(booking.getSeatNumbers());
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("bookings", bookings);
        model.addAttribute("bookedSeatNumbers", bookedSeatNumbers);
        model.addAttribute("bookingCode", bookingCode);
        return "admin/trip-detail";
    }
}
