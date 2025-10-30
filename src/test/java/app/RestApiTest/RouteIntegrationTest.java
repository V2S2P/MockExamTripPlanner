package app.RestApiTest;

import app.config.*;
import app.security.SecurityDAO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RouteIntegrationTest {

    private static Javalin app;
    private static int port;
    private static EntityManagerFactory emf;

    private static String userToken;
    private static String adminToken;
    private static int createdGuideId;
    private static int createdTripId;

    @BeforeAll
    static void setup() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();

        app = ApplicationConfig.startServer(0, emf, true);
        port = app.port();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        SecurityDAO dao = new SecurityDAO(emf);
        try { dao.createRole("User"); } catch (Exception ignored) {}
        try { dao.createRole("Admin"); } catch (Exception ignored) {}
        try { dao.createUser("Gruppe18", "pass12345"); } catch (Exception ignored) {}
        try { dao.createUser("Admin", "pass12345"); } catch (Exception ignored) {}
        try { dao.addUserRole("Gruppe18", "User"); } catch (Exception ignored) {}
        try { dao.addUserRole("Admin", "Admin"); } catch (Exception ignored) {}

        // Login user
        userToken = given()
                .contentType("application/json")
                .body("{\"username\":\"Gruppe18\", \"password\":\"pass12345\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("Token");

        // Login admin
        adminToken = given()
                .contentType("application/json")
                .body("{\"username\":\"Admin\", \"password\":\"pass12345\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("Token");
    }

    @AfterAll
    static void teardown() {
        if (app != null && app.jettyServer() != null) app.stop();
        if (emf != null && emf.isOpen()) emf.close();
    }

    // --- USER LOGIN TEST ---
    @Test
    @Order(1)
    void testLoginWorks() {
        Assertions.assertNotNull(userToken);
        Assertions.assertNotNull(adminToken);
    }

    // --- GUIDES TESTS ---
    @Test
    @Order(2)
    void testCreateAndRetrieveGuide() {
        createdGuideId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Alice Johnson",
                        "email": "alice@example.com",
                        "phoneNumber": "12345678",
                        "yearsOfExperience": 5
                    }
                """)
                .when()
                .post("/guides")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("name", equalTo("Alice Johnson"))
                .extract()
                .path("id");

        given()
                .when()
                .get("/guides")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        given()
                .when()
                .get("/guides/" + createdGuideId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdGuideId))
                .body("name", equalTo("Alice Johnson"));
    }

    @Test
    @Order(3)
    void testUpdateGuide() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Alice Updated",
                        "email": "alice_updated@example.com",
                        "phoneNumber": "87654321",
                        "yearsOfExperience": 10
                    }
                """)
                .when()
                .put("/guides/" + createdGuideId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .when()
                .get("/guides/" + createdGuideId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Alice Updated"));
    }

    // --- TRIP TESTS ---
    @Test
    @Order(4)
    void testCreateTripAsAdmin() {
        createdTripId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Sunny Beach Adventure",
                        "price": 199.99,
                        "category": "BEACH",
                        "latitude": 56.78,
                        "longitude": 12.34,
                        "guideId": %d
                    }
                """.formatted(createdGuideId))
                .when()
                .post("/trips")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .path("id");

        Assertions.assertTrue(createdTripId > 0);
    }

    @Test
    @Order(5)
    void testRetrieveTripsAsUser() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(6)
    void testRetrieveTripByIdAsUser() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/" + createdTripId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdTripId))
                .body("name", notNullValue());
    }

    @Test
    @Order(7)
    void testUpdateTripAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Updated Beach Trip",
                        "price": 249.99,
                        "category": "BEACH",
                        "latitude": 56.80,
                        "longitude": 12.40,
                        "guideId": %d
                    }
                """.formatted(createdGuideId))
                .when()
                .put("/trips/" + createdTripId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/" + createdTripId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Beach Trip"));
    }

    @Test
    @Order(8)
    void testLinkGuideToTripAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/trips/" + createdTripId + "/guides/" + createdGuideId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(9)
    void testGetTotalTripPriceByGuide() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(10)
    void testPackingEndpoints() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/" + createdTripId + "/packing")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/" + createdTripId + "/packing/weight")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(11)
    void testDeleteTripAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/trips/" + createdTripId)
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/trips/" + createdTripId)
                .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @Order(12)
    void testDeleteGuideAsAdmin() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/guides/" + createdGuideId)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(13)
    void testUserCannotCreateTrip() {
        int tempGuideId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Bob Builder",
                        "email": "bob@example.com",
                        "phoneNumber": "98765432",
                        "yearsOfExperience": 10
                    }
                """)
                .when()
                .post("/guides")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .body("""
                    {
                        "name": "Unauthorized Trip",
                        "price": 99.99,
                        "category": "FOREST",
                        "latitude": 55.55,
                        "longitude": 11.11,
                        "guideId": %d
                    }
                """.formatted(tempGuideId))
                .when()
                .post("/trips")
                .then()
                .statusCode(403);
    }
}
