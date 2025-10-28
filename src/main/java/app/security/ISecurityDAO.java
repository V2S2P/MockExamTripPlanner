
package app.security;

import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException; // used for login
    User createUser(String username, String password); // used for register
    Role createRole(String role);
    User addUserRole(String username, String roleName) throws EntityNotFoundException;

}