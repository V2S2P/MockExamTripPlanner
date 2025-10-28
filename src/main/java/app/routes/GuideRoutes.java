package app.routes;

import app.controllers.GuideController;
import app.controllers.TripController;
import app.services.GuideService;
import app.services.TripService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.put;

public class GuideRoutes {
    private final GuideController guideController;

    public GuideRoutes(GuideService guideService) {
        this.guideController = new GuideController(guideService);
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("guides", () -> {
                post(guideController.create());
                get(guideController.getAll());

                path("{id}", () -> {
                    get(guideController.getById());
                    put(guideController.update());
                    delete(guideController.delete());
                });
            });
        };
    }
}
