package app.security;


import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public interface ISecurityController {
    Handler login();

    Handler login(Context ctx); // to get a token

    Handler register(); // to get a user

    Handler authenticate(); // to verify roles inside token

    Handler authorize();


    String createToken(UserDTO user) throws Exception;


}
