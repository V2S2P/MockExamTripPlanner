package app.populator;

import app.config.HibernateConfig;
import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalTime;

public class DBPopulator {

    private final GuideDAO guideDAO;
    private final TripDAO tripDAO;

    public DBPopulator(EntityManagerFactory emf) {
        this.guideDAO = new GuideDAO(emf);
        this.tripDAO = new TripDAO(emf);
    }

    public void populate() {
        // --- Create Guides ---
        Guide guide1 = new Guide();
        guide1.setName("Alice Johnson");
        guide1.setEmail("alice@example.com");
        guide1.setPhoneNumber("12345678");
        guide1.setYearsOfExperience(5);
        guide1 = guideDAO.create(guide1);

        Guide guide2 = new Guide();
        guide2.setName("Bob Smith");
        guide2.setEmail("bob@example.com");
        guide2.setPhoneNumber("87654321");
        guide2.setYearsOfExperience(8);
        guide2 = guideDAO.create(guide2);

        // --- Create Trips ---
        Trip trip1 = new Trip();
        trip1.setName("Sunny Beach Adventure");
        trip1.setStartTime(LocalTime.of(9, 0));
        trip1.setEndTime(LocalTime.of(17, 0));
        trip1.setLongitude(12.34);
        trip1.setLatitude(56.78);
        trip1.setPrice(199.99);
        trip1.setCategory(Category.BEACH);
        trip1.setGuide(guide1);
        tripDAO.create(trip1);

        Trip trip2 = new Trip();
        trip2.setName("Mountain Hike");
        trip2.setStartTime(LocalTime.of(6, 0));
        trip2.setEndTime(LocalTime.of(14, 0));
        trip2.setLongitude(23.45);
        trip2.setLatitude(67.89);
        trip2.setPrice(149.99);
        trip2.setCategory(Category.FOREST);
        trip2.setGuide(guide2);
        tripDAO.create(trip2);

        Trip trip3 = new Trip();
        trip3.setName("City Exploration");
        trip3.setStartTime(LocalTime.of(10, 0));
        trip3.setEndTime(LocalTime.of(18, 0));
        trip3.setLongitude(34.56);
        trip3.setLatitude(78.90);
        trip3.setPrice(99.99);
        trip3.setCategory(Category.CITY);
        trip3.setGuide(guide1);
        tripDAO.create(trip3);

        System.out.println("Database populated with sample guides and trips!");
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        DBPopulator populator = new DBPopulator(emf);
        populator.populate();
    }
}
