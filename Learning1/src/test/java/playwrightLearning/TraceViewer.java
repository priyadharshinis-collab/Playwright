package playwrightLearning;

import java.nio.file.Paths;

import org.testng.annotations.Test;
import org.xml.sax.Locator;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

public class TraceViewer {
	@Test
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 Playwright playwright= Playwright.create();
	      Browser browser=playwright.chromium().launch(
	    		  new BrowserType.LaunchOptions()
	    		  .setHeadless(false)
	    		  .setChannel("chrome"));
	      BrowserContext context = browser.newContext();
	     
	      context.tracing().start(
	    		  new Tracing.StartOptions()
	    		  .setScreenshots(true)
	    		  .setSnapshots(true)
	    		  //we are usingthis for to check the output with java code 
	    		  .setSources(!true));
	      Page page = context.newPage();

			// basic input field 
          page.navigate("https://letcode.in/edit");
	      
	      page.locator("#fullName").type("priya");
          com.microsoft.playwright.Locator locator = page.locator("#join");
          locator.press("End");
          locator.fill(" man");
          locator.press("Tab");
          String attribute=page.locator("id=getMe").getAttribute("value");
          System.out.println(attribute);
			page.locator("(//label[text()='Clear the text']/following::input)[1]").clear();

			// login
			 page.navigate("https://bookcart.azurewebsites.net/");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).first().click();
	      page.getByLabel("Username").fill("Priya");
			
			 page.getByLabel("Password").fill("Test@123");
			
			//strict mode validation error will shown for same name button
			 page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
					.setName("Login")).last().click();
			//System.out.println(userName.split(" ")[1].split(" ")[0]);

			context.tracing().stop(
					new Tracing.StopOptions()
					.setPath(Paths.get("trace.zip"))
					);

			context.close();
			playwright.close();
	}

}
