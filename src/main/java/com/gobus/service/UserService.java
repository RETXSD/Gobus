package com.gobus.service;

import com.gobus.dao.UserDAO;
import com.gobus.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean register(String name, String email, String password) {
        if (userDAO.existsByEmail(email)) return false;
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // plain text as per spec (no Spring Security)
        user.setRole("USER");
        userDAO.save(user);
        return true;
    }

    public User findById(Long id) {
        return userDAO.findById(id);
    }

    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }
}
