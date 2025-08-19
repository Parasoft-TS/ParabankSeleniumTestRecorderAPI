/**
 * 
 */
package com.parabank;

import com.parabank.page.ActivityPage;
import com.parabank.page.ParaBankAccountActivityPage;
import com.parabank.page.ParaBankAccountsOverviewPage;
import com.parabank.page.ParaBankBillPayPage;
import com.parabank.page.ParaBankTransferFundsPage;
import com.parabank.page.ParaBankWelcomeOnlineBankingPage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ParabankTest {

	/**
	 * Parasoft auto generated base URL
	 * Use -DPARABANK_BASE_URL=http://localhost:8080 from command line
	 * or use System.setProperty("PARABANK_BASE_URL", "http://localhost:8080") to change base URL at run time.
	 */
	private static final String PARABANK_BASE_URL = "http://localhost:18080";

	private WebDriver driver;

	@BeforeEach
	public void beforeTest() {
		ChromeOptions opts = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.managed_default_content_settings.geolocation", 2);
		prefs.put("profile.default_content_setting_values.notifications", 2);
		opts.setExperimentalOption("prefs", prefs);
		opts.addArguments("--start-maximized");
		opts.addArguments("--incognito");
		opts.addArguments("--enable-strict-powerful-feature-restrictions");
                opts.addArguments("--headless");       
                opts.addArguments("--no-sandbox");     
                opts.addArguments("--disable-dev-shm-usage");
                opts.addArguments("--disable-gpu");   

		driver = new ChromeDriver(opts);
		driver.manage().window().maximize();
	}

	@AfterEach
	public void afterTest() {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * Name: ts566
	 * Recording file: ts566.json
	 *
	 * Parasoft recorded Selenium test on Tue Aug 19 2025 12:52:10 GMT+0200 (czas środkowoeuropejski letni)
	 */
	@Test
	public void testParabankTest() throws Throwable {
		driver.get(System.getProperty("PARABANK_BASE_URL", PARABANK_BASE_URL)
				+ "/parabank/index.htm;jsessionid=BF577E9701321D4C41A912FF1CE5D060");

		ParaBankWelcomeOnlineBankingPage paraBankWelcomeOnlineBankingPage = new ParaBankWelcomeOnlineBankingPage(
				driver);
		paraBankWelcomeOnlineBankingPage.setUsernameText("john");
		paraBankWelcomeOnlineBankingPage.setPassword("demo");
		paraBankWelcomeOnlineBankingPage.clickLogInSubmit();

		ParaBankAccountsOverviewPage paraBankAccountsOverviewPage = new ParaBankAccountsOverviewPage(driver);
		paraBankAccountsOverviewPage.clickLink();

		ParaBankAccountActivityPage paraBankAccountActivityPage = new ParaBankAccountActivityPage(driver);
		paraBankAccountActivityPage.clickAccountsOverviewLink();
		paraBankAccountsOverviewPage.clickLink2();

		ActivityPage activityPage = new ActivityPage(driver);
		activityPage.clickAccountsOverviewLink();
		paraBankAccountsOverviewPage.clickTransferFundsLink();

		ParaBankTransferFundsPage paraBankTransferFundsPage = new ParaBankTransferFundsPage(driver);
		paraBankTransferFundsPage.setInputText("100");
		paraBankTransferFundsPage.selectToAccountIdSelectOne("12456");
		paraBankTransferFundsPage.clickTransferSubmit();
		paraBankTransferFundsPage.clickBillPayLink();

		ParaBankBillPayPage paraBankBillPayPage = new ParaBankBillPayPage(driver);
		paraBankBillPayPage.setPayeeNameText("John");
		paraBankBillPayPage.setPayeeAddressStreetText("Street");
		paraBankBillPayPage.setPayeeAddressCityText("City");
		paraBankBillPayPage.setPayeeAddressStateText("Utah");
		paraBankBillPayPage.setPayeeAddressZipCodeText("112233");
		paraBankBillPayPage.setPayeePhoneNumberText("123456789");
		paraBankBillPayPage.setPayeeAccountNumberText("12345");
		paraBankBillPayPage.setVerifyAccountText("12456");
		paraBankBillPayPage.setAmountText("100");
		paraBankBillPayPage.clickVerifyAccount();
		paraBankBillPayPage.setVerifyAccountText("12345");
		paraBankBillPayPage.clickSendPaymentSubmit();
	}

}
