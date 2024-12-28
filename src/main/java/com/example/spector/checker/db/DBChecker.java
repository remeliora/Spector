package com.example.spector.checker.db;

public interface DBChecker {
    boolean isAccessible(int retryCount);
}
