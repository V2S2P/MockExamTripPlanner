package app.config;

import app.exceptions.ApiException; // ✅ add this import
import app.security.SecurityController;
import app.services.MockPackingService;
import app.services.TripService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;

public class ApplicationConfig {
    private static Javalin app;

    public static Javalin startServer(int port, EntityManagerFactory emf) {
        ServiceRegistry services = new ServiceRegistry(emf);
        RoutesRegistry routes = new RoutesRegistry(services);

        app = Javalin.create(config -> configure(config, routes));

        // ✅ Global Exception Handler
        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode()).json(Map.of(
                    "status", e.getStatusCode(),
                    "message", e.getMessage()
            ));
        });

        if (!HibernateConfig.getTest()) {
            SecurityController securityController = new SecurityController();
            app.beforeMatched(securityController.authenticate());
            app.beforeMatched(securityController.authorize());
        }

        app.start(port);
        return app;
    }

    public static Javalin startServer(int port, EntityManagerFactory emf, boolean testMode) {
        ServiceRegistry services = new ServiceRegistry(emf);

        if (testMode) {
            services.setTripService(new TripService(emf, new MockPackingService()));
        }

        RoutesRegistry routes = new RoutesRegistry(services);
        app = Javalin.create(config -> configure(config, routes));

        // ✅ Global Exception Handler (same here)
        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode()).json(Map.of(
                    "status", e.getStatusCode(),
                    "message", e.getMessage()
            ));
        });

        // ALWAYS attach security, even in test mode
        SecurityController securityController = new SecurityController();
        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());

        app.start(port);
        return app;
    }

    private static void configure(JavalinConfig config, RoutesRegistry routes) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");

        if (!HibernateConfig.getTest()) {
            config.router.contextPath = "/api/v1";
        }

        config.router.apiBuilder(routes.getRoutes());
    }

    public static void stopServer() {
        if (app != null) {
            System.out.println("Stopping server and closing EMF...");
            app.stop();
            if (HibernateConfig.getEntityManagerFactory().isOpen()) {
                HibernateConfig.getEntityManagerFactory().close();
            }
        }
    }
}
