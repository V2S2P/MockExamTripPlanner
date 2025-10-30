package app.DAO;

import app.config.HibernateConfig;
import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.entities.Guide;
import app.entities.Trip;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TripDAOTest {

    private static EntityManagerFactory emf;
    private TripDAO tripDAO;
    private GuideDAO guideDAO;
    private Guide testGuide;

    @BeforeAll
    void setupAll() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        guideDAO = new GuideDAO(emf);
        tripDAO = new TripDAO(emf);

        // TRUNCATE tables before tests
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Trip").executeUpdate();
            em.createQuery("DELETE FROM Guide").executeUpdate();
            em.getTransaction().commit();
        }

        // Create a test guide
        testGuide = new Guide();
        testGuide.setName("John Doe");
        testGuide.setEmail("john.doe@example.com");
        testGuide.setPhoneNumber("12345678");
        testGuide.setYearsOfExperience(5);
        testGuide = guideDAO.create(testGuide);

        // Create a test trip with guide (truncate nanoseconds)
        Trip initialTrip = new Trip();
        initialTrip.setName("Adventure Trip");
        initialTrip.setStartTime(LocalTime.now().withNano(0));
        initialTrip.setEndTime(LocalTime.now().withNano(0));
        initialTrip.setLatitude(55.6761);
        initialTrip.setLongitude(12.5683);
        initialTrip.setPrice(100);
        initialTrip.setGuide(testGuide);
        tripDAO.create(initialTrip);
    }

    @Test
    void testGetAllTrips() {
        List<Trip> trips = tripDAO.getAll();
        assertThat(trips, is(not(empty())));
        assertThat(trips.get(0).getGuide(), is(notNullValue()));
        assertThat(trips.get(0).getGuide().getName(), equalTo("John Doe"));
    }

    @Test
    void testGetTripById() {
        Trip trip = tripDAO.getAll().get(0);
        Trip found = tripDAO.getById(trip.getId());
        assertThat(found, is(notNullValue()));
        assertThat(found.getGuide(), is(notNullValue()));
        assertThat(found.getGuide().getName(), equalTo("John Doe"));
    }

    @Test
    void testCreateTripWithGuide() {
        Trip newTrip = new Trip();
        newTrip.setName("Mountain Hike");
        newTrip.setStartTime(LocalTime.of(9, 0));  // Fixed safe value
        newTrip.setEndTime(LocalTime.of(17, 0));   // Fixed safe value
        newTrip.setLatitude(56.0);
        newTrip.setLongitude(12.0);
        newTrip.setPrice(150);
        newTrip.setGuide(testGuide);

        Trip created = tripDAO.create(newTrip);
        assertThat(created.getId(), is(notNullValue()));
        assertThat(created.getGuide(), is(notNullValue()));
        assertThat(created.getGuide().getId(), equalTo(testGuide.getId()));
    }

    @Test
    void testUpdateTrip() {
        Trip trip = tripDAO.getAll().get(0);
        trip.setName("Updated Trip Name");
        trip.setStartTime(LocalTime.of(8, 30));  // Safe fixed value
        trip.setEndTime(LocalTime.of(18, 0));    // Safe fixed value
        Trip updated = tripDAO.update(trip.getId(), trip);
        assertThat(updated.getName(), equalTo("Updated Trip Name"));
        assertThat(updated.getGuide(), is(notNullValue()));
    }

    @Test
    void testDeleteTrip() {
        Trip trip = tripDAO.getAll().get(0);
        tripDAO.deleteById(trip.getId());
        Assertions.assertThrows(ApiException.class, () -> tripDAO.getById(trip.getId()));
    }
}
