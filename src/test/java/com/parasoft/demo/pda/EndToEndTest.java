/**
 * 
 */
package com.parasoft.demo.pda;

import com.parasoft.demo.pda.page.CategoriesPage;
import com.parasoft.demo.pda.page.HomePage;
import com.parasoft.demo.pda.page.OrderWizardPage;
import com.parasoft.demo.pda.page.OrdersPage;
import com.parasoft.demo.pda.page.LoginPage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class EndToEndTest {

	/**
	 * Parasoft auto generated base URL Use -DBASE_URL=http://localhost:8080 from
	 * command line or use System.setProperty("BASE_URL", "http://localhost:8080")
	 * to change base URL at run time.
	 */
	private static final String BASE_URL = "http://qa1.parasoft.com:8092";
	private static final String CHROME_DRIVER = "C:\\Users\\whaaker\\Downloads\\SOAVirt\\Extensions\\chromedriver_win64_(v116)\\chromedriver.exe";

	private WebDriver driver;

	@Before
	public void beforeTest() {
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER);

		ChromeOptions opts = new ChromeOptions();
		opts.addArguments("--start-maximized");
		opts.addArguments("--disable-geolocation");
		opts.addArguments("--incognito");
		opts.addArguments("--enable-strict-powerful-feature-restrictions");
		opts.addArguments("--remote-allow-origins=*");

		driver = new ChromeDriver(opts);
		driver.manage().window().maximize();
	}

	@After
	public void afterTest() {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * Name: FullFlowCampingSkin Recording file: FullFlowCampingSkin.json
	 *
	 * Parasoft recorded Selenium test on Wed Nov 30 2022 17:25:31 GMT-0800 (Pacific
	 * Standard Time)
	 */
	@Test
	public void testFullFlowCampingSkin() throws Throwable {
		driver.get(System.getProperty("BASE_URL", BASE_URL) + "/loginPage");

		LoginPage loginPage = new LoginPage(driver);
		loginPage.setUsernameField("purchaser");
		loginPage.setPasswordField("password");
		loginPage.clickSignInButton();

		HomePage homePage = new HomePage(driver);
		homePage.clickSleepingBagsLink();

		CategoriesPage categoriesPage = new CategoriesPage(driver);
		categoriesPage.clickAddToCart();
		categoriesPage.clickAddToCartButton();
		categoriesPage.clickAddToCartButton2();
		categoriesPage.clickAddToCartButton3();
		categoriesPage.clickRightBarImg();
		categoriesPage.clickProceedToSubmission();

		OrderWizardPage orderWizardPage = new OrderWizardPage(driver);
		orderWizardPage.selectRegionSelectDropdown("Parkside General Store");
		orderWizardPage.setReceiverNameInputField("123");
		orderWizardPage.clickGetLocationButton();
		orderWizardPage.clickInvoiceAssignmentButton();
		orderWizardPage.setCampaignIdInputField("456");
		orderWizardPage.setServiceNumberInputField("789");
		orderWizardPage.clickGoToReviewButton();
		orderWizardPage.clickSubmitForApprovalButton();
		orderWizardPage.clickUsername();
		orderWizardPage.clickSignOutLink();
		loginPage.setUsernameField("approver");
		loginPage.setPasswordField("password");
		loginPage.clickSignInButton();
		homePage.clickOrderLink();
		homePage.selectStatusDropdown("Approve");
		homePage.clickSaveAndSendResponse();
		homePage.clickUsername();
		homePage.clickSignOutLink();
		loginPage.setUsernameField("purchaser");
		loginPage.setPasswordField("password");
		loginPage.clickSignInButton();
		homePage.clickOrderImg();

		OrdersPage ordersPage = new OrdersPage(driver);
		ordersPage.clickOrderLink();

		Thread.sleep(10000);
		ordersPage.clickCloseButton();
		ordersPage.clickUsernameButton();
		ordersPage.clickSignOutLink();
	}

}