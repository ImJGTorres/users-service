package com.pragma.powerup.domain.exception;

public class UnderageUserException extends DomainException {
    public UnderageUserException() {
        super("The user must be of legal age");
    }
}
