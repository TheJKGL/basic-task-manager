package com.taskmanager.exception;

public class UnmodifiedException extends RuntimeException {
    public UnmodifiedException(String message) {
        super(message);
    }
}
