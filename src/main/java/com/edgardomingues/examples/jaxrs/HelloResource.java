package com.edgardomingues.examples.jaxrs;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {

    @GET
    public String getHello() {
        return "Hello World";
    }
}
