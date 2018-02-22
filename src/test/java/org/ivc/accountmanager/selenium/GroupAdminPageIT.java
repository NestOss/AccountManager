/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.selenium;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.AppConfig;
import org.ivc.accountmanager.config.LdapConfig;
import static org.ivc.accountmanager.selenium.Utils.loginIfNecessary;
import org.ivc.accountmanager.selenium.pages.GroupAdminPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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
 *
 * @author Администратор
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(value = "server.port=9000")
@SeleniumTest(driver = FirefoxDriver.class, baseUrl = "http://localhost:9000/accountmanager/group_administrator_page")
@ActiveProfiles("test")
public class GroupAdminPageIT {
  //-------------------Logger---------------------------------------------------

    //-------------------Constants------------------------------------------------
    private static final String LDIF_FILE_NAME = "ivc.ldif";
    private static final String VALID_USER_ID = "user";
    private static final String VALID_USER_PASSWORD = "123";
    private static final String VALID_USER_NAME = "name";
    private static final String VALID_USER_LASTNAME = "lastname";
        private static final String[] USER_VALID_LOGINS = new String[]{
        "000", "111", "222"};

    private static final String[] USER_VALID_COMMON_NAMES = new String[]{
        "Roman Osipov", "Slava Sokolov", "Vitaliy Denisov"};

    private static final String[] USER_VALID_ORGANIZATIONS = new String[]{
        "TestOrganization1", "TestOrganization2", "TestOrganization1"};

    private static final String[] USER_VALID_ROLES = new String[]{
        "ADMIN", "ROCKETADMIN", "USER"};
    //-------------------Fields---------------------------------------------------
    @Autowired
    private LdapContextSource contextSource;

    @Autowired
    private WebDriver driver;

    @Value("${security.admin.name}")
    public String adminName;

    @Value("${security.admin.password}")
    public String adminPassword;

    private GroupAdminPage page;

    @Rule
    public TestName testName = new TestName();
  //-------------------Constructors---------------------------------------------
    //-------------------Getters and setters--------------------------------------
    //-------------------Methods--------------------------------------------------

    @Before
    public void setUp() throws Exception {
        System.out.println("Setup data for Organizations page test.");
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
        LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName(LdapConfig.LDAP_BASE),
                new ClassPathResource(LDIF_FILE_NAME));
        loginIfNecessary(driver, adminName, adminPassword);
        page = PageFactory.initElements(driver, GroupAdminPage.class);
        driver.navigate().refresh();
        Thread.sleep(3000); // pause for render page
        System.out.println("Organization page ready for test.");
        System.out.println("Start test: " + testName.getMethodName());
    }

    @After
    public void tearDown() {
        System.out.println("Finish test: " + testName.getMethodName());
    }

    private void addNewUser() {
        page.getLoginInput().sendKeys(VALID_USER_ID);
        page.getNameInput().sendKeys(VALID_USER_NAME);
        page.getLastnameInput().sendKeys(VALID_USER_LASTNAME);
        page.getPasswordInput().sendKeys(VALID_USER_PASSWORD);
        page.getConfirmInput().sendKeys(VALID_USER_PASSWORD);
        page.getActiveInput().click();
        page.getSendFormButton().click();
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
}
