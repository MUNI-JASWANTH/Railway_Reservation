package com.irctc.Service;

import com.irctc.Entities.User;
import com.irctc.localdb.LocalDB;
import com.irctc.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginService {

    private final List<User> usersDb;
    private User loggedInUser = null;

    private final LocalDB<User> userDB;

    public LoginService(List<User> usersDb, LocalDB<User> userDB) {
        this.usersDb = usersDb;
        this.userDB = userDB;
    }

    // Register user
    public boolean register(String name, String email, String password) {

        // Check duplicate email (case-insensitive)
        for (User u : usersDb) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        User newUser = new User();
        newUser.setUserId("U-" + UUID.randomUUID().toString().substring(0, 8));
        newUser.setName(name);
        newUser.setEmail(email);

        // Encrypt password (PasswordUtil.hash(...) expected)
        String hashedPassword = PasswordUtil.hash(password);
        newUser.setPasswordHash(hashedPassword);

        // Initialize empty ticket id list (List<String>)
        newUser.setBookedTickets(new ArrayList<>());

        // Add and save to JSON
        usersDb.add(newUser);
        if (userDB != null) userDB.writeList(usersDb);

        return true;
    }

    // Login using hashed password check
    public boolean login(String email, String password) {
        if (email == null || password == null) return false;

        for (User user : usersDb) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                String storedHash = user.getPasswordHash();
                if (storedHash != null && PasswordUtil.matches(password, storedHash)) {
                    loggedInUser = user;
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // Logout
    public void logout() {
        loggedInUser = null;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
