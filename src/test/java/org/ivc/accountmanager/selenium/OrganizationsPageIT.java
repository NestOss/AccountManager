/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.selenium;

import org.apache.commons.lang3.ArrayUtils;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import org.ivc.accountmanager.config.AppConfig;
import org.ivc.accountmanager.config.LdapConfig;
import static org.ivc.accountmanager.selenium.Utils.loginIfNecessary;
import org.ivc.accountmanager.selenium.pages.OrganizationsPage;
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
 * Тест страницы организации.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(value = "server.port=9000")
@SeleniumTest(driver = FirefoxDriver.class, baseUrl = "http://localhost:9000/accountmanager/organizations_page")
@ActiveProfiles("test")
public class OrganizationsPageIT {
    //-------------------Logger---------------------------------------------------
    //-------------------Constants------------------------------------------------

    private static final String LDIF_FILE_NAME = "ivc.ldif";

    private static final String[] ORGANIZATION_VALID_NAMES = new String[]{
        "TestOrganization1", "TestOrganization2"};

    private static final String ORGANIZATION_VALID_NAME = "Это организация №3";

    //-------------------Fields---------------------------------------------------
    @Autowired
    private LdapContextSource contextSource;

    @Autowired
    private WebDriver driver;

    @Value("${security.admin.name}")
    public String adminName;

    @Value("${security.admin.password}")
    public String adminPassword;

    private OrganizationsPage page;

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
        page = PageFactory.initElements(driver, OrganizationsPage.class);
        driver.navigate().refresh();
        Thread.sleep(3000); // pause for render page
        System.out.println("Organization page ready for test.");
        System.out.println("Start test: " + testName.getMethodName());
    }

    @After
    public void tearDown() {
        System.out.println("Finish test: " + testName.getMethodName());
    }

    private void addNewOrganization() {
        page.getOrgnameInput().sendKeys(ORGANIZATION_VALID_NAME);
        page.getSendFormButton().click();
    }

    private void deleteTestOrganization1() {
        page.getDeleteOrg1Button().click();
        page.getConfirmDeleteButton().click();
    }

    private void editTestOrganization1() throws Exception {
       page.getEditOrg1Button().click();
       page.getOrgnameInput().clear();
       page.getOrgnameInput().sendKeys(ORGANIZATION_VALID_NAME);
       page.getSendFormButton().click();
    }
            
    @Test
    public void organizationListShouldBeShown() {
        // assert
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly(ORGANIZATION_VALID_NAMES);
    }

    @Test
    public void validOrganizationShouldBeAddedAndShown() throws Exception {
        // act
        addNewOrganization();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(ORGANIZATION_VALID_NAMES.length + 1)
                .containsOnly(ArrayUtils.add(ORGANIZATION_VALID_NAMES, ORGANIZATION_VALID_NAME));
    }

    @Test
    public void organizationShouldBeDeletedAndOrganizationListMustBeUpdated() throws Exception {
        // act
        deleteTestOrganization1();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(ORGANIZATION_VALID_NAMES.length - 1)
                .containsOnly(ORGANIZATION_VALID_NAMES[1]);
    }
    
    @Test
    public void organizationShouldBeEditedAndOrganizationListMustBeUpdated() throws Exception {
        // act
        editTestOrganization1();
        Thread.sleep(3000);
        // assert
        assertThat(extractProperty("text").from(page.getOrgSpans()))
                .hasSize(ORGANIZATION_VALID_NAMES.length)
                .containsOnly(ORGANIZATION_VALID_NAME, ORGANIZATION_VALID_NAMES[1]);
    }

}
