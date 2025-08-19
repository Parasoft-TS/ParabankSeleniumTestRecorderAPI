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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ParaBankTransferFundsPage {

	@FindBy(name = "input")
	private WebElement inputText;

	@FindBy(id = "toAccountId")
	private WebElement toAccountIdSelectOne;

	@FindBy(css = "input[value='Transfer']")
	private WebElement transferSubmit;

	@FindBy(linkText = "Bill Pay")
	private WebElement billPayLink;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	public ParaBankTransferFundsPage(WebDriver driver) {
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

	public void setInputText(String text) {
		waitFor(inputText).clear();
		inputText.sendKeys(text);
	}

	public void selectToAccountIdSelectOne(String text) {
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(webdriver -> new Select(toAccountIdSelectOne).getOptions().stream()
				.anyMatch(element -> text.equals(element.getText())));
		Select dropdown = new Select(toAccountIdSelectOne);
		dropdown.selectByVisibleText(text);
	}

	public void clickTransferSubmit() {
		click(transferSubmit);
	}

	public void clickBillPayLink() {
		click(billPayLink);
	}

}