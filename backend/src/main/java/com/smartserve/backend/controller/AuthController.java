package com.smartserve.backend.controller;

import com.smartserve.backend.repository.UserRepository;
import com.smartserve.backend.model.User;
import com.smartserve.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;  // lowercase u — the injected object

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password required"));
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This email is already registered. Please login."));
        }

        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);

        User saved = userRepository.save(user);  // Bug 1 fixed — lowercase u

        String token = jwtUtil.generateToken(
                saved.getEmail(),
                saved.getId(),
                saved.getRole()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", saved.getId(),
                "role", saved.getRole()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) { // Bug 4 fixed
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required."));
        }

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail()); // Bug 2 fixed

        if (userOptional.isEmpty()) {   // Bug 3 fixed — consistent name
            return ResponseEntity.status(401)
                    .body(Map.of("error", "No account found with this email."));
        }

        User user = userOptional.get(); // Bug 3 fixed

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid password"));
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "role", user.getRole()
        ));
    }

    // Inner class for login — only email + password needed
    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setEmail(String e) { this.email = e; }
        public void setPassword(String p) { this.password = p; }
    }
}