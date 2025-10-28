package app.config;

import app.security.SecurityController;
import app.services.MockPackingService;
import app.services.TripService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig {
    private static Javalin app;

    public static Javalin startServer(int port, EntityManagerFactory emf) {
        ServiceRegistry services = new ServiceRegistry(emf);
        RoutesRegistry routes = new RoutesRegistry(services);

        app = Javalin.create(config -> configure(config, routes));

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

        if (!testMode) {
            SecurityController securityController = new SecurityController();
            app.beforeMatched(securityController.authenticate());
            app.beforeMatched(securityController.authorize());
        }

        app.start(port);
        return app;
    }


    private static void configure(JavalinConfig config, RoutesRegistry routes) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");
        config.router.contextPath = "/api/v1";
        config.router.apiBuilder(routes.getRoutes());
    }

/**
     * Stop server
     *
     * @param-app Instans af Javalin som skal stoppes
     */
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
