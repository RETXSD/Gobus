package com.gobus.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gobus.entity.User;
import com.gobus.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private static final String REMEMBER_EMAIL_COOKIE = "gobus_remember_email";
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(HttpSession session,
                        @CookieValue(value = REMEMBER_EMAIL_COOKIE, required = false) String rememberedEmail) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = restoreSessionFromRememberCookie(session, rememberedEmail);
        }
        if (user != null) {
            return user.getRole().equals("ADMIN") ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session,
                            Model model,
                            @CookieValue(value = REMEMBER_EMAIL_COOKIE, required = false) String rememberedEmail) {
        if (session.getAttribute("user") != null) return "redirect:/";
        User rememberedUser = restoreSessionFromRememberCookie(session, rememberedEmail);
        if (rememberedUser != null) return "redirect:/";
        model.addAttribute("rememberedEmail", rememberedEmail);
        model.addAttribute("rememberMe", rememberedEmail != null && !rememberedEmail.isBlank());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(required = false) String rememberMe,
                        HttpSession session,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        User user = userService.login(email, password);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password!");
            return "redirect:/login";
        }

        session.setAttribute("user", user);
        if (rememberMe != null) {
            addRememberCookie(response, email);
        } else {
            deleteRememberCookie(response);
        }

        return user.getRole().equals("ADMIN") ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> apiLogin(@RequestBody Map<String, Object> payload,
                                        HttpSession session,
                                        HttpServletResponse response) {
        String email = String.valueOf(payload.getOrDefault("email", ""));
        String password = String.valueOf(payload.getOrDefault("password", ""));
        boolean rememberMe = Boolean.TRUE.equals(payload.get("rememberMe"));

        User user = userService.login(email, password);
        if (user == null) {
            return Map.of(
                    "code", 401,
                    "message", "Invalid email or password!"
            );
        }

        session.setAttribute("user", user);
        if (rememberMe) {
            addRememberCookie(response, email);
        } else {
            deleteRememberCookie(response);
        }

        String redirectUrl = user.getRole().equals("ADMIN") ? "/admin/dashboard" : "/user/dashboard";
        return Map.of(
                "code", 200,
                "message", "Login success",
                "redirectUrl", redirectUrl
        );
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/";
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        boolean success = userService.register(name, email, password);
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Email is already registered!");
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        deleteRememberCookie(response);
        return "redirect:/login";
    }

    private void addRememberCookie(HttpServletResponse response, String email) {
        Cookie cookie = new Cookie(REMEMBER_EMAIL_COOKIE, email);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 3);
        response.addCookie(cookie);
    }

    private void deleteRememberCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_EMAIL_COOKIE, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private User restoreSessionFromRememberCookie(HttpSession session, String rememberedEmail) {
        if (rememberedEmail == null || rememberedEmail.isBlank()) {
            return null;
        }

        User user = userService.findByEmail(rememberedEmail);
        if (user != null) {
            session.setAttribute("user", user);
        }
        return user;
    }
}
