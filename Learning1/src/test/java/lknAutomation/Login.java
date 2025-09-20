package lknAutomation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

public class Login {
    public static void main(String[] args) {
        // Create a Playwright instance
        try (Playwright playwright = Playwright.create()) {
            // Launch a browser instance
        	Playwright playwright1= Playwright.create();
            Browser browser=playwright1.chromium().launch(
          		  new BrowserType.LaunchOptions()
          		  .setHeadless(false)
          		  .setChannel("chrome"));
            Page page=browser.newPage();
            page.navigate("https://dev.lakshmikrishnanaturals.com/login");
            // Fill in the username and password fields
            page.locator("#mobile_number").fill("9787656765"); 
            page.locator("#login_sumbit").click(); 

            page.locator("#verify_number").fill("1234"); 

            // Click the login button
            Locator loginButton = page.locator("#verify_otp");
            loginButton.scrollIntoViewIfNeeded();
            loginButton.waitFor();
            loginButton.click();
            page.locator("(//button[contains(@class,'btn-close')])[5]").click(); 


           
            // Step 2: Click search icon without entering a product name
            Locator searchIcon = page.locator("#search_click");
            searchIcon.click();
            Locator errorMessage = page.locator("#search_error").first(); // Select the first element
            if (errorMessage.isVisible()) {
                System.out.println("Error message: " + errorMessage.textContent());
            } else {
                System.out.println("Error message not shown.");
            }
         // Step 3: Enter empty space in search field and click search icon
            Locator searchField = page.locator("#search-bar"); 
            searchField.fill("          "); // Fill empty spaces
            searchIcon.click();

            // Check if the error message appears
            if (page.locator("#search_error").first().isVisible()) { 
                System.out.println("Error message for empty space: " + page.locator(".error-message-class").textContent());
            } else {
                System.out.println("Error message not shown for empty space.");
            }
            // Step 4: Enter a product name and click the search icon
            searchField.fill("dye"); // Replace with actual product name
            searchIcon.click();

            // Step 5: Add the product to the cart and verify
            Locator addToCartButton = page.locator("(//a[contains(.,'Add to Cart')])[1]"); 
            addToCartButton.waitFor();
            addToCartButton.click();
            Locator successMessage = page.locator("(//span[@class='text text-1'][contains(.,'Success')])[3]");
            successMessage.waitFor();
            System.out.println("Product added to cart: " + successMessage.textContent());
            
            page.locator("(//i[@class='icon-x'])[2]").click(); 

            // Verify cart count
            Locator cartCount = page.locator("#Cart_count"); 
            System.out.println("Cart count: " + cartCount.textContent());

           // Step 6: Close the cart page
       Locator closeCartButton = page.locator(".close-cart-class");
      closeCartButton.click();
      System.out.println("Cart page closed successfully.");

            // Step 7: Verify the product in the cart
            Locator cartIcon = page.locator("//span[contains(@class,'lnr-cart')]");
            cartIcon.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            cartIcon.click();
            Locator cartProduct = page.locator("#cart_list_popup"); 
            cartProduct.waitFor();
            System.out.println("Product in cart: " + cartProduct.textContent());
            
            
            
            
        }
    }
}
