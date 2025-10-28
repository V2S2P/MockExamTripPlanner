package app.security;

import javax.management.relation.Role;

public interface ISecurityUser {
    boolean verifyPassword(String pw);
    void addRole(Role role);

    void addRole(app.entities.Role role);

    void removeRole(String role);
}
