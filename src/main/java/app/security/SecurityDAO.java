package app.security;

import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

public class SecurityDAO implements ISecurityDAO {

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try (var em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);

            if (foundUser == null) {
                throw new ValidationException("Invalid username or password");
            }

            // Merge to ensure managed (optional but safe)
            foundUser = em.merge(foundUser);

            // Eagerly fetch roles to avoid LazyInitializationException
            foundUser.getRoles().size();

            if (!foundUser.verifyPassword(password)) {
                throw new ValidationException("Invalid username or password");
            }

            return foundUser;
        }
    }

    @Override
    public User createUser(String username, String password) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            User user = new User(username, password);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Role createRole(String roleName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Role role = new Role(roleName);
            em.persist(role);
            em.getTransaction().commit();
            return role;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public User addUserRole(String username, String roleName) throws EntityNotFoundException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, roleName);

            if (foundUser == null || foundRole == null) {
                throw new EntityNotFoundException("User or role does not exist");
            }

            foundUser.addRole(foundRole);
            em.getTransaction().commit();

            return foundUser;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Optional helper to fetch a role by name
    public Role getRoleByName(String roleName) throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            Role role = em.find(Role.class, roleName);
            if (role == null) {
                throw new EntityNotFoundException("Role not found: " + roleName);
            }
            return role;
        }
    }

    // Optional helper to fetch a user by username
    public User getUserByName(String username) throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null) {
                throw new EntityNotFoundException("User not found: " + username);
            }
            // Eagerly fetch roles
            user.getRoles().size();
            return user;
        }
    }
}
