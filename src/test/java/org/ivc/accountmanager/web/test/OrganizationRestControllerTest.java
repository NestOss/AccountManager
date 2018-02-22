/*
 Информационно-вычислительный центр  
 */
package org.ivc.accountmanager.web.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.is;
import org.ivc.accountmanager.config.SecurityConfig;
import org.ivc.accountmanager.domain.Organization;
import org.ivc.accountmanager.domain.RestError;
import org.ivc.accountmanager.repository.OrganizationRepository;
import org.ivc.accountmanager.web.ExceptionHandlerAdvice;
import org.ivc.accountmanager.web.OrganizationRestController;
import org.ivc.accountmanager.web.TrimmerAdvice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyObject;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;

/**
 * OrganizationRestController tests.
 *
 * @author Roman Osipov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration
@ActiveProfiles("test")
public class OrganizationRestControllerTest {

    //-------------------Inner classes--------------------------------------------
    @SpringBootApplication
    @Import({SecurityConfig.class, OrganizationRestController.class, ExceptionHandlerAdvice.class,
        TrimmerAdvice.class})
    public static class OrganizationRestControllerTestConfig {
    }

    //-------------------Constants------------------------------------------------
    private static final ObjectWriter jsonWriter = new ObjectMapper().writer();

    private static final List<Organization> VALID_ORGANIZATIONS
            = new ArrayList<>(Arrays.asList(new Organization[]{
                new Organization("Org1"),
                new Organization("Org2")}));
    private static final String VALID_ORGANIZATION_NAME = "Организация";
    private static final String VALID_NEW_ORGANIZATION_NAME = "Новая организация";
    private static final String EXCEPTION_MESSAGE = "My Exception Message";

    //-------------------Fields---------------------------------------------------
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    private OrganizationRepository organizationRepository;

    @Autowired
    @InjectMocks
    OrganizationRestController organizationRestController;

    private MockMvc mockMvc;

    //-------------------Before tests------------------------------------------
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    //-------------------Tests-------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnOrganizationListForAdmin() throws Exception {
        // arrange
        when(organizationRepository.findAll())
                .thenReturn(VALID_ORGANIZATIONS);
        // act & assert
        mockMvc.perform(get(OrganizationRestController.ORGANIZATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(VALID_ORGANIZATIONS.size())))
                .andExpect(jsonPath("$[0]." + Organization.NAME_PROPERTY_NAME,
                                is(VALID_ORGANIZATIONS.get(0).getName())))
                .andExpect(jsonPath("$[1]." + Organization.NAME_PROPERTY_NAME,
                                is(VALID_ORGANIZATIONS.get(1).getName())));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyAccessToOrganizationListForUser() throws Exception {
        // act & assert
        mockMvc.perform(get(OrganizationRestController.ORGANIZATIONS_PATH))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldCreateOrganizationForAdmin() throws Exception {
        // arrange
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        when(organizationRepository.create(organization))
                .thenReturn(organization);
        // act & assert
        mockMvc.perform(post(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + Organization.NAME_PROPERTY_NAME,
                                is(VALID_ORGANIZATION_NAME)));
        verify(organizationRepository).create(organization);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForCreateOrganizationForExceptionInServiceCall() throws Exception {
        Organization organization = new Organization(VALID_NEW_ORGANIZATION_NAME);
        doThrow(new IllegalArgumentException(EXCEPTION_MESSAGE))
                .when(organizationRepository).create(anyObject());
        // act & assert
        mockMvc.perform(post(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(EXCEPTION_MESSAGE)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyCreateOrganizationForUser() throws Exception {
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        // act & assert
        mockMvc.perform(post(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldRenameOrganizationForAdmin() throws Exception {
        // arrange
        Organization oldOrganization = new Organization(VALID_ORGANIZATION_NAME);
        Organization newOrganization = new Organization(VALID_NEW_ORGANIZATION_NAME);
        List<Organization> organizations = new ArrayList<>(
                Arrays.asList(oldOrganization, newOrganization));
        // act & assert
        mockMvc.perform(put(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organizations))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + Organization.NAME_PROPERTY_NAME,
                                is(VALID_NEW_ORGANIZATION_NAME)));
        verify(organizationRepository).replace(oldOrganization, newOrganization);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForRenameOrganizationForRequestWithOneOrganization() throws Exception {
        // arrange
        Organization newOrganization = new Organization(VALID_NEW_ORGANIZATION_NAME);
        List<Organization> organizations = new ArrayList<>(Arrays.asList(newOrganization));
        // act & assert
        mockMvc.perform(put(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organizations))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(OrganizationRestController.INVALID_REPLACE_ORGANIZATION_REQUEST_BODY_MESSAGE)));
        verify(organizationRepository, never()).replace(anyObject(), anyObject());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnErrorForRenameOrganizationForExceptionInServiceCall() throws Exception {
        // arrange
        Organization oldOrganization = new Organization("");
        Organization newOrganization = new Organization("");
        List<Organization> organizations = new ArrayList<>(
                Arrays.asList(oldOrganization, newOrganization));
        doThrow(new IllegalArgumentException(EXCEPTION_MESSAGE))
                .when(organizationRepository).replace(anyObject(),anyObject());
        // act & assert
        mockMvc.perform(put(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organizations))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(EXCEPTION_MESSAGE)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyRenameOrganizationForUser() throws Exception {
        Organization oldOrganization = new Organization(VALID_ORGANIZATION_NAME);
        Organization newOrganization = new Organization(VALID_NEW_ORGANIZATION_NAME);
        List<Organization> organizations = new ArrayList<>(
                Arrays.asList(oldOrganization, newOrganization));
        // act & assert
        mockMvc.perform(put(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organizations))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldDeleteOrganizationForAdmin() throws Exception {
        // arrange
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        // act & assert
        mockMvc.perform(delete(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + Organization.NAME_PROPERTY_NAME,
                                is(VALID_ORGANIZATION_NAME)));
        verify(organizationRepository).delete(organization);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void sshouldReturnErrorForDeleteOrganizationForExceptionInServiceCall() throws Exception {
        // arrange
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        doThrow(new IllegalArgumentException(EXCEPTION_MESSAGE))
                .when(organizationRepository).delete(anyObject());
        // act & assert
        mockMvc.perform(delete(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$." + RestError.CLASS_NAME_PROPERTY_NAME,
                                is(IllegalArgumentException.class.getCanonicalName())))
                .andExpect(jsonPath("$." + RestError.MESSAGE_PROPERTY_NAME,
                                is(EXCEPTION_MESSAGE)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldDenyDeleteOrganizationForUser() throws Exception {
        Organization organization = new Organization(VALID_ORGANIZATION_NAME);
        // act & assert
        mockMvc.perform(delete(OrganizationRestController.ORGANIZATIONS_PATH
                + OrganizationRestController.ITEM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWriter.writeValueAsString(organization))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
