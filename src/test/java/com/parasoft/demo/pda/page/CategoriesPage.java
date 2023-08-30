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
import org.openqa.selenium.support.ui.WebDriverWait;

public class CategoriesPage {

	@FindBy(xpath = "/descendant::button[normalize-space(.)='ADD TO CART'][1]")
	private WebElement addToCart;

	@FindBy(xpath = "/descendant::div[@id='confirm_button']/button")
	private WebElement addToCartButton;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='ADD TO CART'][3]")
	private WebElement addToCartButton2;

	@FindBy(xpath = "/descendant::div[@id='confirm_button']/button")
	private WebElement addToCartButton3;

	@FindBy(xpath = "/descendant::li[@id='cart-button']/div")
	private WebElement rightBarImg;

	@FindBy(xpath = "/descendant::button[normalize-space(.)='PROCEED TO SUBMISSION']")
	private WebElement proceedToSubmission;

	private WebDriver driver;

	private static final Duration DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT = Duration.ofSeconds(15);

	private static final String[] TITLE_WORDS = { "PARASOFT", "DEMO", "APP" };

	public CategoriesPage(WebDriver driver) {
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

	public void clickAddToCart() {
		click(addToCart);
	}

	public void clickAddToCartButton() {
		click(addToCartButton);
	}

	public void clickAddToCartButton2() {
		click(addToCartButton2);
	}

	public void clickAddToCartButton3() {
		click(addToCartButton3);
	}

	public void clickRightBarImg() {
		click(rightBarImg);
	}

	public void clickProceedToSubmission() {
		click(proceedToSubmission);
	}

}