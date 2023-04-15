package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.*;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class API_StepDefs {

    LoginPage loginPage;
    BookPage bookPage;

    RequestSpecification givenPart;
    Response response;
    static String newId;
    static String param;
    static Map<String, Object> requestBody = new LinkedHashMap<>();

    static String newEmailAPI;
    static String newPasswordAPI;
    static String newUserNameAPI;




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


    }


    @Then("status code should be {int}")
    public void status_code_should_be(Integer statusCode) {

        response.then().statusCode(200);

    }
    @Then("Response Content type is {string}")
    public void response_content_type_is(String contentType) {

        response.then().contentType(contentType);

    }
    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {

        response.then().body(path, notNullValue());

        newId = response.then().extract().jsonPath().getString(path);

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

        String path1 = response.then().extract().jsonPath().getString(path);

        Assert.assertEquals(path1, param);

    }
    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> dataList) {

        for (String eachField : dataList) {
            response.then().body(eachField, is(notNullValue()));
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
                newUserNameAPI = (String) requestBody.get("full_name");
                newEmailAPI = (String) requestBody.get("email");
                newPasswordAPI = (String) requestBody.get("password");
                break;
        }

        givenPart = givenPart.formParams(requestBody);



    }
    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String postEndPoint) {

        response = givenPart.post(ConfigurationReader.getProperty("library.baseUri") + postEndPoint).prettyPeek();

    }
    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String message, String messageItSelf) {

       response.then().body(message, is(messageItSelf));

    }

    /*
            US03 part 2
     */


    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {

        loginPage = new LoginPage();
        loginPage.login(userType);

    }
    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String modulePage) {

        loginPage = new LoginPage();
        loginPage.navigateModule(modulePage);

    }
    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
        bookPage = new BookPage();
        bookPage.search.sendKeys(requestBody.get("name").toString());
        BrowserUtil.waitFor(2);


        String query = "select * from books where id = " + newId;
        DB_Util.runQuery(query);

        Map<String, Object> actualMapFromDB = DB_Util.getRowMap(1);


        JsonPath jsonPath = givenPart.pathParam("id", newId)
                .get(ConfigurationReader.getProperty("library.baseUri") + "/get_book_by_id/{id}").prettyPeek()
                .then().statusCode(200)
                .extract().jsonPath();

        Map<Object, Object> expectedMapAPI = jsonPath.getMap("");


        bookPage.editBook((String) expectedMapAPI.get("name")).click();

        String actualBookNameUI = bookPage.bookName.getAttribute("value");
        String actualIsbnUI = bookPage.isbn.getAttribute("value");
        String actualYearUI = bookPage.year.getAttribute("value");
        String actualAuthorUI = bookPage.author.getAttribute("value");

        Assert.assertEquals(expectedMapAPI,actualMapFromDB);

        System.out.println("expectedMapAPI = " + expectedMapAPI);

        Assert.assertEquals(expectedMapAPI.get("name"),actualBookNameUI);
        Assert.assertEquals(expectedMapAPI.get("isbn"),actualIsbnUI);
        Assert.assertEquals(expectedMapAPI.get("year"),actualYearUI);
        Assert.assertEquals(expectedMapAPI.get("author"),actualAuthorUI);


        /*  If any Librarian have rights to delete Book/User
        givenPart.pathParam("id", newId)
                .when().delete(ConfigurationReader.getProperty("library.baseUri") + "/delete_book/{id}").prettyPeek()
                .then().statusCode(204);

        givenPart.pathParam("id", newId)
                .when().get(ConfigurationReader.getProperty("library.baseUri") + "/get_book_by_id/{id}")
                .then().statusCode(404);

         */

    }


    /*
            US 04
     */



    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

        String query = "select * from users where id = " + newId;
        DB_Util.runQuery(query);

        Map<String, Object> actualMapDB = DB_Util.getRowMap(1);



        JsonPath jsonPath = givenPart.pathParam("id", newId)
                .get(ConfigurationReader.getProperty("library.baseUri") + "/get_user_by_id/{id}").prettyPeek()
                .then().statusCode(200)
                .extract().jsonPath();

        Map<Object, Object> expectedMapAPI = jsonPath.getMap("");


        Assert.assertEquals(expectedMapAPI, actualMapDB);


    }
    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {

        loginPage = new LoginPage();

        loginPage.login(newEmailAPI, newPasswordAPI);
        BrowserUtil.waitFor(2);

    }
    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {

        loginPage = new LoginPage();
        String actualUserNameUI = loginPage.accountHolderName.getText();

        Assert.assertEquals(newUserNameAPI, actualUserNameUI);

    }


    /*
            US 05
     */


    static String token;
    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {

        token = given().log().uri()
                .contentType(ContentType.URLENC)
                .formParam("email", email)
                .formParam("password", password)
                .when()
                .post(ConfigurationReader.getProperty("library.baseUri") +"/login").prettyPeek()
                .then()
                .statusCode(200).extract().jsonPath().getString("token");


        givenPart = given().log().uri();

    }
    @Given("I send token information as request body")
    public void i_send_token_information_as_request_body() {

        givenPart.formParam("token", token).when();

    }





}
