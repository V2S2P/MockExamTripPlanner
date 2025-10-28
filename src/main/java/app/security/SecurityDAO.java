package app.security;

import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class SecurityDAO implements ISecurityDAO{

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);
            foundUser.getRoles();
            if (foundUser.verifyPassword(password)) {
                return foundUser;
            } else {
                throw new ValidationException("Invalid username or password");
            }
        }
    }

    @Override
    public User createUser(String username, String password) {
        try (EntityManager em = emf.createEntityManager()) {
            User user = new User(username, password);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Role createRole(String roleName) {
        try (EntityManager em = emf.createEntityManager()) {
            Role role = new Role(roleName);
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
            return role;
        }
    }

    @Override
    public User addUserRole(String username, String roleName) throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, roleName);
            if (foundUser == null || foundRole == null) {
                throw new EntityNotFoundException("user and role does not exist");
            }
            em.find(Role.class, roleName);
            em.getTransaction().begin();
            foundUser.addRole(foundRole);
            em.getTransaction().commit();
            return foundUser;
        }
    }
}
