package app.controllers;

import app.dtos.GuideDTO;
import app.mappers.GuideMapper;
import app.services.GuideService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.List;

public class GuideController implements IController {
    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @Override
    public Handler create() {
        return (Context ctx) -> {
            GuideDTO guideDTO = ctx.bodyAsClass(GuideDTO.class);
            GuideDTO newGuideDTO = guideService.createGuide(guideDTO);
            ctx.status(HttpStatus.CREATED).json(newGuideDTO);
        };
    }

    @Override
    public Handler getById() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            GuideDTO guideDTO = guideService.getById(id);
            ctx.status(HttpStatus.OK).json(guideDTO);
        };
    }

    @Override
    public Handler getAll() {
        return (Context ctx) -> {
            List<GuideDTO> guideDTOs = guideService.getAll();
            ctx.status(HttpStatus.OK).json(guideDTOs);
        };
    }

    @Override
    public Handler update() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            GuideDTO existing = ctx.bodyAsClass(GuideDTO.class);
            GuideDTO updatedDTO = guideService.update(existing, id);
            ctx.status(HttpStatus.OK).json(updatedDTO);
        };
    }

    @Override
    public Handler delete() {
        return (Context ctx) -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            guideService.delete(id);
            ctx.status(HttpStatus.NO_CONTENT);
        };
    }
}
