package app.controllers;

import app.dtos.TripDTO;
import app.exceptions.ApiException;
import app.services.TripService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class TripController implements IController{
    private final TripService tripService;
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @Override
    public Handler create() {
        return (Context ctx) -> {
            System.out.println("User role in create trip: " + ctx.attribute("role"));
            TripDTO tripDTO = ctx.bodyAsClass(TripDTO.class);
            TripDTO newTripDTO = tripService.create(tripDTO);
            ctx.status(HttpStatus.CREATED).json(newTripDTO);
        };
    }

    @Override
    public Handler getById() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TripDTO tripDTO = tripService.getById(id);
            ctx.status(HttpStatus.OK).json(tripDTO);
        };
    }

    @Override
    public Handler getAll() {
        return (Context ctx) -> {
            List<TripDTO> tripDTOs = tripService.getAll();
            ctx.status(HttpStatus.OK).json(tripDTOs);
        };
    }

    @Override
    public Handler update() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TripDTO existing = ctx.bodyAsClass(TripDTO.class);
            TripDTO updatedDTO = tripService.update(existing, id);
            //Could also use NO_CONTENT, but if so, remove the body(the .json)
            ctx.status(HttpStatus.OK).json(updatedDTO);
        };
    }

    @Override
    public Handler delete() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            tripService.delete(id);
            //No body since we use NO_CONTENT
            ctx.status(HttpStatus.NO_CONTENT);
        };
    }

    public Handler linkGuide() {
        return ctx -> {
            int tripId = Integer.parseInt(ctx.pathParam("tripId"));
            int guideId = Integer.parseInt(ctx.pathParam("guideId"));
            TripDTO updatedTrip = tripService.linkGuideToTrip(tripId, guideId);
            ctx.status(HttpStatus.OK).json(updatedTrip);
        };
    }
    public Handler filterTripsByCategory() {
        return ctx -> {
            String category = ctx.queryParam("category");
            if (category == null || category.isBlank()) {
                throw new ApiException(400, "Category query parameter is required");
            }
            List<TripDTO> tripDTOs = tripService.filterTripsByCategory(category);
            ctx.status(HttpStatus.OK).json(tripDTOs);
        };
    }
    public Handler getTotalTripPriceByGuide() {
        return ctx -> {
            Map<Integer, Double> totals = tripService.getTotalPriceByGuide();
            ctx.status(HttpStatus.OK).json(totals);
        };
    }
    //FOR FETCHING EXTERNAL API
    public Handler getTripWithPacking() {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TripDTO tripDTO = tripService.getByIdWithPacking(id);
            ctx.status(HttpStatus.OK).json(tripDTO);
        };
    }

    public Handler getTotalPackingWeight() {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            int totalWeight = tripService.getTotalPackingWeight(id);
            ctx.status(HttpStatus.OK).json(totalWeight);
        };
    }
}
