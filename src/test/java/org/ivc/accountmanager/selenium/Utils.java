/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Utils for UI tests.
 *
 * @author Roman Osipov
 */
public class Utils {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------

    public static void loginIfNecessary(WebDriver driver, String login, String password) {
        try {
            WebElement loginForm = driver.findElement(By.cssSelector("form[name='f']"));
            driver.findElement(By.cssSelector("input[name='username']")).sendKeys(login);
            driver.findElement(By.cssSelector("input[name='password']")).sendKeys(password);
            driver.findElement(By.cssSelector("input[name='submit']")).click();
        } catch (NoSuchElementException e) {
            System.out.println("Already logged");
        }
    }
}
