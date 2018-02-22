/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.selenium.pages;

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 *
 * @author Roman Osipov
 */
public class OrganizationsPage {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    //-------------------Fields---------------------------------------------------

    @FindBy(css = "span[ng-bind='org.name']")
    List<WebElement> orgSpans;

    @FindBy(id = "orgname")
    WebElement orgnameInput;

    @FindBy(id = "sendFormButton")
    WebElement sendFormButton;

    @FindBy(xpath = "//span[text()='TestOrganization1']/../../td[2]/button[2]")
    WebElement deleteOrg1Button;
    
    @FindBy(xpath = "//span[text()='TestOrganization1']/../../td[2]/button[1]")
    WebElement editOrg1Button;
    
    @FindBy(xpath = "//div[@class='ui-dialog-buttonset']/button[1]")
    WebElement confirmDeleteButton;

    // div:contains('Text')
    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    public List<WebElement> getOrgSpans() {
        return orgSpans;
    }

    public WebElement getOrgnameInput() {
        return orgnameInput;
    }

    public WebElement getSendFormButton() {
        return sendFormButton;
    }

    public WebElement getDeleteOrg1Button() {
        return deleteOrg1Button;
    }

    public WebElement getConfirmDeleteButton() {
        return confirmDeleteButton;
    }

    public WebElement getEditOrg1Button() {
        return editOrg1Button;
    }
    
}
