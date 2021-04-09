package com.edgardomingues.examples.jaxrs;

import com.edgardomingues.examples.jaxrs.HelloResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@TestHTTPEndpoint(HelloResource.class)
class HelloResourceIT {

    @Test
    void getHello() {
        when()
            .get()
        .then()
            .statusCode(200)
            .body(containsString("Hello from static file without extension."));
    }

    @Test
    void getHelloJson() {
        given()
            .accept(MediaType.APPLICATION_JSON)
        .when()
            .get()
        .then()
            .statusCode(200)
            .body(containsString("Hello from static file without extension."));
    }

    @Test
    void get404() {
        when()
            .get("404")
        .then()
            .statusCode(200)
            .body(containsString("Hello from static file without extension."));
    }
}