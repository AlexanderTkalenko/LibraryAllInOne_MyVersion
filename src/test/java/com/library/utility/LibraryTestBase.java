package com.library.utility;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.reset;

public class LibraryTestBase {

    @BeforeAll
    public static void init(){

        baseURI="https://library2.cydeo.com/rest/v1";

    }

    @AfterAll
    public static void destroy(){
        reset();
    }

}
