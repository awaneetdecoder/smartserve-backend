package com.smartserve.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "queue_entries")


public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private  String tokenNumber;
    private String tokenType;

    private String status;
    private boolean isDeleted= false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne     //one user is for many token
    @JoinColumn(name = "user_id")   // this is for the foreign key
    private User user;
}
