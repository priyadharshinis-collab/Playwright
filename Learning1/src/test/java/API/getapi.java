package API;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;

import org.testng.Assert;
import org.testng.annotations.*;

public class getapi {
    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    static Page page;
    static APIRequestContext request;

    @BeforeClass
    public void setup() {
        // --- Playwright setup ---
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();

        // --- API Context (no trailing slash in base URL!) ---
        request = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL("https://reqres.in/api")); 
    }

    // Create a new user via API
    @Test(priority = 1)
    public void createUserApiTest() {
        String requestBody = "{ \"name\": \"Priya\", \"job\": \"QA Tester\" }";

        APIResponse apiResponse = request.post("users", // no leading slash!
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        System.out.println("API Status: " + apiResponse.status());
        System.out.println("API Response: " + apiResponse.text());

        Assert.assertEquals(apiResponse.status(), 201, "User creation failed!");
    }

    // Login via API
    @Test(priority = 2, dependsOnMethods = {"createUserApiTest"})
    public void loginApiTest() {
        String loginPayload = "{ \"email\": \"eve.holt@reqres.in\", \"password\": \"cityslicka\" }";

        APIResponse loginResponse = request.post("login", // no leading slash!
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(loginPayload));

        System.out.println("Login API Status: " + loginResponse.status());
        System.out.println("Login API Response: " + loginResponse.text());

        Assert.assertEquals(loginResponse.status(), 200, "Login failed!");
    }

    @AfterClass
    public void teardown() {
        if (request != null) request.dispose();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
