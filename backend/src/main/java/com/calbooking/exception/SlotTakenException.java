package com.calbooking.exception;

public class SlotTakenException extends RuntimeException {
    public SlotTakenException(String message) {
        super(message);
    }
}
