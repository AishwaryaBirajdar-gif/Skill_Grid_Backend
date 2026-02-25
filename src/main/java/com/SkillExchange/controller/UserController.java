package com.SkillExchange.controller;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.User;
import com.SkillExchange.service.BaseUserService;
import com.SkillExchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private UserService userService;

    // ---------------- CREATE USER ----------------
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User userDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Get email from JWT

        BaseUser baseUser = baseUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("BaseUser not found"));

        User createdUser = userService.createUser(baseUser, userDetails);
        return ResponseEntity.ok(createdUser);
    }

    // ---------------- GET USER BY ID ----------------
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ---------------- GET ALL USERS ----------------
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ---------------- UPDATE USER ----------------
 // UserController.java
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userDetails) {
        // This will now receive the name, bio, skillsOffered, and skillsWanted
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    // ---------------- DELETE USER ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
    
 // Add this to your UserController.java

    @GetMapping("/browse")
    public ResponseEntity<List<User>> browseUsers(@RequestParam String skill) {
        List<User> users = userService.searchUsersBySkill(skill);
        return ResponseEntity.ok(users);
    }
}
