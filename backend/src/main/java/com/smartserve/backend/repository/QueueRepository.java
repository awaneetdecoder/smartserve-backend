package com.smartserve.backend.repository;

import com.smartserve.backend.model.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<QueueEntry, Long> {

    // Change findByUserId to findByUser_Id (The underscore tells JPA to look inside the User object for the ID)
    List<QueueEntry> findByUser_Id(Long userId);
}
