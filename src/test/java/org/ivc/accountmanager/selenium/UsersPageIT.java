/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.selenium;

import java.util.Date;
import org.apache.commons.lang3.ArrayUtils;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.AppConfig;
import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.Role;
import static org.ivc.accountmanager.selenium.Utils.loginIfNecessary;
import org.ivc.accountmanager.selenium.pages.UsersPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test users_page page.
 *
 * @author Sokolov@ivc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(value = "server.port=9000")
@SeleniumTest(driver = FirefoxDriver.class, baseUrl = "http://localhost:9000/accountmanager/users_page")
@ActiveProfiles("test")
public class UsersPageIT {

    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------
    private static final String LDIF_FILE_NAME = "ivc.ldif";

    private static final String[] USER_VALID_LOGINS = new String[]{
        "000", "111", "222"};

    private static final String[] USER_VALID_COMMON_NAMES = new String[]{
        "Roman Osipov", "Slava Sokolov", "Vitaliy Denisov"};

    private static final String[] USER_VALID_ORGANIZATIONS = new String[]{
        "TestOrganization1", "TestOrganization2", "TestOrganization1"};

    private static final String[] USER_VALID_ROLES = new String[]{
        "ADMIN", "ROCKETADMIN", "USER"};

    private static final String VALID_USER_ID = "user";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String VALID_USER_NAME = "name";
    private static final String VALID_USER_LASTNAME = "lastname";
    private static final String VALID_USER_ORGANIZATION = "TestOrganization1";
    private static final String VALID_USER_ROLE = Role.ROCKET_USER;

    //-------------------Fields---------------------------------------------------
    @Autowired
    private LdapContextSource contextSource;

    @Autowired
    private WebDriver driver;

    @Value("${security.admin.name}")
    public String adminName;

    @Value("${security.admin.password}")
    public String adminPassword;

    private UsersPage page;

    @Rule
    public TestName testName = new TestName();

    //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------
    private void addNewUser() {
        page.getLoginInput().sendKeys(VALID_USER_ID);
        page.getNameInput().sendKeys(VALID_USER_NAME);
        page.getLastnameInput().sendKeys(VALID_USER_LASTNAME);
        page.getOrgnameSelect().sendKeys(VALID_USER_ORGANIZATION);
        page.getRoleSelect().sendKeys(VALID_USER_ROLE);
        page.getPasswordInput().sendKeys(VALID_USER_PASSWORD);
        page.getConfirmInput().sendKeys(VALID_USER_PASSWORD);
        page.getActiveInput().click();
        page.getSendFormButton().click();
    }

    private void deleteUser() {
        page.getDeleteUserButton().click();
        page.getConfirmDeleteButton().click();
    }

    private void changePassword() throws Exception {
        page.getChangePasswordButton().click();
        page.getChangePassInput().sendKeys(VALID_USER_PASSWORD);
        page.getChangeConfirmInput().sendKeys(VALID_USER_PASSWORD);
        page.getChangeSubmit().click();
        page.getLogout().click();
        Thread.sleep(3000);
        driver.navigate().to("http://localhost:9000/accountmanager/users_page");
        loginIfNecessary(driver, adminName, VALID_USER_PASSWORD);
    }

    private void editUser() throws Exception {
        page.getEditUserButton().click();

        page.getNameInput().clear();
        page.getLastnameInput().clear();

        page.getNameInput().sendKeys(VALID_USER_NAME);
        page.getLastnameInput().sendKeys(VALID_USER_LASTNAME);
        page.getOrgnameSelect().sendKeys(VALID_USER_ORGANIZATION);
        page.getRoleSelect().sendKeys(VALID_USER_ROLE);
        page.getActiveInput().click();
        page.getSendFormButton().click();
    }

    private void analyzeLog() {
        System.out.println("URL IS " + driver.getCurrentUrl());
        LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : entries) {
            System.out.println(new Date(entry.getTimestamp()) + " "
                    + entry.getLevel() + " "
                    + entry.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Setup data for Users page test.");
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
        LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName(LdapConfig.LDAP_BASE),
                new ClassPathResource(LDIF_FILE_NAME));
        loginIfNecessary(driver, adminName, adminPassword);
        page = PageFactory.initElements(driver, UsersPage.class);
        driver.navigate().refresh();
        Thread.sleep(10000); // pause for render page
        System.out.println("User page ready for test.");
        System.out.println("Start test: " + testName.getMethodName());
    }

