package com.smartserve.backend.repository;

import com.smartserve.backend.model.User; // This must match your model filename
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Fixed: changed 'user' to 'User'
    Optional<User> findByEmail(String email);
}