package com.smartserve.backend.service;

import com.smartserve.backend.model.QueueEntry;
import com.smartserve.backend.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {
    @Autowired
    private QueueRepository queueRepository;

    public QueueEntry joinQueue(QueueEntry entry) {
        // 1. Safety Check: Does the user even exist in the request?
        if(entry.getUser() ==null|| entry.getUser().getId()==null){
            throw new RuntimeException("Invalid User: ID is required to join the queue.");
        }
        //The Check (Simplified and safer)
        Long newUserId = entry.getUser().getId();
        boolean isAlreadyInQueue = queueRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getStatus() != null)
                .anyMatch(e -> e.getUser().getId().equals(newUserId)
                        && e.getStatus().equalsIgnoreCase("WAITING"));

        if(isAlreadyInQueue){
            throw new RuntimeException("You are already in the queue!");
        }
        long count= queueRepository.count();
        String nextToken ="T-" +(100 +count+1);
        entry.setTokenNumber(nextToken);
        entry.setStatus("waiting");
        return queueRepository.save(entry);
    }

    public List<QueueEntry> getQueueByUserId(Long userId) {
        return queueRepository.findByUser_Id(userId);

    }

    public List<QueueEntry> getAllQueueEntries(){
        return queueRepository.findAll();
    }

    public QueueEntry updateEntryStatus(Long id, String newStatus){
        QueueEntry entry =queueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found with id:"+ id));
        entry.setStatus(newStatus.toUpperCase());
        return  queueRepository.save(entry);
    }

    public int getEstimatedWaitTime(Long userId){
        List<QueueEntry> waitingQueue = queueRepository.findAll().stream().filter(e-> e.getStatus().equalsIgnoreCase("WAITING")).toList();

        int position=0;
        for(int i=0; i<waitingQueue.size();i++){
            if(waitingQueue.get(i).getUser().getId().equals(userId)){
                position =i;
                break;
            }
        }
        return position* 5;
    }
    public void cancelEntry(Long id){
        if(!queueRepository.existsById(id)){
            throw new RuntimeException("Cannot cancel: Token ID" +id +"not found.");

        }
        queueRepository.deleteById(id);
    }
    public void softDeleteEntry(Long id){
        QueueEntry entry =queueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token not found for deletion!"));

        entry.setDeleted(true);
        entry.setStatus("CANCELLED");

        queueRepository.save(entry);
    }

    public List<QueueEntry> getAllActiveQueue(){
        return queueRepository.findAll().stream().filter(
                e -> e.isDeleted())
                .toList();

    }
}
