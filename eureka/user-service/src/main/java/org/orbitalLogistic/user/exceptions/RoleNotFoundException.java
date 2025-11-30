package org.orbitalLogistic.user.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
    
    public RoleNotFoundException(Long id) {
        super("Role not found with id: " + id);
    }
    
    public RoleNotFoundException(String name, boolean byName) {
        super("Role not found with name: " + name);
    }
}
