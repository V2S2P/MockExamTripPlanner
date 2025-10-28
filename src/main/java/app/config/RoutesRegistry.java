package app.config;

import app.routes.GuideRoutes;
import app.routes.TripRoutes;
import app.security.SecurityRoutes;
import io.javalin.apibuilder.EndpointGroup;

public class RoutesRegistry {

    private final GuideRoutes guideRoutes;
    private final TripRoutes tripRoutes;
    private final SecurityRoutes securityRoutes;

    public RoutesRegistry(ServiceRegistry services) {
        this.guideRoutes = new GuideRoutes(services.guideService);
        this.tripRoutes = new TripRoutes(services.tripService);
        this.securityRoutes = new SecurityRoutes();
    }

    public EndpointGroup getRoutes() {
        return () -> {
            guideRoutes.getRoutes().addEndpoints();
            tripRoutes.getRoutes().addEndpoints();
            securityRoutes.getSecurityRoutes().addEndpoints();
            SecurityRoutes.getSecuredRoutes().addEndpoints();

        };
    }
}
