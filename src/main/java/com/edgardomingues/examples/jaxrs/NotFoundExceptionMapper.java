package com.edgardomingues.examples.jaxrs;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
  @Override
  @Produces(MediaType.TEXT_HTML)
  public Response toResponse(NotFoundException exception) {
    InputStream resource = ClassLoader.getSystemResourceAsStream("META-INF/resources/hello");
    return null == resource
           ? Response.status(NOT_FOUND).build()
           : Response.ok().entity(resource).build();
  }
}