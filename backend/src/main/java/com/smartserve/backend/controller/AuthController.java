package com.smartserve.backend.controller;

import com.smartserve.backend.repository.UserRepository;
import com.smartserve.backend.model.User;
import com.smartserve.backend.security.JwtFilter;
import com.smartserve.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController   /// this for the rest api request
@RequestMapping("/api/auth")  /// this is for the base address

@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    /// this is for the connection with the controller
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) { //  grab the data from the flutter app
        if (user.getFullName() == null || user.getEmail() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name or Email is missing!"));
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This email is already registered. Please login."));
        }
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(
                saved.getEmail(),
                saved.getId(),
                saved.getRole() != null ? saved.getRole() : "STUDENT"
        );


        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", saved.getId(),
                "email", saved.getEmail(),
                "fullName", saved.getFullName() != null ? saved.getFullName() : "",
                "role", saved.getRole() != null ? saved.getRole() : "STUDENT",
                "message", "Registration successful"

        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required."));
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "No account found with this email."));
        }

        User user = userOptional.get();

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Incorrect password."));
        }
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole() != null ? user.getRole() : "STUDENT"
        );

        return ResponseEntity.ok(Map.of(
                "token", token,      // ← THE JWT TOKEN — Flutter saves this
                "userId", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "role", user.getRole() != null ? user.getRole() : "STUDENT",
                "message", "Login successful"
        ));
    }

    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public void setEmail(String e) {
            this.email = e;
        }

        public void setPassword(String p) {
            this.password = p;
        }
    }
}

