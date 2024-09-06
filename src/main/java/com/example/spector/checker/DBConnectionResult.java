package com.example.spector.checker;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DBConnectionResult {
    private final boolean success;
    private final String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DBConnectionResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
