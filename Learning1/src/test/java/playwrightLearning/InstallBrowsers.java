package playwrightLearning;

import java.io.IOException;

import com.microsoft.playwright.CLI;

public class InstallBrowsers {
    public static void main(String[] args) {
        // Run playwright install to download Chromium, Firefox, WebKit
        try {
			CLI.main(new String[]{"install"});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

