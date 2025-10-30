package app.routes;

import app.controllers.TripController;
import app.security.Roles;
import app.services.TripService;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TripRoutes {
    private final TripController tripController;

    public TripRoutes(TripService tripService) {
        this.tripController = new TripController(tripService);
    }
    public EndpointGroup getRoutes() {
        return () -> {
            path("trips", () -> {
                post(tripController.create(), Roles.ADMIN);

                // Single GET handler that handles filtering or returns all
                get(ctx -> {
                    String category = ctx.queryParam("category");
                    if (category != null && !category.isBlank()) {
                        tripController.filterTripsByCategory().handle(ctx);
                    } else {
                        tripController.getAll().handle(ctx);
                    }
                }, Roles.USER);
                get("guides/totalprice", tripController.getTotalTripPriceByGuide(), Roles.USER);

                path("{id}", () -> {
                    get(tripController.getById(), Roles.USER);
                    put(tripController.update(), Roles.ADMIN);
                    delete(tripController.delete(), Roles.ADMIN);
                    get("packing",tripController.getTripWithPacking(), Roles.USER); // /trips/{id}/packing
                    get("packing/weight",tripController.getTotalPackingWeight(), Roles.USER); // /trips/{id}/packing/weight
                });

                path("{tripId}/guides/{guideId}", () -> {
                    put(tripController.linkGuide(), Roles.ADMIN);
                });
            });
        };
    }
}
