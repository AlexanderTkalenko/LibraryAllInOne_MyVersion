package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.*;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class API_StepDefs {

    LoginPage loginPage = new LoginPage();
    BookPage bookPage = new BookPage();

    RequestSpecification givenPart;
    ValidatableResponse thenPart;
    Response response;
    static String newBookId;
    static String param;
    static Map<String, Object> requestBody = new LinkedHashMap<>();




    /*
        US 01
   */

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String usertype) {

        givenPart = given().log().uri()
                .header("x-library-token", LibraryAPI_Util.getToken(usertype));

    }
    @Given("Accept header is {string}")
    public void accept_header_is(String contentType) {

        givenPart.accept(contentType);

    }
    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String getEndpoint) {

        response = givenPart.when().get(ConfigurationReader.getProperty("library.baseUri") + getEndpoint).prettyPeek();

        thenPart = response.then();

    }
    @Then("status code should be {int}")
    public void status_code_should_be(Integer statusCode) {

        thenPart.statusCode(statusCode);

    }
    @Then("Response Content type is {string}")
    public void response_content_type_is(String contentType) {

        thenPart.contentType(contentType);

    }
    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {

        thenPart.body(path, everyItem(notNullValue()));

    }


    /*
            US 02
     */

    @Given("Path param is {string}")
    public void path_param_is(String param) {

        this.param = param;
        givenPart.pathParam("id", param);

    }
    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {

        String path1 = thenPart.extract().jsonPath().getString(path);

        Assert.assertEquals(path1, param);

    }
    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> dataList) {

        for (String eachField : dataList) {
            thenPart.body(eachField, is(notNullValue()));
        }

    }



    /*
            US 03 part 1
     */

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_igs(String requestContentType) {

        givenPart.contentType(requestContentType);

    }
    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String bookOrUser) {

        switch (bookOrUser){
            case "book":
                requestBody = LibraryAPI_Util.getRandomBookMap();
                break;

            case "user":
                requestBody = LibraryAPI_Util.getRandomUserMap();
                break;
        }


        System.out.println("requestBody = " + requestBody);

        givenPart = givenPart.pathParams(requestBody);


    }
    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String postEndPoint) {

        response = givenPart.post(ConfigurationReader.getProperty("library.baseUri") + postEndPoint).prettyPeek();

    }
    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String message, String messageItSelf) {



    }

    /*
            US03 part 2
     */


    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {

        loginPage.login(userType);

    }
    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String modulePage) {

        loginPage.navigateModule(modulePage);



    }
    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {

        bookPage.search.sendKeys(requestBody.get("name").toString());
        BrowserUtil.waitFor(3);


        String query = "select * from books where id = " + newBookId;
        DB_Util.runQuery(query);

        DB_Util.getRowMap(0);


        JsonPath jsonPath = givenPart.pathParam("id", newBookId)
                .get(ConfigurationReader.getProperty("library.baseUri") + "/get_book_by_id/{id}")
                .then().statusCode(200)
                .extract().jsonPath();




    }





}
