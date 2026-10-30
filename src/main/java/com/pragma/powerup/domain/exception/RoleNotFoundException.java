package com.pragma.powerup.domain.exception;

public class RoleNotFoundException extends DomainException {
    public RoleNotFoundException() {
        super("The requested role does not exist");
    }
}
