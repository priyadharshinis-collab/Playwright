package playwrightLearning;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

public class GetByLocatorsExample {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 Playwright playwright= Playwright.create();
	      Browser browser=playwright.chromium().launch(
	    		  new BrowserType.LaunchOptions()
	    		  .setHeadless(false)
	    		  .setChannel("chrome"));
	      Page page=browser.newPage();
	      page.navigate("https://bookcart.azurewebsites.net/");
	      //click login by text
	      //page.getByText(" Login ").click();
	      //Find username textfield by value 
	      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).first().click();
	      
	      page.waitForTimeout(2000); 
			
	      page.getByLabel("Username").fill("Priya");
			 page.waitForTimeout(2000); 
			
			 page.getByLabel("Password").fill("Test@123");
			 page.waitForTimeout(2000); 
			//strict mode validation error will shown for same name button
			 page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).last().click();
			 page.waitForTimeout(2000); 
			 page.getByPlaceholder("Search books or authors").type("The Hookup");
			//page.getByPlaceholder("Search books", new Page.GetByPlaceholderOptions()
					//.setExact(true)).type("The Hookup");
			
			 page.waitForTimeout(2000); 
			page.getByRole(AriaRole.OPTION).first().click();
			 page.waitForTimeout(2000); 
			
			 page.getByAltText("Book cover image").click();
			System.out.println(page.url());	
			
		
			
			playwright.close();
	}

}

