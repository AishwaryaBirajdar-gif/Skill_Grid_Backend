package com.SkillExchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.SkillExchange.auth.JwtUtil;
import com.SkillExchange.auth.LoginRequest;
import com.SkillExchange.model.BaseUser;
import com.SkillExchange.model.JwtResponse;
import com.SkillExchange.model.User;
import com.SkillExchange.repository.UserRepository;
import com.SkillExchange.repository.BaseUserRepository;
import com.SkillExchange.service.BaseUserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BaseUserRepository baseuserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BaseUserService baseuserService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody BaseUser baseuser) {
        if (baseuserRepository.findByEmail(baseuser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use.");
        }

        // Encode the password
        baseuser.setPassword(passwordEncoder.encode(baseuser.getPassword()));

        // Set default role and status
        if (baseuser.getRole() == null) {
            baseuser.setRole(BaseUser.Role.USER);
        }
        baseuser.setStatus("ACTIVE"); 
        
        // Add CreatedAt timestamp for your CSV Report
        baseuser.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        // Save the user
        BaseUser savedBaseUser = baseuserRepository.save(baseuser);

        // CREATE USER PROFILE DOCUMENT
        User user = new User(savedBaseUser);
        user.setName(savedBaseUser.getName());
        user.setEmail(savedBaseUser.getEmail());
        userRepository.save(user);
        
        // Generate JWT token
        String role = baseuser.getRole().name();
        String token = jwtUtil.generateToken(baseuser.getEmail(), role, baseuser.getId());

        JwtResponse response = new JwtResponse(
                token,
                baseuser.getId(),
                baseuser.getEmail(),
                baseuser.getName(),
                role
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            BaseUser user = baseuserRepository.findByEmail(userDetails.getUsername())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // --- BAN CHECK LOGIC ---
            if ("BANNED".equals(user.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                     .body("Account is banned. Please contact support.");
            }

            // Extract role
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), role, user.getId());

            JwtResponse response = new JwtResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    role
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
}