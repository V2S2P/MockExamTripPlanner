package app.security;

import jakarta.persistence.EntityManagerFactory;

public class SecuritySeeder {

    public static void seedTestData(EntityManagerFactory emf) {
        SecurityDAO dao = new SecurityDAO(emf);

        try {
            // 1. Ensure roles exist
            try { dao.createRole("User"); } catch (Exception ignored) {}
            try { dao.createRole("Admin"); } catch (Exception ignored) {}

            // 2. Ensure users exist
            try { dao.createUser("Valdemar", "pass12345"); } catch (Exception ignored) {}
            try { dao.createUser("Admin", "pass12345"); } catch (Exception ignored) {}

            // 3. Assign roles (returns updated, managed user)
            dao.addUserRole("Valdemar", "User");
            dao.addUserRole("Admin", "Admin");

            System.out.println("Seeded users and roles successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to seed test data", e);
        }
    }
}
