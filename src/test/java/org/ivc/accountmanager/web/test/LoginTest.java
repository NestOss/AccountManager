/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ivc.accountmanager.web.test;

import org.ivc.accountmanager.config.LdapConfig;
import org.ivc.accountmanager.config.Role;
import org.ivc.accountmanager.config.SecurityConfig;
import org.ivc.accountmanager.web.test.LoginTest.LoginTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test for user login.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = LoginTestConfig.class)
@ActiveProfiles("test")
public class LoginTest {

    //-------------------Inner classes--------------------------------------------
    @SpringBootApplication
    @Import({SecurityConfig.class, LdapConfig.class})
    public static class LoginTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final String LDIF_FILE_NAME = "ivc.ldif";

    //-------------------Fields------------------------------------------------
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LdapContextSource contextSource;

    private MockMvc mockMvc;

    private static final String ADMIN_NAME = "000";
    private static final String ADMIN_PASSWORD = "000";

    //-------------------Before test-------------------------------------------
    @Before
    public void setup() throws Exception {
        LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
        LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName(LdapConfig.LDAP_BASE),
                new ClassPathResource(LDIF_FILE_NAME));
        MockitoAnnotations.initMocks(this);
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    //-------------------Tests-------------------------------------------------
    @Test
    public void loginAsAdmin() throws Exception {
        mockMvc.perform(formLogin("/login").user(ADMIN_NAME).password(ADMIN_PASSWORD))
                .andExpect(authenticated().withRoles(Role.ADMIN));
    }

}
