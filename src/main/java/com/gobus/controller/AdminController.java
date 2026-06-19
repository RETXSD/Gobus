package com.gobus.controller;

import com.gobus.entity.Booking;
import com.gobus.entity.Bus;
import com.gobus.entity.Jadwal;
import com.gobus.entity.User;
import com.gobus.service.BookingService;
import com.gobus.service.BusService;
import com.gobus.service.JadwalService;
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
    private final JadwalService jadwalService;
    private final BookingService bookingService;

    public AdminController(BusService busService, JadwalService jadwalService, BookingService bookingService) {
        this.busService = busService;
        this.jadwalService = jadwalService;
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
        model.addAttribute("jadwalList", jadwalService.findAll());
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

    // ==================== JADWAL ====================

    @GetMapping("/jadwal/form")
    public String jadwalForm(@RequestParam(required = false) Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Jadwal jadwal = (id != null) ? jadwalService.findById(id) : new Jadwal();
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("busList", busService.findAll());
        return "admin/jadwal-form";
    }

    @PostMapping("/jadwal/save")
    public String jadwalSave(@RequestParam(required = false) Long id,
                             @RequestParam Long busId,
                             @RequestParam String route,
                             @RequestParam String departureTime,
                             @RequestParam BigDecimal price,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Jadwal jadwal = new Jadwal();
        jadwal.setId(id);
        jadwal.setBusId(busId);
        jadwal.setRoute(route);
        jadwal.setDepartureTime(LocalDateTime.parse(departureTime));
        jadwal.setPrice(price);
        if (id != null) {
            jadwalService.update(jadwal);
            ra.addFlashAttribute("success", "Schedule updated successfully!");
        } else {
            jadwalService.save(jadwal);
            ra.addFlashAttribute("success", "Schedule added successfully!");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/jadwal/delete")
    public String jadwalDelete(@RequestParam Long id, HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        jadwalService.delete(id);
        ra.addFlashAttribute("success", "Schedule deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    // ==================== TIKET / CEK PERJALANAN ====================

    @GetMapping("/tiket")
    public String tiketList(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        List<Jadwal> jadwalList = jadwalService.findAll();
        model.addAttribute("jadwalList", jadwalList);
        model.addAttribute("now", LocalDateTime.now());
        return "admin/tiket-list";
    }

    @RequestMapping(value = "/api/trips/{id}", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> deleteExpiredTrip(@PathVariable Long id, HttpSession session) {
        if (requireAdmin(session) == null) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        boolean deleted = jadwalService.deleteIfExpired(id);
        if (!deleted) {
            return Map.of("success", false, "message", "Only departed trips can be deleted.");
        }

        return Map.of("success", true, "message", "Departed trip deleted successfully.");
    }

    @GetMapping("/tiket/detail")
    public String tiketDetail(@RequestParam Long scheduleId,
                              @RequestParam(required = false) String bookingCode,
                              HttpSession session,
                              Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Jadwal jadwal = jadwalService.findById(scheduleId);
        List<Booking> allBookings = bookingService.findByScheduleId(scheduleId);
        List<Booking> bookings = bookingService.searchByScheduleIdAndBookingCode(scheduleId, bookingCode);
        List<Integer> bookedSeatNumbers = new ArrayList<>();
        for (Booking booking : allBookings) {
            bookedSeatNumbers.addAll(booking.getSeatNumbers());
        }
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("bookings", bookings);
        model.addAttribute("bookedSeatNumbers", bookedSeatNumbers);
        model.addAttribute("bookingCode", bookingCode);
        return "admin/tiket-detail";
    }
}
