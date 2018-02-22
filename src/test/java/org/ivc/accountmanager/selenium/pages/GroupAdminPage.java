package org.ivc.accountmanager.selenium.pages;

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/*
   Информационно-вычислительный центр  
*/

/**
 *
 * @author Администратор
 */
public class GroupAdminPage {
  //-------------------Logger---------------------------------------------------
  
  //-------------------Constants------------------------------------------------

  //-------------------Fields---------------------------------------------------
    @FindBy(css = "span[ng-bind='user.login']")
    List<WebElement> loginSpans;

    @FindBy(css = "span[ng-bind='user.commonName']")
    List<WebElement> commonNameSpans;

    @FindBy(css = "span[ng-bind='user.organizationName']")
    List<WebElement> orgSpans;

    @FindBy(css = "span[ng-bind='user.role']")
    List<WebElement> roleSpans;

    @FindBy(id = "login")
    WebElement loginInput;

    @FindBy(id = "name")
    WebElement nameInput;

    @FindBy(id = "lastname")
    WebElement lastnameInput;

    @FindBy(id = "orgname")
    WebElement orgnameSelect;

    @FindBy(id = "password")
    WebElement passwordInput;

    @FindBy(id = "confirm")
    WebElement confirmInput;

    @FindBy(id = "active")
    WebElement activeInput;

    @FindBy(id = "sendFormButton")
    WebElement sendFormButton;

    @FindBy(xpath = "//span[text()='222']/../../td[6]/button[3]")
    WebElement deleteUserButton;

    @FindBy(xpath = "//span[text()='222']/../../td[6]/button[1]")
    WebElement editUserButton;

    @FindBy(xpath = "//span[text()='000']/../../td[6]/button[2]")
    WebElement changePasswordButton;

    @FindBy(xpath = "//div[@class='ui-dialog-buttonset']/button[1]")
    WebElement confirmDeleteButton;

    @FindBy(id = "changedPass")
    WebElement changePassInput;

    @FindBy(id = "changedConfirm")
    WebElement changeConfirmInput;

    @FindBy(xpath = "//span[@id='ui-id-2']/../../div[3]/div[1]/button[1]")
    WebElement changeSubmit;

    @FindBy(id = "logout")
    WebElement logout;

  //-------------------Getters and setters--------------------------------------
    public List<WebElement> getLoginSpans() {
        return loginSpans;
    }

    public List<WebElement> getCommonNameSpans() {
        return commonNameSpans;
    }

    public List<WebElement> getOrgSpans() {
        return orgSpans;
    }

    public List<WebElement> getRoleSpans() {
        return roleSpans;
    }

    public WebElement getLoginInput() {
        return loginInput;
    }

    public WebElement getNameInput() {
        return nameInput;
    }

    public WebElement getLastnameInput() {
        return lastnameInput;
    }

    public WebElement getOrgnameSelect() {
        return orgnameSelect;
    }

    public WebElement getPasswordInput() {
        return passwordInput;
    }

    public WebElement getConfirmInput() {
        return confirmInput;
    }

    public WebElement getActiveInput() {
        return activeInput;
    }

    public WebElement getSendFormButton() {
        return sendFormButton;
    }

    public WebElement getDeleteUserButton() {
        return deleteUserButton;
    }

    public WebElement getConfirmDeleteButton() {
        return confirmDeleteButton;
    }

    public WebElement getEditUserButton() {
        return editUserButton;
    }

    public WebElement getChangePasswordButton() {
        return changePasswordButton;
    }

    public WebElement getLogout() {
        return logout;
    }

    public WebElement getChangePassInput() {
        return changePassInput;
    }

    public WebElement getChangeConfirmInput() {
        return changeConfirmInput;
    }

    public WebElement getChangeSubmit() {
        return changeSubmit;
    }


  //-------------------Methods--------------------------------------------------

}