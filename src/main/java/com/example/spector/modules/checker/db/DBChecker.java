package com.example.spector.modules.checker.db;

public interface DBChecker {
    boolean isAccessible(int retryCount);
}
