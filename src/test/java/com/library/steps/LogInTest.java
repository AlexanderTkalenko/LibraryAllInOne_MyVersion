package com.library.steps;


import com.library.utility.LibraryTestBase;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;

public class LogInTest extends LibraryTestBase {

    String token;

    @Test
    public void loginTest(){


        token = given().log().uri()
                .contentType(ContentType.URLENC)
                .formParam("email", "librarian23@library")
                .formParam("password", "libraryUser")
                //this is how to send body with URL-ENCODED format
                .when()
                .post("/login").prettyPeek()
                .then()
                .statusCode(200).extract().jsonPath().getString("token");

        //how to extract token info

        System.out.println("token = " + token);


        //send get request to retrieve dashboard_stats

        given().log().uri()
                .accept(ContentType.JSON)
                .header("x-library-token", token)
                .when()
                .get("/dashboard_stats").prettyPeek()
                .then()
                .statusCode(200);

    }





}
