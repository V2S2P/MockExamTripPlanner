package app.security;

import app.config.HibernateConfig;
import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;

public class Main {
    public static void main(String[] args) {
        // TODO: Make this more clean: maybe a loader?

        // FIXME Der skal laves en user først med create. Ændre til update

        ISecurityDAO dao = new SecurityDAO(HibernateConfig.getEntityManagerFactory());

        User user = dao.createUser("Valdemar", "pass12345");
        System.out.println(user.getUserName()+": "+user.getPassword());
        Role role = dao.createRole("User");

        User admin = dao.createUser("Admin", "pass12345");
        System.out.println(admin.getUserName()+": "+admin.getPassword());
        Role roleAdmin = dao.createRole("Admin");

        try {
            User updatedUser = dao.addUserRole("Valdemar", "User");
            System.out.println(updatedUser);
            User updatedAdmin = dao.addUserRole("Admin", "Admin");
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        try {
            User validatedUser = dao.getVerifiedUser("Valdemar", "pass12345");
            System.out.println("User was validated: "+validatedUser.getUserName());
            User validatedAdmin = dao.getVerifiedUser("Admin", "pass12345");
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
