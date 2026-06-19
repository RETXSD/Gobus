package com.gobus.config;

import com.gobus.entity.User;
import com.gobus.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String REMEMBER_EMAIL_COOKIE = "gobus_remember_email";
    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (isWhitelisted(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            user = restoreUserFromRememberCookie(request);
            if (user != null) {
                session = request.getSession(true);
                session.setAttribute("user", user);
            }
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (path.startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/user/jadwal");
            return false;
        }

        if (path.startsWith("/user") && !"USER".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return false;
        }

        return true;
    }

    private boolean isWhitelisted(String path) {
        return path.equals("/")
                || path.equals("/login")
                || path.equals("/api/login")
                || path.equals("/register")
                || path.equals("/error")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.startsWith("/favicon");
    }

    private User restoreUserFromRememberCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (REMEMBER_EMAIL_COOKIE.equals(cookie.getName())) {
                String email = cookie.getValue();
                if (email != null && !email.isBlank()) {
                    return userService.findByEmail(email);
                }
            }
        }
        return null;
    }
}
