package app.controllers;

import io.javalin.http.Handler;

public interface IController {
    Handler create();

    Handler getAll();

    Handler update();

    Handler delete();

    Handler getById();
}
