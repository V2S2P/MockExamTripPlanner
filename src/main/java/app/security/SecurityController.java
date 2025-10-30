package app.security;

import app.config.HibernateConfig;
import app.entities.Role;
import app.entities.User;
import app.exceptions.ApiException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.*;
import jakarta.persistence.EntityExistsException;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {

    ObjectMapper objectMapper = new ObjectMapper();

    // DAO til at snakke med databasen (bruges til at finde brugere og verificere login)
    ISecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());

    // Mapper bruges til at lave JSON-objekter nemt
    ObjectMapper mapper = new ObjectMapper();

    // Klassen der håndterer alt med tokens (JWT’er)
    TokenSecurity tokenSecurity = new TokenSecurity();


    /**
     * LOGIN HANDLER
     * - Tager brugernavn og password fra request body
     * - Tjekker om brugeren findes og password matcher
     * - Opretter JWT token hvis alt er OK
     * - Returnerer token + brugernavn som JSON
     */
    @Override
    public Handler login() {
        return (Context ctx) -> {
            // Læs JSON body ind som et User-objekt
            User user = ctx.bodyAsClass(User.class);
            try {
                // Tjek i databasen om brugeren findes og password passer
                User verified = securityDAO.getVerifiedUser(user.getUserName(), user.getPassword());

                // Lav rollerne om til Strings (f.eks. "USER", "ADMIN")
                Set<String> stringRoles = verified.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet());

                System.out.println("Successfully logged in " + verified.getUserName());

                // Opret en DTO som bruges til token (indeholder username + roller)
                UserDTO userDTO = new UserDTO(verified.getUserName(), stringRoles);

                // Lav et JWT token ud fra userDTO
                String token = createToken(userDTO);

                // Byg JSON response
                ObjectNode on = mapper
                        .createObjectNode()
                        .put("Token", token)
                        .put("username", userDTO.getUsername());

                // Send JSON tilbage med status 200 OK
                ctx.json(on).status(200);

            } catch (Exception e) {
                e.printStackTrace();
                // Hvis login fejler, smid fejl
                throw new ValidationException("No user logged in");
            }
        };
    }

    // Ikke implementeret (kan bruges hvis man vil kalde login direkte med Context)
    @Override
    public Handler login(Context ctx) {
        return null;
    }

    // Ikke implementeret (her kunne man lave brugeroprettelse)
    @Override
    public Handler register() {
        return ctx -> {
            var returnObject = objectMapper.createObjectNode();
            try {
                UserDTO userInput = ctx.bodyAsClass(UserDTO.class);

                // Create user
                User created = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());

                // Ensure USER role exists
                try { securityDAO.createRole("User"); } catch (Exception ignored) {}

                // Assign role and get updated user with roles
                User updatedUser = securityDAO.addUserRole(created.getUserName(), "User");

                // Create JWT token
                String token = createToken(new UserDTO(updatedUser.getUserName(), updatedUser.getRolesAsStrings()));

                ctx.status(HttpStatus.CREATED).json(returnObject
                        .put("token", token)
                        .put("username", updatedUser.getUserName()));

            } catch (EntityExistsException e) {
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT)
                        .json(returnObject.put("msg", "User already exists"));
            }
        };
    }



    /**
     * POPULATE HANDLER
     * - Creates default users and roles
     * - Only for development/testing purposes
     */
    public Handler populate() {
        return (ctx) -> {
            try {
                // Create roles
                Role userRole = securityDAO.createRole("User");
                Role adminRole = securityDAO.createRole("Admin");

                // Create users
                User user = securityDAO.createUser("Gruppe18", "pass12345");
                User admin = securityDAO.createUser("Admin", "pass12345");

                // Assign roles
                securityDAO.addUserRole("Gruppe18", "User");
                securityDAO.addUserRole("Admin", "Admin");

                // Build response
                ctx.status(201).json(Map.of(
                        "message", "Database populated with default users and roles",
                        "users", Map.of(
                                "user", "Gruppe18 / pass12345",
                                "admin", "Admin / pass12345"
                        )
                ));
            } catch (EntityNotFoundException e) {
                ctx.status(500).json(Map.of("error", "Failed to assign roles: " + e.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", "Failed to populate database: " + e.getMessage()));
            }
        };
    }


    /**
     * AUTHENTICATE HANDLER
     * - Bliver kaldt som "before" filter i Javalin
     * - Tjekker om token eksisterer og er gyldig
     * - Lægger brugeren fra token ind i context som attribut
     */
    @Override
    public Handler authenticate() {
        return (Context ctx) -> {

            // Hvis det er en preflight (OPTIONS-request), skal vi ikke tjekke token
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }

            // Hent de roller, som er tilladt for den route
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // Hvis endpointet er åbent (ANYONE), spring auth over
            if (isOpenEndpoint(allowedRoles))
                return;

            // Ellers: hent og verificér token
            UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);

            // Gem brugeren i context så vi kan bruge den senere
            ctx.attribute("user", verifiedTokenUser);
        };
    }


    /**
     * AUTHORIZE HANDLER
     * - Tjekker om brugeren har den rigtige rolle til at tilgå et endpoint
     */
    @Override
    public Handler authorize() {
        return (Context ctx) -> {
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // 1. Hvis endpoint er åbent, gør ingenting
            if (isOpenEndpoint(allowedRoles))
                return;

            // 2. Hent brugeren (fra authenticate)
            UserDTO user = ctx.attribute("user");
            if (user == null) {
                throw new ForbiddenResponse("No user was added from the token");
            }

            // 3. Tjek om brugeren har en af de tilladte roller
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        };
    }


    /**
     * Opretter et JWT token
     * - Henter settings (issuer, expire time, secret key)
     * - Kalder tokenSecurity til at oprette selve token
     */
    @Override
    public String createToken(UserDTO user) throws Exception {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            // Hvis vi kører i "production" (deployet)
            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                // Lokalt - hent værdier fra config.properties
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }

            // Brug tokenSecurity til at oprette token
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token");
        }
    }


    /**
     * Henter token fra "Authorization" headeren
     * Forventet format: "Bearer <token>"
     */
    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing");
        }

        // Split "Bearer <token>" og tag kun selve token
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed");
        }
        return token;
    }


    /**
     * Verificér token med secret key
     * - Tjek om token er gyldig og ikke udløbet
     * - Returnér UserDTO fra token
     */
    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ?
                System.getenv("SECRET_KEY") :
                Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new UnauthorizedResponse("Token is not valid");
            }
        } catch (ParseException | TokenVerificationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    /**
     * Kombineret metode:
     * - Henter token fra header
     * - Verificerer den
     * - Returnerer brugeren fra token
     */
    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token");
        }
        return verifiedTokenUser;
    }

    /**
     * Returnerer true hvis endpointet er åbent (ingen roller eller ANYONE)
     */
    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        if (allowedRoles.isEmpty())
            return true;
        return allowedRoles.contains("ANYONE");
    }

    /**
     * Returnerer true hvis brugeren har mindst én af de krævede roller
     */
    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }

    // Health check for the API. Used in deployment
    public void healthCheck(Context ctx) {
        ctx.status(200).json(Map.of("msg", "API is up and running")); // safe and proper
    }
}
