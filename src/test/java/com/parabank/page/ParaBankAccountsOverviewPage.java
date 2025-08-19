/**
 * 
 */
package com.parabank.page;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import java.time.Duration;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ParaBankAccountsOverviewPage {

	@FindBy(linkText = "12345")
	private WebElement link;

	@FindBy(linkText = "12456")
	private WebElement link2;

	@FindBy(linkText = "Transfer Funds")
	private WebElement transferFundsLink;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	public ParaBankAccountsOverviewPage(WebDriver driver) {
		this.driver = driver;
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

	public void clickLink() {
		click(link);
	}

	public void clickLink2() {
		click(link2);
	}

	public void clickTransferFundsLink() {
		click(transferFundsLink);
	}

}