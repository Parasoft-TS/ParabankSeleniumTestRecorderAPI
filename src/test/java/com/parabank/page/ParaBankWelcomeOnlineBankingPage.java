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

public class ParaBankWelcomeOnlineBankingPage {

	@FindBy(name = "username")
	private WebElement usernameText;

	@FindBy(name = "password")
	private WebElement password;

	@FindBy(css = "input[value='Log In']")
	private WebElement logInSubmit;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	public ParaBankWelcomeOnlineBankingPage(WebDriver driver) {
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

	public void setUsernameText(String text) {
		waitFor(usernameText).clear();
		usernameText.sendKeys(text);
	}

	public void setPassword(String text) {
		waitFor(password).clear();
		password.sendKeys(text);
	}

	public void clickLogInSubmit() {
		click(logInSubmit);
	}

}