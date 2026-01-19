package me.ihqqq.marriage.storage;

import me.ihqqq.marriage.model.MarriageRecord;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface Storage {

    
    CompletableFuture<Void> init();

    
    CompletableFuture<Void> saveMarriage(MarriageRecord record);

    
    CompletableFuture<Void> deleteMarriage(UUID player);

    
    CompletableFuture<MarriageRecord> findByPlayer(UUID player);

    
    void close();
}