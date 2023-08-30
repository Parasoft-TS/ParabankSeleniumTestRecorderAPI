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

public class HomePage {

	@FindBy(linkText = "Sleeping bags")
	private WebElement sleepingBagsLink;

	@FindBy(xpath = "//tr[@class='order_content ng-scope'][1]/td/a")
	private WebElement orderLink;

	@FindBy(name = "status")
	private WebElement statusDropdown;

	@FindBy(xpath = "/descendant::button[@id='save_btn']")
	private WebElement saveAndSendResponse;

	@FindBy(xpath = "/descendant::li[@id='username-button']")
	private WebElement usernameButton;

	@FindBy(linkText = "Sign out")
	private WebElement signOutLink;

	@FindBy(xpath = "/descendant::li[@id='requisition-button']")
	private WebElement orderImg;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	private static final String[] TITLE_WORDS = { "PARASOFT", "DEMO", "APP" };

	public HomePage(WebDriver driver) {
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

	public void clickSleepingBagsLink() {
		click(sleepingBagsLink);
	}

	public void clickOrderLink() {
		click(orderLink);
	}

	public void selectStatusDropdown(String text) {
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(webdriver -> new Select(statusDropdown).getOptions().stream()
				.anyMatch(element -> text.equals(element.getText())));
		Select dropdown = new Select(statusDropdown);
		dropdown.selectByVisibleText(text);
	}

	public void clickSaveAndSendResponse() {
		click(saveAndSendResponse);
	}

	public void clickUsername() {
		click(usernameButton);
	}

	public void clickSignOutLink() {
		click(signOutLink);
	}

	public void clickOrderImg() {
		click(orderImg);
	}

}