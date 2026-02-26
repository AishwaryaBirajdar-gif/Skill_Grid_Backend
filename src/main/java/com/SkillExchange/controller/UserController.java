package com.SkillExchange.controller;

import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.User;
import com.SkillExchange.service.BaseUserService;
import com.SkillExchange.service.UserService;
import com.SkillExchange.repository.BaseUserRepository;
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

    @Autowired
    private BaseUserRepository baseUserRepository;

    // ---------------- GET USER BY ID ----------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return baseUserService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- UPDATE USER ----------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody BaseUser userDetails) {
        return baseUserService.findById(id).map(existingUser -> {
            existingUser.setName(userDetails.getName());
            existingUser.setBio(userDetails.getBio());
            existingUser.setLocation(userDetails.getLocation());
            existingUser.setSkillsOffered(userDetails.getSkillsOffered());
            existingUser.setSkillsWanted(userDetails.getSkillsWanted());
            
            existingUser.refreshCounts();
            
            BaseUser savedUser = baseUserRepository.save(existingUser);
            return ResponseEntity.ok(savedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ---------------- SEARCH / BROWSE SKILLS (Updated for Barter) ----------------
    @GetMapping("/browse")
    public ResponseEntity<List<BaseUser>> browseUsers(@RequestParam String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return ResponseEntity.ok(baseUserRepository.findAll());
        }
        // ✅ Uses the case-insensitive search we just added to the repository
        List<BaseUser> users = baseUserRepository.findBySkillsOfferedContainingIgnoreCase(skill);
        return ResponseEntity.ok(users);
    }

    // ---------------- CREATE USER ----------------
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User userDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); 

        BaseUser baseUser = baseUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("BaseUser not found"));

        User createdUser = userService.createUser(baseUser, userDetails);
        return ResponseEntity.ok(createdUser);
    }

    // ---------------- GET ALL USERS ----------------
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ---------------- DELETE USER ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}