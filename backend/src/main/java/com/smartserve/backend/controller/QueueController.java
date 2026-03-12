package com.smartserve.backend.controller;

import com.smartserve.backend.model.QueueEntry;
//import com.smartserve.backend.repository.QueueRepository;

import com.smartserve.backend.repository.QueueRepository;
import com.smartserve.backend.service.QueueService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/queue")
public class QueueController {

    // Top of the class
    @Autowired
    private QueueService queueService; // This is the ONLY one you need now

    // Fix line 26
    @PostMapping("/join")
    public QueueEntry joinQueue(@RequestBody QueueEntry entry) {
        return queueService.joinQueue(entry); // Call the method you actually wrote!
    }

    // Fix line 33
    @GetMapping("/user/{userId}")
    public List<QueueEntry> getUserQueue(@PathVariable Long userId) {
        return queueService.getQueueByUserId(userId); // Forward the request to the Service
    }

    // Fix line 39
    @GetMapping("/all")
    public List<QueueEntry> getAllEntries() {
        return queueService.getAllQueueEntries(); // Let the Service handle the fetch
    }
    @PutMapping("/{id}/status")
    public QueueEntry updateStatus(@PathVariable long id, @RequestParam String status){
        return queueService.updateEntryStatus(id, status);
    }
    @GetMapping("/user/{userId}/wait-time")
    public String getWaitTime(@PathVariable("userId") Long userID){
        int minute=queueService.getEstimatedWaitTime(userID);
        return "Estimated wait time:" + minute + "minutes";
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelToken(@PathVariable Long id){
        queueService.softDeleteEntry(id);
        return ResponseEntity.ok("Token "+ id+ "has been cancelled(Soft Deleted).");
    }
}