package org.orbitalLogistic.user.exceptions;

public class UserAlreadyAssignedException extends RuntimeException {
    
    public UserAlreadyAssignedException(String message) {
        super(message);
    }

    public UserAlreadyAssignedException(Long missionId, Long userId) {
        super(String.format("User with ID %d is already assigned to mission with ID %d", userId, missionId));
    }

    public UserAlreadyAssignedException(Long missionId, Long userId, String userName) {
        super(String.format("User '%s' (ID: %d) is already assigned to mission with ID %d", userName, userId, missionId));
    }
}