    @After
    public void tearDown() {
        analyzeLog();
        System.out.println("Finish test: " + testName.getMethodName());
    }

    @Test
    public void userListShouldBeShown() {
        // assert
        assertThat(extractProperty("text").from(page.getLoginSpans()))
                .hasSize(USER_VALID_LOGINS.length)
                .containsOnly(USER_VALID_LOGINS);
        assertThat(extractProperty("text").from(page.getCommonNameSpans()))
                .hasSize(USER_VALID_COMMON_NAMES.length)
                .containsOnly(USER_VALID_COMMON_NAMES);
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(USER_VALID_ORGANIZATIONS.length)
                .containsOnly(USER_VALID_ORGANIZATIONS);
        assertThat(extractProperty("text").from(page.getRoleSpans()))
                .hasSize(USER_VALID_ROLES.length)
                .containsOnly(USER_VALID_ROLES);
    }

    @Test
    public void validUserShouldBeAddedAndShown() throws Exception {
        // act
        addNewUser();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getLoginSpans()))
                .hasSize(USER_VALID_LOGINS.length + 1)
                .containsOnly(ArrayUtils.add(USER_VALID_LOGINS, VALID_USER_ID));
        assertThat(extractProperty("text").from(page.getCommonNameSpans()))
                .hasSize(USER_VALID_COMMON_NAMES.length + 1)
                .containsOnly(ArrayUtils.add(USER_VALID_COMMON_NAMES,
                                VALID_USER_NAME + " " + VALID_USER_LASTNAME));
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(USER_VALID_ORGANIZATIONS.length + 1)
                .containsOnly(ArrayUtils.add(USER_VALID_ORGANIZATIONS,
                                VALID_USER_ORGANIZATION));
        assertThat(extractProperty("text").from(page.getRoleSpans()))
                .hasSize(USER_VALID_ROLES.length + 1)
                .containsOnly(ArrayUtils.add(USER_VALID_ROLES, VALID_USER_ROLE));
    }

    @Test
    public void userShouldBeDeletedAndUserListMustBeUpdated() throws Exception {
        // act
        deleteUser();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getLoginSpans()))
                .hasSize(USER_VALID_LOGINS.length - 1)
                .containsOnly(ArrayUtils.removeElement(USER_VALID_LOGINS, "222"));
    }

    @Test
    public void userShouldBeEditedAndUserListMustBeUpdated() throws Exception {
        // act
        editUser();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getCommonNameSpans()))
                .hasSize(USER_VALID_COMMON_NAMES.length)
                .containsOnly(VALID_USER_NAME + " " + VALID_USER_LASTNAME,
                        USER_VALID_COMMON_NAMES[0], USER_VALID_COMMON_NAMES[1]);
        assertThat(extractProperty("text").from(page.getRoleSpans()))
                .hasSize(USER_VALID_ROLES.length)
                .containsOnly(VALID_USER_ROLE, USER_VALID_ROLES[0], USER_VALID_ROLES[1]);
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(USER_VALID_ORGANIZATIONS.length)
                .containsOnly(VALID_USER_ORGANIZATION, USER_VALID_ORGANIZATIONS[0],
                        USER_VALID_ORGANIZATIONS[1]);
    }

    @Test
    public void passwordShouldChangedAndUserMustLoginWithNewPassword() throws Exception {
        //act
        changePassword();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getLoginSpans()))
                .hasSize(USER_VALID_LOGINS.length)
                .containsOnly(USER_VALID_LOGINS);
        assertThat(extractProperty("text").from(page.getCommonNameSpans()))
                .hasSize(USER_VALID_COMMON_NAMES.length)
                .containsOnly(USER_VALID_COMMON_NAMES);
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(USER_VALID_ORGANIZATIONS.length)
                .containsOnly(USER_VALID_ORGANIZATIONS);
        assertThat(extractProperty("text").from(page.getRoleSpans()))
                .hasSize(USER_VALID_ROLES.length)
                .containsOnly(USER_VALID_ROLES);
    }

}
