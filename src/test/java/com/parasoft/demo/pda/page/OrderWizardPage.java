/**
 * 
 */
package com.parasoft.demo.pda.page;

import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import java.time.Duration;
import java.util.Arrays;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OrderWizardPage {

	@FindBy(id = "region_select")
	private WebElement regionSelectDropdown;

	@FindBy(id = "platoon_id_input")
	private WebElement receiverNameInputField;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='GET LOCATION']")
	private WebElement getLocationButton;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='INVOICE ASSIGNMENT']")
	private WebElement invoiceAssignmentButton;

	@FindBy(id = "campaign_id")
	private WebElement campaignIdInputField;

	@FindBy(id = "campaign_number")
	private WebElement serviceNumberInputField;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='GO TO REVIEW']")
	private WebElement goToReviewButton;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='SUBMIT FOR APPROVAL']")
	private WebElement submitForApprovalButton;

	@FindBy(xpath = "/descendant::li[@id='username-button']")
	private WebElement username;

	@FindBy(linkText = "Sign out")
	private WebElement signOutLink;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	private static final String[] TITLE_WORDS = { "PARASOFT", "DEMO", "APP" };

	public OrderWizardPage(WebDriver driver) {
		this.driver = driver;
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		wait.ignoring(StaleElementReferenceException.class);
		Arrays.stream(TITLE_WORDS).forEach(word -> {
			wait.until(attributeContains(By.tagName("title"), "innerHTML", word));
		});
		PageFactory.initElements(driver, this);
	}

	private WebElement waitFor(WebElement element) {
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		wait.ignoring(StaleElementReferenceException.class);
		return wait.until(elementToBeClickable(element));
	}

	private WebElement scrollTo(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", element);
		return element;
	}

	protected WebElement click(WebElement element) {
		WebElement webElement = scrollTo(waitFor(element));
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		return wait.ignoring(ElementClickInterceptedException.class).until(webDriver -> {
			webElement.click();
			return webElement;
		});
	}

	public void selectRegionSelectDropdown(String text) {
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(webdriver -> new Select(regionSelectDropdown).getOptions().stream()
				.anyMatch(element -> text.equals(element.getText())));
		Select dropdown = new Select(regionSelectDropdown);
		dropdown.selectByVisibleText(text);
	}

	public void setReceiverNameInputField(String text) {
		waitFor(receiverNameInputField).clear();
		receiverNameInputField.sendKeys(text);
	}

	public void clickGetLocationButton() {
		click(getLocationButton);
	}

	public void clickInvoiceAssignmentButton() {
		click(invoiceAssignmentButton);
	}

	public void setCampaignIdInputField(String text) {
		waitFor(campaignIdInputField).clear();
		campaignIdInputField.sendKeys(text);
	}

	public void setServiceNumberInputField(String text) {
		waitFor(serviceNumberInputField).clear();
		serviceNumberInputField.sendKeys(text);
	}

	public void clickGoToReviewButton() {
		click(goToReviewButton);
	}

	public void clickSubmitForApprovalButton() {
		click(submitForApprovalButton);
	}

	public void clickUsername() {
		click(username);
	}

	public void clickSignOutLink() {
		click(signOutLink);
	}

}