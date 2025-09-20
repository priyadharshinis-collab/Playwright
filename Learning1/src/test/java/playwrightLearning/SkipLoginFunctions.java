package playwrightLearning;

import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

public class SkipLoginFunctions {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Playwright playwright= Playwright.create();
	      Browser browser=playwright.chromium().launch(
	    		  new BrowserType.LaunchOptions()
	    		  .setHeadless(false)
	    		  .setChannel("chrome"));
	      Page page=browser.newPage();
	      page.navigate("https://bookcart.azurewebsites.net/");
	      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).first().click();
	      page.getByLabel("Username").fill("Dharshini");
	      page.getByLabel("Password").fill("Test@123");
		  page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).last().click(); 
		  String title=page.title();
	      System.out.println("Page title:" + title);
		  //Skip authentication
	      //generate auth 
	      //context.storageState(new StorageStateOptions().setPath(Paths.get("auth.json")));			
		  
		  
	}

}
