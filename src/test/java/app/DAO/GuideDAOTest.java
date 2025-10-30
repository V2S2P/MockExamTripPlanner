package app.DAO;

import app.config.HibernateConfig;
import app.daos.GuideDAO;
import app.entities.Guide;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GuideDAOTest {

    private static EntityManagerFactory emf;
    private GuideDAO guideDAO;
    private Guide initialGuide; // will hold the persisted entity for all tests

    @BeforeAll
    void setupAll() {
        // 1️⃣ Use a single EMF for all tests
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        guideDAO = new GuideDAO(emf);

        // 2️⃣ Clear the Guide table
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Guide").executeUpdate();
            em.getTransaction().commit();
        }

        // 3️⃣ Persist initial guide and store the entity
        initialGuide = new Guide();
        initialGuide.setName("John Doe");
        initialGuide.setEmail("john.doe@example.com");
        initialGuide.setPhoneNumber("12345678");
        initialGuide.setYearsOfExperience(5);

        initialGuide = guideDAO.create(initialGuide);

        Assertions.assertNotNull(initialGuide.getId(), "Initial guide should have an ID after creation");
    }

    @Test
    void testGetAllGuides() {
        List<Guide> guides = guideDAO.getAll();
        assertThat(guides, is(not(empty())));
        assertThat(guides.get(0).getId(), equalTo(initialGuide.getId()));
    }

    @Test
    void testGetGuideById() {
        Guide found = guideDAO.getById(initialGuide.getId());
        assertThat(found, is(notNullValue()));
        assertThat(found.getName(), equalTo(initialGuide.getName()));
    }

    @Test
    void testCreateGuide() {
        Guide newGuide = new Guide();
        newGuide.setName("Jane Smith");
        newGuide.setEmail("jane.smith@example.com");
        newGuide.setPhoneNumber("87654321");
        newGuide.setYearsOfExperience(3);

        Guide created = guideDAO.create(newGuide);
        assertThat(created.getId(), is(notNullValue()));
        assertThat(created.getName(), equalTo("Jane Smith"));
    }

    @Test
    void testUpdateGuide() {
        initialGuide.setName("Updated Name");
        initialGuide.setEmail("updated.email@example.com");

        Guide updated = guideDAO.update(initialGuide.getId(), initialGuide);
        assertThat(updated.getName(), equalTo("Updated Name"));
        assertThat(updated.getEmail(), equalTo("updated.email@example.com"));
    }

    @Test
    void testDeleteGuide() {
        // Create a separate guide for deletion test
        Guide guideToDelete = new Guide();
        guideToDelete.setName("To Delete");
        guideToDelete.setEmail("delete@example.com");
        guideToDelete.setPhoneNumber("00000000");
        guideToDelete.setYearsOfExperience(1);

        Guide createdGuide = guideDAO.create(guideToDelete);
        final int idToDelete = createdGuide.getId(); // final variable for lambda

        boolean deleted = guideDAO.deleteById(idToDelete);
        assertThat(deleted, is(true));

        Assertions.assertThrows(ApiException.class, () -> guideDAO.getById(idToDelete));
    }
}
