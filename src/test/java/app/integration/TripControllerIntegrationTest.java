package app.integration;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.TripDTO;
import app.entities.Guide;
import app.enums.Category;
import app.populator.DBPopulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TripControllerIntegrationTest {

    private static final String BASE_URL = "http://localhost:7777/api/v1";
    private static EntityManagerFactory emfTest;
    private static Javalin app;
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @BeforeAll
    static void setUpAll() {
        RestAssured.baseURI = BASE_URL;
        jsonMapper.findAndRegisterModules();

        HibernateConfig.setTest(true);
        emfTest = HibernateConfig.getEntityManagerFactoryForTest();

        app = ApplicationConfig.startServer(7777, emfTest, true); // testMode = true
    }

    @AfterAll
    static void tearDownAll() {
        HibernateConfig.setTest(false);
        ApplicationConfig.stopServer();
    }

    @BeforeEach
    void resetDatabase() {
        resetAndPopulateDatabase();

        // Add a guide manually if DBPopulator doesn't already
        EntityManager em = emfTest.createEntityManager();
        em.getTransaction().begin();

        Guide guide = new Guide();
        guide.setName("Test Guide");
        em.persist(guide);

        em.getTransaction().commit();
        em.close();
    }

    /** Utility method for full DB reset before each test */
    private void resetAndPopulateDatabase() {
        EntityManager em = emfTest.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Trip").executeUpdate();
        em.createQuery("DELETE FROM Guide").executeUpdate();
        em.createQuery("DELETE FROM Role").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();

        new DBPopulator(emfTest).populate();
    }
    @Test
    void getTripById_includesPackingData() {
        given()
                .when()
                .get("/api/v1/trips/1")
                .then()
                .statusCode(200)
                .body("packing.items.size()", greaterThan(0))
                .body("packing.totalWeight", equalTo(3300))
                .body("packing.items[0].name", equalTo("Tent"));
    }

    @Test
    @Order(1)
    void getAllTrips_returnsListOfTrips() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue());
    }

    @Test
    @Order(2)
    void getTripsFilteredByCategory_returnsOnlyMatchingTrips() {
        given()
                .queryParam("category", "BEACH")
                .contentType(ContentType.JSON)
                .when()
                .get("/trips")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", greaterThan(0))
                .body("category", everyItem(equalTo("BEACH")));
    }

    @Test
    @Order(3)
    void getTripByIdWithPacking_returnsTripAndPackingItems() {
        // Fetch a valid trip ID dynamically instead of hardcoding
        int tripId = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .extract()
                .path("[0].id");  // take the first trip in the list

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips/{id}/packing", tripId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("id", equalTo(tripId))
                .body("packingItems", not(empty()))
                .body("packingItems[0].name", equalTo("Tent")); // matches MockPackingService
    }

    @Test
    @Order(4)
    void getTotalPackingWeight_returnsPositiveValue() {
        // Fetch a valid trip ID dynamically
        int tripId = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .extract()
                .path("[0].id");  // first trip in DB

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips/{id}/packing/weight", tripId)
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body(greaterThan(0));
    }

    @Test
    @Order(5)
    void getTotalPriceByGuide_returnsValidPriceList() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(6)
    void createUpdateDeleteTrip_worksCorrectly() {
        // --- Create ---
        TripDTO newTrip = new TripDTO();
        newTrip.setName("Test Trip");
        newTrip.setCategory(Category.CITY);
        newTrip.setLatitude(12.34);
        newTrip.setLongitude(56.78);
        newTrip.setPrice(99.99);
        newTrip.setGuideId(1);

        TripDTO createdTrip = given()
                .contentType(ContentType.JSON)
                .body(newTrip)
                .when()
                .post("/trips")
                .then()
                .statusCode(HttpStatus.CREATED.getCode())
                .body("name", equalTo("Test Trip"))
                .extract().as(TripDTO.class);

        // --- Update ---
        createdTrip.setName("Updated Trip");
        given()
                .contentType(ContentType.JSON)
                .body(createdTrip)
                .when()
                .put("/trips/{id}", createdTrip.getId())
                .then()
                .statusCode(HttpStatus.OK.getCode())
                .body("name", equalTo("Updated Trip"));

        // --- Delete ---
        given()
                .when()
                .delete("/trips/{id}", createdTrip.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.getCode());
    }
}
